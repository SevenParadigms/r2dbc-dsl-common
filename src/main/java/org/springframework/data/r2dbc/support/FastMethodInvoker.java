package org.springframework.data.r2dbc.support;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.cglib.reflect.FastClass;
import org.springframework.cglib.reflect.FastMethod;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.RegexPatternTypeFilter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.lang.reflect.Modifier.isStatic;

/**
 * Utilities for reflection interaction.
 *
 * @author Lao Tsing
 */
public final class FastMethodInvoker {
	public static final String UUID_REGEX = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}";
	public static final String NUMBER_REGEX = "^\\d+$";
	public static final String BOOLEAN_REGEX = "^(true|false)$";
	public static final String DOUBLE_REGEX = "^\\d+\\.\\d+$";

	private static final ConcurrentMap<Class<?>, List<Field>> reflectionStorage = new ConcurrentReferenceHashMap<>(256);
	private static final ConcurrentMap<String, FastMethod> methodStorage = new ConcurrentReferenceHashMap<>(256);
	private static final ConcurrentMap<String, Boolean> annotationStorage = new ConcurrentReferenceHashMap<>();
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

	@NonNull
	public static Boolean has(@NonNull Object any, String name) {
		return has(any.getClass(), name);
	}

	@NonNull
	public static Boolean has(Class<?> cls, String name) {
		for (Field field : reflectionStorage(cls)) {
			if (field.getName().equals(name)) return true;
		}
		return false;
	}

	@Nullable
	public static Field getField(Class<?> cls, String name) {
		for (Field field : reflectionStorage(cls)) {
			if (field.getName().equals(name)) return field;
		}
		return null;
	}

	public static <T> T copy(@NonNull Object source, T target) {
		for (Field targetField : reflectionStorage(target.getClass())) {
			if (has(source, targetField.getName())) {
				var sourceValue = getValue(source, targetField.getName());
				var sourceField = getField(source.getClass(), targetField.getName());
				assert sourceField != null;
				if (sourceValue != null && !sourceField.getType().equals(targetField.getType())) {
					if (sourceValue instanceof Enum) {
						sourceValue = Enum.valueOf((Class<? extends Enum>) targetField.getType(), ((Enum) sourceValue).name());
					} else {
						try {
							sourceValue = stringToObject(ConvertUtils.convert(sourceValue), targetField.getType());
						} catch (Exception ignore) {
						}
					}
				}
				setValue(target, targetField.getName(), sourceValue);
			}
		}
		return target;
	}

	public static <T> T copyNotNull(@NonNull Object source, T target) {
		for (Field targetField : reflectionStorage(target.getClass())) {
			if (has(source, targetField.getName())) {
				var sourceValue = getValue(source, targetField.getName());
				if (sourceValue != null) {
					if (sourceValue instanceof JsonNode && ((JsonNode) sourceValue).isNull()) continue;
					var sourceField = getField(source.getClass(), targetField.getName());
					assert sourceField != null;
					if (!sourceField.getType().equals(targetField.getType())) {
						if (sourceValue instanceof Enum) {
							sourceValue = Enum.valueOf((Class<? extends Enum>) targetField.getType(), ((Enum) sourceValue).name());
						} else {
							try {
								sourceValue = stringToObject(ConvertUtils.convert(sourceValue), targetField.getType());
							} catch (Exception ignore) {
							}
						}
					}
					setValue(target, targetField.getName(), sourceValue);
				}
			}
		}
		return target;
	}

	public static <T> T copyIsNull(@NonNull Object source, T target) {
		for (Field targetField : reflectionStorage(target.getClass())) {
			if (has(source, targetField.getName())) {
				var targetValue = getValue(target, targetField.getName());
				if (targetValue == null || (targetValue instanceof JsonNode && ((JsonNode) targetValue).isNull())) {
					var sourceValue = getValue(source, targetField.getName());
					var sourceField = getField(source.getClass(), targetField.getName());
					assert sourceField != null;
					if (sourceValue != null && !sourceField.getType().equals(targetField.getType())) {
						if (sourceValue instanceof Enum) {
							sourceValue = Enum.valueOf((Class<? extends Enum>) targetField.getType(), ((Enum) sourceValue).name());
						} else {
							try {
								sourceValue = stringToObject(ConvertUtils.convert(sourceValue), targetField.getType());
							} catch (Exception ignore) {
							}
						}
					}
					setValue(target, targetField.getName(), sourceValue);
				}
			}
		}
		return target;
	}

	@NonNull
	public static Map<String, ?> objectToMap(@NonNull Object any) {
		return reflectionStorage(any.getClass()).stream()
				.filter(field -> !isStatic(field.getModifiers()) && getValue(any, field.getName()) != null)
				.collect(Collectors.toMap(Field::getName, (field) -> getValue(any, field.getName())));
	}

	@NonNull
	public static Map<String, ?> objectsToMap(@NonNull Collection<?> collection, @NonNull String keyName, @NonNull String valueName) {
		return collection.stream()
				.filter(entry -> getValue(entry, valueName) != null)
				.collect(Collectors.toMap((entry) -> ConvertUtils.convert(getValue(entry, keyName)), (entry) -> getValue(entry, valueName)));
	}

	public static void setValue(@NonNull Object any, @NonNull String name, @Nullable Object value) {
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

	public static void setMapValues(@NonNull Object any, @NonNull Map<String, ?> map) {
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

	@Nullable
	public static <T> T getValue(@NonNull Object any, @NonNull String name, final Class<T> cls) {
		return (T) getValue(any, name);
	}

	@Nullable
	public static Object getValue(@NonNull Object any, @NonNull String name) {
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
					} catch (InvocationTargetException ignored) {
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
					if (object.matches(NUMBER_REGEX))
						return BigInteger.valueOf(Long.parseLong(object));
					else
						return null;
				case "BigDecimal":
					if (object.matches(DOUBLE_REGEX) || object.matches(NUMBER_REGEX))
						return new BigDecimal(Double.parseDouble(object), new MathContext(4, RoundingMode.HALF_EVEN));
					else
						return null;
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
					} catch (Exception ignore) {
					}
			}
		}
		return null;
	}

	@NonNull
	public static Optional<Field> getFieldByAnnotation(final Class<?> cls, final Class<?> ann) {
		return getFieldsByAnnotation(cls, ann).stream().findFirst();
	}

	@NonNull
	public static List<Field> getFieldsByAnnotation(final Class<?> cls, final Class<?> ann) {
		Class<?> c = cls;
		var result = new ArrayList<Field>();
		while (c != null) {
			for (Field field : reflectionStorage(c)) {
				var key = ann.getSimpleName().concat("_").concat(c.getSimpleName()).concat("_").concat(field.getName());
				if (annotationStorage.containsKey(key)) {
					if (annotationStorage.get(key)) {
						result.add(field);
					}
				} else {
					var annotations = field.getDeclaredAnnotations();
					for (Annotation annotation : annotations) {
						if (annotation.annotationType() == ann) {
							annotationStorage.put(key, true);
							result.add(field);
						}
					}
					if (!result.contains(field)) {
						annotationStorage.put(key, false);
					}
				}
			}
			c = c.getSuperclass();
		}
		return result;
	}

	@NonNull
	public static Set<Field> getFields(Class<?> cls, String fiendName, Class<?>... annotations) {
		var result = new HashSet<Field>();
		if (!ObjectUtils.isEmpty(fiendName) && has(cls, fiendName)) {
			result.add(getField(cls, fiendName));
		}
		if (ObjectUtils.isNotEmpty(annotations)) {
			for (Class<?> ann : annotations) {
				var fields = getFieldsByAnnotation(cls, ann);
				result.addAll(fields);
			}
		}
		return result;
	}

	public static Set<BeanDefinition> findClasses(Class<?> example) {
		return findClasses(example.getPackageName());
	}

	public static Set<BeanDefinition> findClasses(String javaPackage) {
		final ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
		provider.addIncludeFilter(new RegexPatternTypeFilter(Pattern.compile(".*")));
		return provider.findCandidateComponents(javaPackage);
	}

	public static <T> T clone(@NonNull final T source, final Object... copy) {
		T clone;
		try {
			Constructor<?> constructor = source.getClass().getDeclaredConstructor();
			constructor.setAccessible(true);
			clone = (T) constructor.newInstance();
			FastMethodInvoker.copy(source, clone);
			if (ObjectUtils.isNotEmpty(copy)) {
				for (var src : copy) {
					FastMethodInvoker.copy(src, clone);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return clone;
	}
}