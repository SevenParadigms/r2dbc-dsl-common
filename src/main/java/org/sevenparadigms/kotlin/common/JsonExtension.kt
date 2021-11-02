package org.sevenparadigms.kotlin.common

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import org.apache.commons.beanutils.ConvertUtils
import org.springframework.data.r2dbc.support.JsonUtils

fun Any.toJsonNode(): JsonNode = JsonUtils.objectToJson(this)

fun <T> String.parseJson(cls: Class<T>): T = JsonUtils.stringToObject(this, cls)

fun <T> JsonNode.parseJson(cls: Class<T>): T = JsonUtils.jsonToObject(this, cls)

fun <T> JsonNode.parseArray(cls: Class<T>): List<T> = JsonUtils.jsonToObjectList(this, cls)

fun String.toArrayNode(): ArrayNode = JsonUtils.getMapper().readTree(this) as ArrayNode

fun <T> ArrayNode.contains(value: T): Boolean = this.any { it.asText().equals(ConvertUtils.convert(value, String::class.java).toString()) }

fun JsonNode.singleQuotes(): String = this.toString().replace("\"", "'")