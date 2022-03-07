package org.sevenparadigms.kotlin.common

import com.fasterxml.jackson.databind.JsonNode
import org.apache.commons.lang3.ObjectUtils
import org.springframework.data.r2dbc.support.FastMethodInvoker
import org.springframework.data.r2dbc.support.JsonUtils
import java.util.concurrent.ConcurrentHashMap

fun JsonNode.jsonToMap(): Map<String, *> = JsonUtils.jsonToMap(this)

fun Map<String, *>.mapToJson(): JsonNode = JsonUtils.mapToJson(this)

fun Any.objectToMap(): Map<String, Any?> =
        FastMethodInvoker.reflectionStorage(this.javaClass).associate { it.name to FastMethodInvoker.getValue(this, it.name) }

fun Collection<Any>.objectsToMap(keyName: String, valueName: String): Map<String, Any?> =
        this.associate { it.getValue(keyName) as String to it.getValue(valueName) as Any }

fun Map<String, Any?>.putIfNotEmpty(name: String, value: Any?) =
        if (ObjectUtils.isNotEmpty(value)) (this as HashMap).put(name, value!!)
        else null

fun Map<String, Any?>.putIfNotEmpty(name: Enum<*>, value: Any?) = putIfNotEmpty(name.name, value)

fun ConcurrentHashMap<String, *>.clone() = ConcurrentHashMap(this)

fun HashMap<String, Any?>.add(name: Enum<*>, value: Any?) = put(name.name, value)