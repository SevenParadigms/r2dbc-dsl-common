package org.springframework.data.r2dbc.support

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.notNullValue
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.hasKey
import org.hamcrest.Matchers.hasValue
import org.hamcrest.Matchers.hasEntry
import org.junit.jupiter.api.Test

internal class JsonUtilsTest {

    @Test
    fun `should return JSON from map`() {
        val map: MutableMap<String, Any> = HashMap()
        map["a"] = 1
        val result = JsonUtils.mapToJson(map)
        assertThat(result, notNullValue())
        assertThat(result.javaClass, equalTo(ObjectNode::class.java))
        assertThat(result.size(), `is`(1))
    }

    @Test
    fun `should return map from JSON`() {
        val objectMapper = ObjectMapper()
        val node: JsonNode = objectMapper.createObjectNode()
        (node as ObjectNode).put("id", 5)
        node.put("name", "Slava")
        val resultMap = JsonUtils.jsonToMap(node)
        assertThat(resultMap, notNullValue())
        assertThat(resultMap, hasKey("id"))
        assertThat(resultMap, hasValue(5))
        assertThat(resultMap, hasEntry("id", 5))
    }

    @Test
    fun `copy fields from sources to target with parameter one source`() {
        val objectMapper = ObjectMapper()
        val source1: JsonNode = objectMapper.createObjectNode()
        val target: JsonNode = objectMapper.createObjectNode()
        (source1 as ObjectNode).put("id", 5)
        source1.put("name", "Slava")
        JsonUtils.copy(source1, target)
        assertThat(target, notNullValue())
        assertThat(target.javaClass, equalTo(ObjectNode::class.java))
        assertThat(target.size(), `is`(2))
    }

    @Test
    fun `copy fields from sources to target with parameter varargs sources`() {
        val objectMapper = ObjectMapper()
        val source1: JsonNode = objectMapper.createObjectNode()
        val source2: JsonNode = objectMapper.createObjectNode()
        val target: JsonNode = objectMapper.createObjectNode()
        (source1 as ObjectNode).put("id", 5)
        source1.put("name", "Slava")
        (source2 as ObjectNode).put("id", 2)
        source2.put("name", "Vasya")
        val jsonNodes = arrayOfNulls<JsonNode>(2)
        jsonNodes[0] = source1
        jsonNodes[1] = source2
        JsonUtils.copy(target, *jsonNodes)
        assertThat(target, notNullValue())
        assertThat(target.javaClass, equalTo(ObjectNode::class.java))
        assertThat(target.size(), `is`(2))
    }

    @Test
    fun `should return ArrayList from JsonNode`() {
        val objectMapper = ObjectMapper()
        val array: JsonNode = objectMapper.createObjectNode().arrayNode()
        val result = JsonUtils.nodeToObject(array)
        assertThat(result, notNullValue())
        assertThat(result.javaClass, equalTo(ArrayList::class.java))
    }

    @Test
    fun `should return Json when parameter is string`() {
        val parameter = "\"test parameter\""
        val result = JsonUtils.objectToJson(parameter)
        assertThat(result, notNullValue())
        assertThat(result.javaClass, equalTo(TextNode::class.java))
    }

    @Test
    fun `should return Json when parameter is byte array`() {
        val parameter = arrayOf<Byte>(1, 2, 3, 4, 5)
        val result = JsonUtils.objectToJson(parameter)
        assertThat(result, notNullValue())
        assertThat(result.javaClass, equalTo(ArrayNode::class.java))
        assertThat(result.size(), `is`(5))
    }

    @Test
    fun `should return map from object`() {
        val objectMapper = ObjectMapper()
        val user = User(4, "test")
        val resultMap = objectMapper.convertValue(user, MutableMap::class.java)
        assertThat(resultMap, notNullValue())
        assertThat(resultMap.containsKey("age"), equalTo(true))
        assertThat(resultMap.containsValue("test"), equalTo(true))
    }

    @Test
    @Throws(NoSuchFieldException::class)
    fun `should return object from map`() {
        val map: MutableMap<String, Any> = HashMap()
        map["age"] = 1
        map["name"] = "test"
        val result = JsonUtils.mapToObject(map, User().javaClass)
        assertThat(result, notNullValue())
        assertThat(result.age, equalTo(1))
        assertThat(result.name, equalTo("test"))
    }

    @Test
    fun `should return object from Json`() {
        val source1: JsonNode = ObjectMapper().createObjectNode()
        (source1 as ObjectNode).put("id", 5)
        source1.put("name", "test")
        val result = JsonUtils.jsonToObject(source1, Any::class.java)
        assertThat(result, notNullValue())
        assertThat(result is Map<*, *>, `is`(true))
    }

    @Test
    fun `should return object from String`() {
        val text = "123"
        val result = JsonUtils.stringToObject(text, Number::class.java)
        assertThat(result, `is`(123))
        assertThat(result.javaClass.simpleName, equalTo("Integer"))

    }

    @Test
    fun `should return ObjectList from JsonNode`() {
        val source1 = ObjectMapper().createObjectNode()
        source1.put("id", 1)
        source1.put("name", "test")
        source1.put("age", 5)
        val resultList = JsonUtils.jsonToObjectList(source1, User::class.java)
        assertThat(resultList[0].id, `is`(1))
        assertThat(resultList[0].name, `is`("test"))
        assertThat(resultList[0].age, `is`(5))
    }

    @Test
    fun `should return ObjectNode`() {
        val result = JsonUtils.objectNode()
        assertThat(result, notNullValue())
        assertThat(result.javaClass, equalTo(ObjectNode::class.java))
    }

    @Test
    fun `should return ArrayNode`() {
        val result = JsonUtils.arrayNode()
        assertThat(result, notNullValue())
        assertThat(result.javaClass, equalTo(ArrayNode::class.java))
    }

    internal class User {
        var id = 0
        var name: String? = null
        var age = 0

        constructor() {}
        constructor(id: Int, name: String?) {
            this.id = id
            this.name = name
        }
    }
}