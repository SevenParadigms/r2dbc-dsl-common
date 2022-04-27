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
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

internal class JsonUtilsTest {

    @Test
    fun `should return JSON from map`() {
        val map: MutableMap<String, Any> = HashMap()
        map["a"] = 1
        val jsonNode = JsonUtils.mapToJson(map)
        assertThat(jsonNode, notNullValue())
        assertThat(jsonNode.javaClass, equalTo(ObjectNode::class.java))
        assertThat(jsonNode.size(), `is`(1))
    }

    @Test
    fun `should return map from JSON`() {
        val objectMapper = ObjectMapper()
        val node: JsonNode = objectMapper.createObjectNode()
        (node as ObjectNode).put("id", 5)
        node.put("name", "Slava")
        val map = JsonUtils.jsonToMap(node)
        assertThat(map, notNullValue())
        assertThat(map, hasKey("id"))
        assertThat(map, hasValue(5))
        assertThat(map, hasEntry("id", 5))
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
        (source1).put("name", "Slava")
        (source2 as ObjectNode).put("id", 2)
        (source2).put("name", "Vasya")
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
        class User(var age: Int, var name: String)

        val objectMapper = ObjectMapper()
        val user = User(4, "Slava")
        val map = objectMapper.convertValue(user, MutableMap::class.java)
        assertThat(map, notNullValue())
        assertThat(map.containsKey("age"), equalTo(true))
        assertThat(map.containsValue("Slava"), equalTo(true))
    }

    @Test
    @Throws(NoSuchFieldException::class)
    fun `should return object from map`() {
        class User {
            var age = 0
            var name: String? = null

            constructor() {}
            constructor(age: Int, name: String?) {
                this.age = age
                this.name = name
            }
        }

        val objectMapper = ObjectMapper()
        val map: MutableMap<String, Any> = HashMap()
        map["age"] = 1
        map["name"] = "Slava"
        val user = User()
        val userR = objectMapper.convertValue(map, user.javaClass)
        assertThat(userR, notNullValue())
        assertThat(userR.age, equalTo(1))
        assertThat(userR.name, equalTo("Slava"))
    }

    @Test
    fun `should return object from Json`() {
        val objectMapper = ObjectMapper()
        val source1: JsonNode = objectMapper.createObjectNode()
        (source1 as ObjectNode).put("id", 5)
        source1.put("name", "Slava")
        val result = JsonUtils.jsonToObject(source1, Any::class.java)
        assertThat(result, notNullValue())
        assertThat(result is Map<*, *>, `is`(true))
    }

    @Test
    fun `should return object from String`() {
        val s = "some text"
        assertThrows(IllegalArgumentException::class.java) {
            JsonUtils.stringToObject(s, MutableMap::class.java)
        }
    }

    @Test
    fun `should return ObjectList from JsonNode`() {
        class User {
            var id = 0
            var age = 0
            var name: String? = null

            constructor() {}
            constructor(id: Int, name: String?, age: Int) {
                this.id = id
                this.age = age
                this.name = name
            }
        }

        val objectMapper = ObjectMapper()
        val source1 = objectMapper.createObjectNode()
        source1.put("id", 1)
        source1.put("name", "Vasya")
        source1.put("age", 5)
        val arrayLists = JsonUtils.jsonToObjectList(source1, User::class.java)
        assertThat(arrayLists[0].id, `is`(1))
        assertThat(arrayLists[0].name, `is`("Vasya"))
        assertThat(arrayLists[0].age, `is`(5))
    }

    @Test
    fun `should return ObjectNode`() {
        val objectNode = JsonUtils.objectNode()
        assertThat(objectNode, notNullValue())
        assertThat(objectNode.javaClass, equalTo(ObjectNode::class.java))
    }

    @Test
    fun `should return ArrayNode`() {
        val arrayNode = JsonUtils.arrayNode()
        assertThat(arrayNode, notNullValue())
        assertThat(arrayNode.javaClass, equalTo(ArrayNode::class.java))
    }
}