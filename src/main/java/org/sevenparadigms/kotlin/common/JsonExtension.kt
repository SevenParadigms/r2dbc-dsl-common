package org.sevenparadigms.kotlin.common

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.commons.beanutils.ConvertUtils
import org.springframework.data.r2dbc.support.JsonUtils

fun Any.objectToJson(): JsonNode = JsonUtils.objectToJson(this)

inline fun <reified T> String.parseJson(cls: Class<T>): T = JsonUtils.stringToObject(this, cls)

inline fun <reified T> JsonNode.jsonToObject(cls: Class<T>): T = JsonUtils.jsonToObject(this, cls)

inline fun <reified T> JsonNode.jsonToObjectList(cls: Class<T>): List<T> = JsonUtils.jsonToObjectList(this, cls)

fun String.toArrayNode(): ArrayNode = JsonUtils.getMapper().readTree(this) as ArrayNode

inline fun <reified T> ArrayNode.has(value: T): Boolean = this.any { it.asText().equals(ConvertUtils.convert(value).toString()) }

fun JsonNode.singleQuotes(): String = this.toString().replace("\"", "'")

fun JsonNode.put(key: String, value: String): JsonNode = (this as ObjectNode).put(key, value)

fun JsonNode.put(key: String, value: Int): JsonNode = (this as ObjectNode).put(key, value)

fun JsonNode.put(key: String, value: Boolean): JsonNode = (this as ObjectNode).put(key, value)

fun JsonNode.put(key: String, value: Long): JsonNode = (this as ObjectNode).put(key, value)

fun JsonNode.put(key: Enum<*>, value: String): JsonNode = (this as ObjectNode).put(key.name, value)

fun JsonNode.put(key: Enum<*>, value: Int): JsonNode = (this as ObjectNode).put(key.name, value)

fun JsonNode.put(key: Enum<*>, value: Boolean): JsonNode = (this as ObjectNode).put(key.name, value)

fun JsonNode.put(key: Enum<*>, value: Long): JsonNode = (this as ObjectNode).put(key.name, value)

fun JsonNode.get(key: Enum<*>): JsonNode = (this as ObjectNode).get(key.name)

fun JsonNode.remove(key: Enum<*>): JsonNode = (this as ObjectNode).remove(key.name)

fun JsonNode.remove(key: String): JsonNode = (this as ObjectNode).remove(key)

fun JsonNode.has(key: Enum<*>): Boolean = !isNull && !isEmpty && has(key.name)

fun JsonNode.copyTo(target: JsonNode): JsonNode = JsonUtils.copy(this, target)

fun ObjectNode.put(key: Enum<*>, value: String): JsonNode = this.put(key.name, value)

fun ObjectNode.put(key: Enum<*>, value: Int): JsonNode = this.put(key.name, value)

fun ObjectNode.put(key: Enum<*>, value: Boolean): JsonNode = this.put(key.name, value)

fun ObjectNode.put(key: Enum<*>, value: Long): JsonNode = this.put(key.name, value)

fun ObjectNode.has(key: Enum<*>): Boolean = !isNull && !isEmpty && has(key.name)

fun ObjectNode.get(key: Enum<*>): JsonNode = this.get(key.name)