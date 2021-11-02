package org.springframework.data.r2dbc.support;

import org.apache.commons.beanutils.ConvertUtils;
import org.springframework.cglib.reflect.FastClass;
import org.springframework.cglib.reflect.FastMethod;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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

    public static Boolean isField(Object any, String name) {
        return isField(any.getClass(), name);
    }

    public static Boolean isField(Class<?> cls, String name) {
        for (Field field : FastMethodInvoker.reflectionStorage(cls)) {
            if (field.getName().equals(name)) return true;
        }
        return false;
    }

    public static Field getField(Object any, String name) {
        for (Field field : FastMethodInvoker.reflectionStorage(any.getClass())) {
            if (field.getName().equals(name)) return field;
        }
        return null;
    }

    public static Object copyTo(Object source, Object target) {
        for (Field sourceField : FastMethodInvoker.reflectionStorage(source.getClass())) {
            if (isField(target, sourceField.getName())) {
                setValue(target, sourceField.getName(), getValue(source, sourceField.getName()));
            }
        }
        return target;
    }

    public static Map<String, ?> convertMap(Object any) {
        return FastMethodInvoker.reflectionStorage(any.getClass()).stream()
                .filter(field -> !isStatic(field.getModifiers()))
                .collect(Collectors.toMap(Field::getName, (field) -> getValue(field, any, field.getName())));
    }

    public static Map<String, ?> convertMap(Collection<?> collection, String keyName, String valueName) {
        return collection.stream().collect(
                Collectors.toMap((entry) -> (String) getValue(entry, keyName), (entry) -> getValue(entry, valueName)));
    }

    public static void setValue(Object any, String name, Object value) {
        for (Field field : FastMethodInvoker.reflectionStorage(any.getClass())) {
            if (field.getName().equals(name)) {
                String methodName = SET + StringUtils.capitalize(name);
                String fastMethodKey = any.getClass().getName() + DOT + methodName;
                FastMethod fastMethod = FastMethodInvoker.getCacheMethod(fastMethodKey);
                if (fastMethod == null) {
                    fastMethod = FastClass.create(any.getClass()).getMethod(methodName, new Class[]{field.getType()});
                    FastMethodInvoker.setCacheMethod(fastMethodKey, fastMethod);
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
            for (Field field : FastMethodInvoker.reflectionStorage(any.getClass())) {
                if (field.getName().equals(name)) {
                    String methodName = SET + StringUtils.capitalize(name);
                    String fastMethodKey = any.getClass().getName() + DOT + methodName;
                    FastMethod fastMethod = FastMethodInvoker.getCacheMethod(fastMethodKey);
                    if (fastMethod == null) {
                        fastMethod = FastClass.create(any.getClass()).getMethod(methodName, new Class[]{field.getType()});
                        FastMethodInvoker.setCacheMethod(fastMethodKey, fastMethod);
                    }
                    try {
                        Object value = null;
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
        for (Field field : FastMethodInvoker.reflectionStorage(any.getClass())) {
            return getValue(field, any, name);
        }
        return null;
    }

    public static Object getValue(Field field, Object any, String name) {
        if (field.getName().equals(name) && !isStatic(field.getModifiers())) {
            for (String prefix : Arrays.asList(GET, IS)) {
                String methodName = prefix + StringUtils.capitalize(name);
                String fastMethodKey = any.getClass().getName() + DOT + methodName;
                FastMethod fastMethod = FastMethodInvoker.getCacheMethod(fastMethodKey);
                if (fastMethod == null) {
                    try {
                        fastMethod = FastClass.create(any.getClass()).getMethod(methodName, null);
                    } catch (NoSuchMethodError e) {
                        throw new RuntimeException(e);
                    }
                }
                FastMethodInvoker.setCacheMethod(fastMethodKey, fastMethod);
                try {
                    return fastMethod.invoke(any, null);
                } catch (InvocationTargetException e) {
                    continue;
                }
            }
        }
        return null;
    }

    public static Object stringToObject(final String object, final Class<?> cls) {
        try {
            if (cls.equals(UUID.class)) {
                if (object.matches(UUID_REGEX))
                    return UUID.fromString(object);
                else
                    return null;
            }
            if (object.matches(NUMBER_REGEX) || object.matches(DOUBLE_REGEX) || object.matches(BOOLEAN_REGEX))
                return ConvertUtils.convert(object, cls);
            else {
                String fastMethodKey = cls.getName() + ".parse";
                FastMethod fastMethod = FastMethodInvoker.getCacheMethod(fastMethodKey);
                if (fastMethod == null) {
                    fastMethod = FastClass.create(cls).getMethod("parse", new Class[]{CharSequence.class});
                    FastMethodInvoker.setCacheMethod(fastMethodKey, fastMethod);
                }
                return fastMethod.invoke(null, new Object[]{object});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}