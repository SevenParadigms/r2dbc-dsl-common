package org.sevenparadigms.kotlin.common

import org.apache.commons.lang3.ObjectUtils
import org.apache.commons.lang3.StringUtils
import org.springframework.data.r2dbc.support.FastMethodInvoker
import java.lang.reflect.Field
import java.util.*

fun Any.setValue(name: String, value: Any?) = FastMethodInvoker.setValue(this, name, value)

fun Any.getValue(name: String): Any? = FastMethodInvoker.getValue(this, name)

inline fun <reified T> Any.getValue(name: String, cls: Class<T>): T? = FastMethodInvoker.getValue(this, name, cls) as T

fun Any.setValue(name: Enum<*>, value: Any) = FastMethodInvoker.setValue(this, name.name, value)

fun Any.getValue(name: Enum<*>): Any? = FastMethodInvoker.getValue(this, name.name)

inline fun <reified T> Any.getValue(name: Enum<*>, cls: Class<T>): T? = FastMethodInvoker.getValue(this, name.name, cls) as T

inline fun <reified T> String.stringToObject(cls: Class<T>): T = FastMethodInvoker.stringToObject(this, cls) as T

fun Any.getFieldByAnnotation(cls: Class<*>): Optional<Field> = FastMethodInvoker.getFieldByAnnotation(this.javaClass, cls)

fun Any.getFieldsByAnnotation(cls: Class<*>): List<Field> = FastMethodInvoker.getFieldsByAnnotation(this.javaClass, cls)

fun Any.setMapValues(map: Map<String, Any>) = FastMethodInvoker.setMapValues(this, map)

inline fun <reified T> Any.copyTo(target: T): T = FastMethodInvoker.copy(this, target)

inline fun <reified T> Any.copyFrom(source: T): T = FastMethodInvoker.copy(source as Any, this) as T

inline fun <reified T> Any.copyNotNull(target: T): T = FastMethodInvoker.copyNotNull(this, target)

inline fun <reified T> Any.copyToNull(target: T): T = FastMethodInvoker.copyIsNull(this, target)

fun Any.has(name: String): Boolean = FastMethodInvoker.has(this, name)

fun Any.has(name: Enum<*>): Boolean = FastMethodInvoker.has(this, name.name)

fun Class<*>.has(name: String): Boolean = FastMethodInvoker.has(this, name)

fun Class<*>.has(name: Enum<*>): Boolean = FastMethodInvoker.has(this, name.name)

fun Any.getCachedField(name: Enum<*>): Field? = FastMethodInvoker.getField(this.javaClass, name.name)

fun Any.getCachedField(name: String): Field? = FastMethodInvoker.getField(this.javaClass, name)

fun Any.getFields(name: Enum<*>, vararg annotations: Class<*>): Set<Field> = FastMethodInvoker.getFields(this.javaClass, name.name, *annotations)

fun Any.getFields(name: String, vararg annotations: Class<*>): Set<Field> = FastMethodInvoker.getFields(this.javaClass, name, *annotations)

fun Any.getFields(vararg annotations: Class<*>): Set<Field> =
    if (ObjectUtils.isNotEmpty(annotations))
        FastMethodInvoker.getFields(this.javaClass, StringUtils.EMPTY, *annotations)
    else
        HashSet(FastMethodInvoker.reflectionStorage((this.javaClass)))

fun Class<*>.findClasses(): List<Class<*>> = FastMethodInvoker.findClasses(this.packageName)
    .filter { !it.isSingleton }.map { Class.forName(it.beanClassName) }

inline fun <reified T> Any.clone(vararg sources: Any): T = FastMethodInvoker.clone(this, *sources) as T