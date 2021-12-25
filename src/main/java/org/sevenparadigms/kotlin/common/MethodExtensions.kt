package org.sevenparadigms.kotlin.common

import org.springframework.data.r2dbc.support.FastMethodInvoker
import java.lang.reflect.Field
import java.util.*

fun Any.objectToMap(): Map<String, Any?> =
    FastMethodInvoker.reflectionStorage(this.javaClass).associate { it.name to FastMethodInvoker.getValue(this, it.name) }

fun Collection<Any>.objectsToMap(keyName: String, valueName: String): Map<String, Any?> =
    this.associate { it.getValue(keyName) as String to it.getValue(valueName) as Any }

fun Any.setValue(name: String, value: Any) = FastMethodInvoker.setValue(this, name, value)

fun Any.getValue(name: String): Any? = FastMethodInvoker.getValue(this, name)

fun Any.setValue(name: Enum<*>, value: Any) = FastMethodInvoker.setValue(this, name.name, value)

fun Any.getValue(name: Enum<*>): Any? = FastMethodInvoker.getValue(this, name.name)

fun <T> String.stringToObject(cls: Class<T>): Any = FastMethodInvoker.stringToObject(this, cls)

fun Class<*>.getFieldByAnnotation(cls: Class<*>): Optional<Field> = FastMethodInvoker.getFieldByAnnotation(this, cls)

fun Class<*>.getFieldsByAnnotation(cls: Class<*>): List<Field> = FastMethodInvoker.getFieldsByAnnotation(this, cls)

fun Any.setMapValues(map: Map<String, Any>) = FastMethodInvoker.setMapValues(this, map)

fun <T> Any.copyTo(target: T): T = FastMethodInvoker.copy(this, target)

fun <T> Any.copyNotNullTo(target: T): T = FastMethodInvoker.copyNotNull(this, target)

fun Any.has(name: String): Boolean = FastMethodInvoker.has(this, name)

fun Any.has(name: Enum<*>): Boolean = FastMethodInvoker.has(this, name.name)

fun Class<*>.has(name: String): Boolean = FastMethodInvoker.has(this, name)

fun Class<*>.has(name: Enum<*>): Boolean = FastMethodInvoker.has(this, name.name)

fun Class<*>.getCachedField(name: Enum<*>): Field = FastMethodInvoker.getField(this, name.name)

fun Class<*>.getCachedField(name: String): Field = FastMethodInvoker.getField(this, name)

fun Class<*>.getFields(name: Enum<*>, vararg annotations: Class<*>): Set<Field> = FastMethodInvoker.getFields(this, name.name, *annotations)

fun Class<*>.getFields(name: String, vararg annotations: Class<*>): Set<Field> = FastMethodInvoker.getFields(this, name, *annotations)