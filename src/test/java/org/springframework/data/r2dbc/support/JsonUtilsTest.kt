package org.springframework.data.r2dbc.support

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.util.*

internal class JsonUtilsTest {
    var objectMapper = ObjectMapper()
    var node: JsonNode = objectMapper.createObjectNode()
    var source1: JsonNode = objectMapper.createObjectNode()
    var source2: JsonNode = objectMapper.createObjectNode()
    var target: JsonNode = objectMapper.createObjectNode()
    var array: JsonNode = objectMapper.createObjectNode().arrayNode()

    @Test
    fun mapToJson() {
        val map: MutableMap<String, Any> = HashMap()
        map.put("a", 1)
        val jsonNode = JsonUtils.mapToJson(map)
        assertThat(jsonNode, notNullValue())
        assertThat(jsonNode.javaClass, equalTo<Class<out JsonNode>>(ObjectNode::class.java))
        assertThat(jsonNode.size(), `is`(1))
    }

    @Test
    fun jsonToMap() {
        (node as ObjectNode).put("id", 5)
        (node as ObjectNode).put("name", "Slava")
        val map = JsonUtils.jsonToMap(node)
        assertThat(map, notNullValue())
        assertThat(map, hasKey("id"))
        assertThat(map, hasValue(5))
        assertThat(map, hasEntry("id", 5))
    }

    @Test
    fun copyWithParameterOneSource() {
        (source1 as ObjectNode).put("id", 5)
        (source1 as ObjectNode).put("name", "Slava")
        JsonUtils.copy(source1, target)
        assertThat(target, notNullValue())
        assertThat(target.javaClass, equalTo<Class<out JsonNode>>(ObjectNode::class.java))
        assertThat(target.size(), `is`(2))
    }

    @Test
    fun copyWithParameterVarargsSources() {
        (source1 as ObjectNode).put("id", 5)
        (source1 as ObjectNode).put("name", "Slava")
        (source2 as ObjectNode).put("id", 2)
        (source2 as ObjectNode).put("name", "Vasya")
        val jsonNodes = arrayOfNulls<JsonNode>(2)
        jsonNodes[0] = source1
        jsonNodes[1] = source2
        JsonUtils.copy(target, *jsonNodes)
        assertThat(target, notNullValue())
        assertThat(target.javaClass, equalTo<Class<out JsonNode>>(ObjectNode::class.java))
        assertThat(target.size(), `is`(2))
    }

    @Test
    fun nodeToObject() {
        val result = JsonUtils.nodeToObject(array)
        assertThat(result, notNullValue())
        assertThat(result.javaClass, equalTo<Class<out Any>>(ArrayList::class.java))
    }

    @Test
    fun objectToJsonWhenParameterIsString() {
        val parameter = "\"test parameter\""
        val result = JsonUtils.objectToJson(parameter)
        assertThat(result, notNullValue())
        assertThat(result.javaClass, equalTo<Class<out JsonNode>>(TextNode::class.java))
    }

    @Test
    fun objectToJsonWhenParameterIsByteArray() {
        val parameter = arrayOf<Byte>(1, 2, 3, 4, 5)
        val result = JsonUtils.objectToJson(parameter)
        assertThat(result, notNullValue())
        assertThat(result.javaClass, equalTo<Class<out JsonNode>>(ArrayNode::class.java))
        assertThat(result.size(), `is`(5))
    }

    @Test
    fun objectToJsonWhenParameterIsObject() {
        val parameter: Any = arrayOf<Byte>(1, 2, 3, 4, 5)
        val result = JsonUtils.objectToJson(parameter)
        assertThat(result, notNullValue())
        assertThat(result.javaClass, equalTo<Class<out JsonNode>>(ArrayNode::class.java))
        assertThat(result.size(), `is`(5))
    }

    @Test
    fun jsonToObject() {
        (source1 as ObjectNode).put("id", 5)
        (source1 as ObjectNode).put("name", "Slava")
        val result = JsonUtils.jsonToObject<Any>(source1, Any::class.java)
        assertThat(result, notNullValue())
        assertThat(result is Map<*, *>, `is`(true))
    }

    @Test
    fun stringToObject() {
        val s = "some text"
        assertThrows<IllegalArgumentException>(IllegalArgumentException::class.java) {
            JsonUtils.stringToObject<MutableMap<*, *>>(
                s,
                MutableMap::class.java
            )
        }
    }
}