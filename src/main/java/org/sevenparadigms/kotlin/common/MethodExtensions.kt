package org.sevenparadigms.kotlin.common

import org.springframework.data.r2dbc.support.FastMethodInvoker

fun Any.convertMap(): Map<String, Any?> =
    FastMethodInvoker.reflectionStorage(this.javaClass).associate { it.name to FastMethodInvoker.getValue(this, it.name) }

fun Collection<Any>.convertMap(keyName: String, valueName: String): Map<String, Any?> =
    this.associate { it.getValue(keyName) as String to it.getValue(valueName) as Any }

fun Any.setValue(name: String, value: Any) = FastMethodInvoker.setValue(this, name, value)

fun Any.getValue(name: String): Any? = FastMethodInvoker.getValue(this, name)