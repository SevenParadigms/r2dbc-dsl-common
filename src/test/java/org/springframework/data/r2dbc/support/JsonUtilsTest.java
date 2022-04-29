package org.springframework.data.r2dbc.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class JsonUtilsTest {

    @Test
    void shouldReturnJsonFromMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("a", 1);
        var result = JsonUtils.mapToJson(map);

        assertThat(result, notNullValue());
        assertThat(result.getClass(), equalTo(ObjectNode.class));
        assertThat(result.size(), is(1));
    }

    @Test
    void shouldReturnMapFromJson() {
        ObjectNode node = new ObjectMapper().createObjectNode();
        node.put("id", 5);
        Map<String, ?> resultMap = JsonUtils.jsonToMap(node);

        assertThat(resultMap, notNullValue());
        assertThat(resultMap, hasKey("id"));
        assertThat(resultMap, hasValue(5));
        assertThat(resultMap, hasEntry("id", 5));
    }

    @Test
    void copyFieldsFromSourcesToTargetWithParameterOneSource() {
        ObjectNode source1 = new ObjectMapper().createObjectNode();
        ObjectNode target = new ObjectMapper().createObjectNode();
        source1.put("id", 5);
        source1.put("name", "Slava");
        JsonUtils.copy(source1, target);

        assertThat(target, notNullValue());
        assertThat(target.getClass(), equalTo(ObjectNode.class));
        assertThat(target.size(), is(2));
    }

    @Test
    void copyFieldsFromSourcesToTargetWithParameterVarargsSources() {
        ObjectNode source1 = new ObjectMapper().createObjectNode();
        ObjectNode source2 = new ObjectMapper().createObjectNode();
        ObjectNode target = new ObjectMapper().createObjectNode();
        source1.put("id", 5);
        source1.put("name", "Slava");
        source2.put("id", 2);
        source2.put("name", "Vasya");
        var jsonNodes = new JsonNode[2];
        jsonNodes[0] = source1;
        jsonNodes[1] = source2;
        JsonUtils.copy(target, jsonNodes);

        assertThat(target, notNullValue());
        assertThat(target.getClass(), equalTo(ObjectNode.class));
        assertThat(target.size(), is(2));
    }

    @Test
    void shouldReturnArrayListFromJsonNode() {
        JsonNode array = new ObjectMapper().createObjectNode().arrayNode();
        var result = JsonUtils.nodeToObject(array);

        assertThat(result, notNullValue());
        assertThat(result.getClass(), equalTo(ArrayList.class));
    }

    @Test
    void shouldReturnJsonWhenParameterIsString() {
        String parameter = "\"test parameter\"";
        var result = JsonUtils.objectToJson(parameter);

        assertThat(result, notNullValue());
        assertThat(result.getClass(), equalTo(TextNode.class));
    }

    @Test
    void shouldReturnJsonWhenParameterIsByteArray() {
        Byte[] parameter = new Byte[]{1, 2, 3, 4, 5};
        JsonNode result = JsonUtils.objectToJson(parameter);

        assertThat(result, notNullValue());
        assertThat(result.getClass(), equalTo(ArrayNode.class));
        assertThat(result.size(), is(5));
    }

    @Test
    void shouldReturnMapFromObject() {
        var user = new User(4, "test");
        var resultMap = JsonUtils.objectToMap(user);

        assertThat(resultMap, notNullValue());
        assertThat(resultMap.containsKey("id"), equalTo(true));
        assertThat(resultMap.containsValue("test"), equalTo(true));
    }

    @Test
    void shouldReturnObjectFromMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", 1);
        map.put("name", "test");
        User result = JsonUtils.mapToObject(map, User.class);

        assertThat(result, notNullValue());
        assertThat(result.getId(), equalTo(1));
        assertThat(result.getName(), equalTo("test"));
    }

    @Test
    void shouldReturnObjectFromJson() {
        ObjectNode source1 = new ObjectMapper().createObjectNode();
        source1.put("id", 5);
        source1.put("name", "test");
        var result = JsonUtils.jsonToObject(source1, Object.class);

        assertThat(result, notNullValue());
        assertThat(result instanceof Map, is(true));
    }

    @Test
    void shouldReturnObjectFromString() {
        var text = "123";
        var result = JsonUtils.stringToObject(text, Number.class);

        assertThat(result, is(123));
        assertThat(result.getClass().getSimpleName(), equalTo("Integer"));
    }

    @Test
    void shouldReturnObjectListFromJsonNode() {
        var source1 = new ObjectMapper().createObjectNode();
        source1.put("id", 1);
        source1.put("name", "test");
        source1.put("age", 5);
        var resultList = JsonUtils.jsonToObjectList(source1, User.class);

        assertThat(resultList.get(0).id, is(1));
        assertThat(resultList.get(0).name, is("test"));
        assertThat(resultList.get(0).age, is(5));
    }

    @Test
    void shouldReturnObjectNode() {
        var result = JsonUtils.objectNode();

        assertThat(result, notNullValue());
        assertThat(result.getClass(), equalTo(ObjectNode.class));
    }

    @Test
    void shouldReturnArrayNode() {
        var result = JsonUtils.arrayNode();

        assertThat(result, notNullValue());
        assertThat(result.getClass(), equalTo(ArrayNode.class));
    }

    static class User {
        int id;
        int age;
        String name;

        public User() {
        }

        public User(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setAge(int age) {
            this.age = age;
        }
    }
}