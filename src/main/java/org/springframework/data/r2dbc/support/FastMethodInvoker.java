package org.springframework.data.r2dbc.support;

import org.apache.commons.beanutils.ConvertUtils;
import org.springframework.cglib.reflect.FastClass;
import org.springframework.cglib.reflect.FastMethod;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import static java.lang.reflect.Modifier.isStatic;

/**
 * Utilities for reflection interaction.
 *
 * @author Lao Tsing
 */
public final class FastMethodInvoker {
    private static final String UUID_REGEX = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}";
    private static final String NUMBER_REGEX = "^\\d+$";
    private static final String BOOLEAN_REGEX = "^(true|false)$";
    private static final String DOUBLE_REGEX = "^\\d+\\.\\d+$";
    private static final ConcurrentMap<Class<?>, List<Field>> reflectionStorage = new ConcurrentReferenceHashMap<>(720);
    private static final ConcurrentMap<String, FastMethod> methodStorage = new ConcurrentReferenceHashMap<>(720);
    private static final ConcurrentMap<String, Boolean> annotationStorage = new ConcurrentReferenceHashMap<>(720);
    private static final String SET = "set", GET = "get", IS = "is", DOT = ".";

    public static List<Field> reflectionStorage(Class<?> classKey) {
        if (reflectionStorage.containsKey(classKey))
            return reflectionStorage.get(classKey);
        else {
            List<Field> reflectionDeclaredFields = new ArrayList<>();
            Class<?> recursion = classKey;
            do {
                reflectionDeclaredFields.addAll(Arrays.asList(recursion.getDeclaredFields()));
                recursion = recursion.getSuperclass();
            } while (recursion != null);
            reflectionStorage.put(classKey, reflectionDeclaredFields);
            return reflectionDeclaredFields;
        }
    }

    public static FastMethod getCacheMethod(String classKey) {
        return methodStorage.get(classKey);
    }

    public static void setCacheMethod(String classKey, FastMethod fastMethod) {
        methodStorage.put(classKey, fastMethod);
    }

    public static Boolean has(Object any, String name) {
        return has(any.getClass(), name);
    }

    public static Boolean has(Class<?> cls, String name) {
        for (Field field : reflectionStorage(cls)) {
            if (field.getName().equals(name)) return true;
        }
        return false;
    }

    public static Field getField(Class<?> cls, String name) {
        for (Field field : reflectionStorage(cls)) {
            if (field.getName().equals(name)) return field;
        }
        return null;
    }

    public static <T> T copy(Object source, T target) {
        for (Field sourceField : reflectionStorage(source.getClass())) {
            if (has(target, sourceField.getName())) {
                setValue(target, sourceField.getName(), getValue(source, sourceField.getName()));
            }
        }
        return target;
    }

    public static <T> T copyNotNull(Object source, T target) {
        for (Field sourceField : reflectionStorage(source.getClass())) {
            if (has(target, sourceField.getName())) {
                var value = getValue(source, sourceField.getName());
                if (value != null) {
                    setValue(target, sourceField.getName(), value);
                }
            }
        }
        return target;
    }

    public static Map<String, ?> objectToMap(Object any) {
        return reflectionStorage(any.getClass()).stream()
                .filter(field -> !isStatic(field.getModifiers()) && getValue(any, field.getName()) != null)
                .collect(Collectors.toMap(Field::getName, (field) -> getValue(any, field.getName())));
    }

    public static Map<String, ?> objectsToMap(Collection<?> collection, String keyName, String valueName) {
        return collection.stream()
                .filter(entry -> getValue(entry, valueName) != null)
                .collect(Collectors.toMap((entry) -> (String) getValue(entry, keyName), (entry) -> getValue(entry, valueName)));
    }

    public static void setValue(Object any, String name, Object value) {
        for (Field field : reflectionStorage(any.getClass())) {
            if (field.getName().equals(name)) {
                String methodName = SET + StringUtils.capitalize(name);
                String fastMethodKey = any.getClass().getName() + DOT + methodName;
                FastMethod fastMethod = getCacheMethod(fastMethodKey);
                if (fastMethod == null) {
                    fastMethod = FastClass.create(any.getClass()).getMethod(methodName, new Class[]{field.getType()});
                    setCacheMethod(fastMethodKey, fastMethod);
                }
                try {
                    if (!field.getType().equals(String.class) && value instanceof String) {
                        value = stringToObject((String) value, field.getType());
                    }
                    fastMethod.invoke(any, new Object[]{value});
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static void setMapValues(Object any, Map<String, ?> map) {
        for (var name : map.keySet()) {
            for (Field field : reflectionStorage(any.getClass())) {
                if (field.getName().equals(name)) {
                    String methodName = SET + StringUtils.capitalize(name);
                    String fastMethodKey = any.getClass().getName() + DOT + methodName;
                    FastMethod fastMethod = getCacheMethod(fastMethodKey);
                    if (fastMethod == null) {
                        fastMethod = FastClass.create(any.getClass()).getMethod(methodName, new Class[]{field.getType()});
                        setCacheMethod(fastMethodKey, fastMethod);
                    }
                    try {
                        Object value;
                        if (!field.getType().equals(String.class) && map.get(name) instanceof String) {
                            value = stringToObject((String) map.get(name), field.getType());
                        } else
                            value = map.get(name);
                        fastMethod.invoke(any, new Object[]{value});
                    } catch (InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    public static Object getValue(Object any, String name) {
        for (Field field : reflectionStorage(any.getClass())) {
            if (field.getName().equals(name) && !isStatic(field.getModifiers())) {
                for (String prefix : Arrays.asList(GET, IS)) {
                    String methodName = prefix + StringUtils.capitalize(field.getName());
                    String fastMethodKey = any.getClass().getName() + DOT + methodName;
                    FastMethod fastMethod = getCacheMethod(fastMethodKey);
                    if (fastMethod == null) {
                        try {
                            fastMethod = FastClass.create(any.getClass()).getMethod(methodName, null);
                        } catch (NoSuchMethodError e) {
                            continue;
                        }
                    }
                    setCacheMethod(fastMethodKey, fastMethod);
                    try {
                        return fastMethod.invoke(any, null);
                    } catch (InvocationTargetException e) {
                        continue;
                    }
                }
            }
        }
        return null;
    }

    public static Object stringToObject(final String object, final Class<?> cls) {
        if (object != null) {
            if (cls.equals(String.class)) return object;
            if (object.matches(NUMBER_REGEX) || object.matches(DOUBLE_REGEX) || object.matches(BOOLEAN_REGEX)) {
                return ConvertUtils.convert(object, cls);
            }
            switch (cls.getSimpleName()) {
                case "UUID":
                    if (object.matches(UUID_REGEX))
                        return UUID.fromString(object);
                    else
                        return null;
                case "BigInteger":
                    return BigInteger.valueOf(Long.parseLong(object));
                case "byte[]":
                    return object.getBytes(StandardCharsets.UTF_8);
                default:
                    try {
                        String fastMethodKey = cls.getName() + ".parse";
                        FastMethod fastMethod = getCacheMethod(fastMethodKey);
                        if (fastMethod == null) {
                            fastMethod = FastClass.create(cls).getMethod("parse", new Class[]{CharSequence.class});
                            setCacheMethod(fastMethodKey, fastMethod);
                        }
                        return fastMethod.invoke(null, new Object[]{object});
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
            }
        }
        return null;
    }

    public static Optional<Field> getFieldByAnnotation(final Class<?> cls, final Class<?> ann) {
        return getFieldsByAnnotation(cls, ann).stream().findFirst();
    }

    public static List<Field> getFieldsByAnnotation(final Class<?> cls, final Class<?> ann) {
        Class<?> c = cls;
        var result = new ArrayList<Field>();
        while (c != null) {
            for (Field field : reflectionStorage(c)) {
                var key = ann.getSimpleName().concat(c.getSimpleName()).concat(field.getName());
                if (annotationStorage.containsKey(key)) {
                    if (annotationStorage.get(key)) {
                        result.add(field);
                    }
                } else {
                    Annotation[] annotations = field.getDeclaredAnnotations();
                    for (Annotation annotation : annotations) {
                        if (annotation.annotationType() == ann) {
                            annotationStorage.put(key, true);
                            result.add(field);
                        } else
                            annotationStorage.put(key, false);
                    }
                }
            }
            c = c.getSuperclass();
        }
        return result;
    }

    public static Set<Field> getFields(Class<?> cls, String fiendName, Class<?>...annotations) {
        var result = new HashSet<Field>();
        if (has(cls, fiendName)) {
            result.add(getField(cls, fiendName));
        }
        for(Class<?> ann : annotations) {
            var fields = getFieldsByAnnotation(cls, ann);
            result.addAll(fields);
        }
        return result;
    }
}