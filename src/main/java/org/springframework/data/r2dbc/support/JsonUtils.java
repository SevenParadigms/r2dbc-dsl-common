package org.springframework.data.r2dbc.support;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.ZonedDateTimeSerializer;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.r2dbc.expression.ExpressionDeserializer;
import org.springframework.data.r2dbc.expression.ExpressionSerializer;
import org.springframework.expression.Expression;
import org.springframework.lang.NonNull;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Utilities for json interaction.
 *
 * @author Lao Tsing
 */
public abstract class JsonUtils {
    private static final ObjectMapper OBJECT_MAPPER;

    static {
        var javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(ZonedDateTime.class, new ZonedDateTimeSerializer(DateTimeFormatter.ISO_ZONED_DATE_TIME));
        var expressionModule = new SimpleModule();
        expressionModule.addSerializer(Expression.class, new ExpressionSerializer());
        expressionModule.addDeserializer(Expression.class, new ExpressionDeserializer());
        OBJECT_MAPPER = new ObjectMapper()
                .registerModule(javaTimeModule)
                .registerModule(expressionModule)
                .registerModule(new Jdk8Module())
                .registerModule(new ParameterNamesModule())
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
                .configure(DeserializationFeature.FAIL_ON_MISSING_EXTERNAL_TYPE_ID_PROPERTY, false)
                .configure(DeserializationFeature.FAIL_ON_UNRESOLVED_OBJECT_IDS, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(DeserializationFeature.WRAP_EXCEPTIONS, false)
                .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
    }

    public static ObjectMapper getMapper() {
        return OBJECT_MAPPER;
    }

    public static JsonNode mapToJson(final Map<String, Object> map) {
        return getMapper().valueToTree(map);
    }

    @NonNull
    public static Map<String, ?> jsonToMap(@NonNull final JsonNode json) {
        var map = new LinkedHashMap<String, Object>();
        var fieldNames = json.fieldNames();
        while (fieldNames.hasNext()) {
            var fieldName = fieldNames.next();
            var jsonNode = json.get(fieldName);
            map.put(fieldName, nodeToObject(jsonNode));
        }
        return map;
    }

    public static JsonNode copy(@NonNull final JsonNode target, final JsonNode... sources) {
        if (ObjectUtils.isNotEmpty(sources)) {
            for (var source : sources) {
                copy(source, target);
            }
        }
        return target;
    }

    public static JsonNode copy(@NonNull final JsonNode source, final JsonNode target) {
        var fieldNames = source.fieldNames();
        while (fieldNames.hasNext()) {
            var fieldName = fieldNames.next();
            var jsonNode = source.get(fieldName);
            ((ObjectNode) target).replace(fieldName, jsonNode);
        }
        return target;
    }

    public static Object nodeToObject(@NonNull final JsonNode json) {
        var type = json.getNodeType();
        switch (type) {
            case ARRAY:
                var array = new ArrayList<>();
                for (JsonNode node : json) {
                    array.add(nodeToObject(node));
                }
                return array;
            case BINARY:
                try {
                    return json.binaryValue();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            case BOOLEAN:
                return json.booleanValue();
            case NULL:
                return null;
            case NUMBER:
                return json.numberValue();
            case OBJECT:
            case POJO:
                return jsonToMap(json);
            case STRING:
            default:
                return json.textValue();
        }
    }

    public static JsonNode objectToJson(final String string) {
        try {
            return getMapper().readTree(string);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static JsonNode objectToJson(final byte[] bytes) {
        try {
            return getMapper().readTree(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static JsonNode objectToJson(final Object object) {
        if (object instanceof String) return objectToJson((String) object);
        if (object instanceof byte[]) return objectToJson((byte[]) object);
        return getMapper().convertValue(object, JsonNode.class);
    }

    public static Map<String, Object> objectToMap(final Object object) {
        return getMapper().convertValue(object, Map.class);
    }

    public static <T> T mapToObject(final Map<String, Object> map, final Class<T> cls) {
        return getMapper().convertValue(map, cls);
    }

    public static <T> T jsonToObject(final JsonNode json, final Class<T> cls) {
        return getMapper().convertValue(json, cls);
    }

    public static <T> T stringToObject(final String json, final Class<T> cls) {
        try {
            return getMapper().readValue(json, getMapper().getTypeFactory().constructType(cls));
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot convert to object", e);
        }
    }

    @NonNull
    public static <T> ArrayList<T> jsonToObjectList(@NonNull final JsonNode json, final Class<T> cls) {
        var list = new ArrayList<T>();
        if (!json.isEmpty()) {
            var maps = JsonUtils.jsonToObject(json, ArrayList.class);
            for (Object map : maps) {
                try {
                    var constructor = cls.getConstructor();
                    var obj = constructor.newInstance();
                    FastMethodInvoker.setMapValues(obj, (Map<String, Object>) map);
                    list.add(obj);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return list;
    }

    public static ObjectNode objectNode() {
        return getMapper().createObjectNode();
    }

    public static ArrayNode arrayNode() {
        return getMapper().createArrayNode();
    }

    public static boolean isEmpty(final JsonNode json) {
        return json == null || json.isNull() || json.isEmpty();
    }
}
