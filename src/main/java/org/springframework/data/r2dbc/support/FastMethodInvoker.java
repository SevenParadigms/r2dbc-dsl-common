package org.springframework.data.r2dbc.support;

import org.springframework.cglib.reflect.FastClass;
import org.springframework.cglib.reflect.FastMethod;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentMap;

/**
 * Utilities for reflection interaction.
 *
 * @author Lao Tsing
 */
public final class FastMethodInvoker {
    public static final String UUID_REGEX = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}";
    public static final String NUMBER_REGEX = "^\\d+$";
    public static final String DOUBLE_REGEX = "^\\d+\\.\\d+$";

    private static final ConcurrentMap<Class<?>, List<Field>> reflectionStorage = new ConcurrentReferenceHashMap<>(720);
    private static final ConcurrentMap<String, FastMethod> methodStorage = new ConcurrentReferenceHashMap<>(720);

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

    public static Map<String, ?> getMapValues(Object any) {
        Map<String, Object> map = new HashMap<>();
        FastMethodInvoker.reflectionStorage(any.getClass()).forEach(field -> map.put(field.getName(), getValue(field, field.getName())));
        return map;
    }

    public static Map<String, ?> getMapValues(Collection<?> collection, String keyName, String valueName) {
        Map<String, Object> map = new HashMap<>();
        collection.forEach(it -> map.put((String) getValue(it, keyName), getValue(it, valueName)));
        return map;
    }

    public static void setValue(Object any, String name, Object value) {
        for (Field field : FastMethodInvoker.reflectionStorage(any.getClass())) {
            if (field.getName().equals(name)) {
                String methodName = "set" + StringUtils.capitalize(name);
                String fastMethodKey = any.getClass().getName() + "." + methodName;
                FastMethod fastMethod = FastMethodInvoker.getCacheMethod(fastMethodKey);
                if (fastMethod == null) {
                    fastMethod = FastClass.create(any.getClass()).getMethod(methodName, new Class[] { field.getType() });
                    FastMethodInvoker.setCacheMethod(fastMethodKey, fastMethod);
                }
                try {
                    fastMethod.invoke(any, new Object[] { value });
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e.getCause());
                }
            }
        }
    }

    public static void setMapValues(Object any, Map<String, ?> map) {
        for (var name : map.keySet()) {
            for (Field field : FastMethodInvoker.reflectionStorage(any.getClass())) {
                if (field.getName().equals(name)) {
                    String methodName = "set" + StringUtils.capitalize(name);
                    String fastMethodKey = any.getClass().getName() + "." + methodName;
                    FastMethod fastMethod = FastMethodInvoker.getCacheMethod(fastMethodKey);
                    if (fastMethod == null) {
                        fastMethod = FastClass.create(any.getClass()).getMethod(methodName, new Class[] { field.getType() });
                        FastMethodInvoker.setCacheMethod(fastMethodKey, fastMethod);
                    }
                    try {
                        Object value = null;
                        if (map.get(name) instanceof String) {
                            value = stringToObject((String) map.get(name));
                        }
                        fastMethod.invoke(any, new Object[] { value });
                    } catch (InvocationTargetException e) {
                        throw new RuntimeException(e.getCause());
                    }
                }
            }
        }
    }

    public static Object getValue(Object any, String name) {
        for (Field field : FastMethodInvoker.reflectionStorage(any.getClass())) {
            if (field.getName().equals(name)) {
                for (String prefix : Arrays.asList("get", "is")) {
                    String methodName = prefix + StringUtils.capitalize(name);
                    String fastMethodKey = any.getClass().getName() + "." + methodName;
                    FastMethod fastMethod = FastMethodInvoker.getCacheMethod(fastMethodKey);
                    if (fastMethod == null) {
                        try {
                            fastMethod = FastClass.create(any.getClass()).getMethod(methodName, null);
                        } catch (NoSuchMethodError ex) {
                            ex.printStackTrace();
                        }
                    }
                    Object result = null;
                    if (fastMethod != null) {
                        FastMethodInvoker.setCacheMethod(fastMethodKey, fastMethod);
                        try {
                            result = fastMethod.invoke(any, null);
                        } catch (InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    return result;
                }
            }
        }
        return null;
    }

    public static Object stringToObject(final String object) {
        if (object.matches(NUMBER_REGEX)) return Long.parseLong(object);
        if (object.matches(DOUBLE_REGEX)) return Double.parseDouble(object);
        if (Arrays.asList(Boolean.TRUE.toString(), Boolean.FALSE.toString()).contains(object.toLowerCase()))
            return Boolean.TRUE.toString().equalsIgnoreCase(object);
        if (object.matches(UUID_REGEX)) return UUID.fromString(object);
        return object;
    }
}