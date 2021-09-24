package org.sevenparadigms.kotlin.common

import com.fasterxml.jackson.databind.JsonNode
import org.springframework.data.r2dbc.support.JsonUtils

fun Any.toJsonNode(): JsonNode = JsonUtils.objectToJson(this)

fun <T> String.parseJson(cls: Class<T>): T = JsonUtils.jsonToObject(this, cls)

fun <T> JsonNode.parseJson(cls: Class<T>): T = JsonUtils.jsonToObject(this, cls)

fun <T> JsonNode.toList(cls: Class<T>) = JsonUtils.jsonToList(this, cls)

fun <T> JsonNode.singleQuotes() = this.toString().replace("\"", "'")