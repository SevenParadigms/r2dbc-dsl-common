package org.sevenparadigms.kotlin.common

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import org.apache.commons.beanutils.ConvertUtils
import org.springframework.data.r2dbc.support.JsonUtils

fun Any.toJsonNode(): JsonNode = JsonUtils.objectToJson(this)

fun <T> String.parseJson(cls: Class<T>): T = JsonUtils.stringToObject(this, cls)

fun <T> JsonNode.parseJson(cls: Class<T>): T = JsonUtils.jsonToObject(this, cls)

fun <T> JsonNode.parseArray(cls: Class<T>): List<T> = JsonUtils.jsonToObjectList(this, cls)

fun String?.toArrayNode(): ArrayNode = if (this == null)
    JsonUtils.arrayNode()
else
    JsonUtils.getMapper().readTree(this) as ArrayNode

fun <T> ArrayNode.contains(value: T): Boolean {
    for (node in this)
        if (node.asText().equals(ConvertUtils.convert(value, String::class.java).toString()))
            return true
    return false
}

fun JsonNode.singleQuotes(): String = this.toString().replace("\"", "'")