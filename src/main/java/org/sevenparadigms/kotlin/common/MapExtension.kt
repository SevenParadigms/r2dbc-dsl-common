package org.sevenparadigms.kotlin.common

import com.fasterxml.jackson.databind.JsonNode
import org.apache.commons.lang3.ObjectUtils
import org.springframework.data.r2dbc.support.FastMethodInvoker
import org.springframework.data.r2dbc.support.JsonUtils
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

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

fun Map<String, *>.copy(vararg target: Map<String, *>): HashMap<String, *> {
    val source = HashMap(this)
    if (ObjectUtils.isNotEmpty(target)) {
        for (map in target) {
            source.putAll(map)
        }
    }
    return source
}

fun ConcurrentMap<String, *>.copy(vararg sources: ConcurrentMap<String, *>): ConcurrentHashMap<String, *> {
    val source = ConcurrentHashMap(this)
    if (ObjectUtils.isNotEmpty(sources)) {
        for (map in sources) {
            source.putAll(map)
        }
    }
    return source
}

fun HashMap<String, Any?>.add(name: Enum<*>, value: Any?) = put(name.name, value)

fun ConcurrentHashMap<String, Any?>.add(name: Enum<*>, value: Any) = put(name.name, value)