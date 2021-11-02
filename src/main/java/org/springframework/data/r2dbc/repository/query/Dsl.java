package org.springframework.data.r2dbc.repository.query;

import org.apache.commons.beanutils.ConvertUtils;
import org.springframework.data.r2dbc.support.SQLInjectionSafe;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.SPACE;

/**
 * Model for web-querying and criteria dsl building
 *
 * @author Lao Tsing
 */
public class Dsl implements Serializable {
    public static final String idProperty = "id";
    public static final String tsvProperty = "tsv";
    public static final String COMMA = ",";
    public static final String COLON = ":";

    public static final String in = "##";
    public static final String notIn = "!#";
    public static final String not = "!";
    public static final String equal = "==";
    public static final String notEqual = "!=";
    public static final String greater = ">>";
    public static final String greaterEqual = ">=-";
    public static final String less = "<<";
    public static final String lessEqual = "<=";
    public static final String isNull = "@";
    public static final String notNull = "!@";
    public static final String like = "~~";
    public static final String fts = "@@";

    public static Dsl create() {
        return new Dsl(null, null, null, null, null, null);
    }

    public Dsl(final String query, final Integer page, final Integer size, final String sort, final String lang, final String fields) {
        this.query = query != null ? query : EMPTY;
        this.page = page != null ? page : -1;
        this.size = size != null ? size : -1;
        this.sort = sort != null ? sort : EMPTY;
        this.lang = lang != null ? lang : EMPTY;
        this.fields = fields != null ? fields.split(COMMA) : new String[0];
    }

    private String query;
    private String lang;
    private String[] fields;
    private Integer page;
    private Integer size;
    private String sort;

    public String getQuery() {
        String decodedQuery = null;
        if (query != null) {
            try {
                decodedQuery = URLDecoder.decode(query.trim(), UTF_8.displayName());
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
        return decodedQuery;
    }

    public String getLang() {
        return lang;
    }

    public String[] getFields() {
        return fields;
    }

    public Integer getPage() {
        return page;
    }

    public Integer getSize() {
        return size;
    }

    public String getSort() {
        return sort;
    }

    public boolean isPaged() {
        return page != -1 && size != -1;
    }

    public Dsl pageable(int page, int size) {
        this.page = page;
        this.size = size;
        return this;
    }

    public Dsl limit(int size) {
        this.size = size;
        return this;
    }

    public boolean isSorted() {
        return !sort.isEmpty() && sort.contains(COLON);
    }

    public Dsl sorting(String field, String ascDesc) {
        if (!sort.isEmpty()) {
            sort += COMMA;
        }
        sort += (field + COLON + ascDesc);
        return this;
    }

    public Dsl fields(List<String> fields) {
        if (fields != null) {
            this.fields = fields.toArray(new String[0]);
        }
        return this;
    }

    public Dsl fields(String... fields) {
        if (fields != null) {
            this.fields = fields;
        }
        return this;
    }

    public Dsl lang(String lang) {
        if (lang != null) {
            this.lang = lang;
        }
        return this;
    }

    public Dsl in(String field, UUID... ids) {
        if (field != null && ids.length > 0) {
            query = start(query) + field + in + Stream.of(ids).map(Object::toString).collect(Collectors.joining(SPACE));
        }
        return this;
    }

    public Dsl in(String field, Number... ids) {
        if (field != null && ids.length > 0) {
            query = start(query) + field + in + Stream.of(ids).map(it ->
                    (String) ConvertUtils.convert(it, String.class)).collect(Collectors.joining(SPACE));
        }
        return this;
    }

    public Dsl in(String field, String... ids) {
        if (field != null && ids.length > 0) {
            query = start(query) + field + in + String.join(SPACE, ids);
        }
        return this;
    }

    public Dsl notIn(String field, UUID... ids) {
        if (field != null && ids.length > 0) {
            query = start(query) + field + notIn + Stream.of(ids).map(Object::toString).collect(Collectors.joining(SPACE));
        }
        return this;
    }

    public Dsl notIn(String field, Number... ids) {
        if (field != null && ids.length > 0) {
            query = start(query) + field + notIn + Stream.of(ids).map(it ->
                    (String) ConvertUtils.convert(it, String.class)).collect(Collectors.joining(SPACE));
        }
        return this;
    }

    public Dsl notIn(String field, String... ids) {
        if (field != null && ids.length > 0) {
            query = start(query) + field + notIn + String.join(SPACE, ids);
        }
        return this;
    }

    public Dsl id(String id) {
        return equals(idProperty, id);
    }

    public Dsl id(UUID id) {
        return equals(idProperty, id);
    }

    public Dsl id(Number id) {
        return equals(idProperty, id);
    }

    public Dsl equals(String field, String value)  {
        return equals(field, (Object) value);
    }

    public Dsl equals(String field, UUID value)  {
        return equals(field, (Object) value);
    }

    public Dsl equals(String field, Number value)  {
        return equals(field, ConvertUtils.convert(value, String.class));
    }

    public Dsl equals(String field, Object value) {
        if (field != null && value != null) {
            query = start(query) + field + equal + value;
        }
        return this;
    }

    public Dsl isTrue(String field) {
        if (field != null) {
            query = start(query) + field;
        }
        return this;
    }

    public Dsl isFalse(String field) {
        if (field != null) {
            query = start(query) + not + field;
        }
        return this;
    }

    public Dsl notEquals(String field, String value)  {
        return notEquals(field, (Object) value);
    }

    public Dsl notEquals(String field, UUID value)  {
        return notEquals(field, (Object) value);
    }

    public Dsl notEquals(String field, Number value)  {
        return notEquals(field, ConvertUtils.convert(value, String.class));
    }

    public Dsl notEquals(String field, Object value) {
        if (field != null && value != null) {
            query = start(query) + field + notEqual + value;
        }
        return this;
    }

    public Dsl greaterThan(String field, Number value) {
        if (field != null && value != null) {
            query = start(query) + field + greater + value;
        }
        return this;
    }

    public Dsl greaterThanOrEquals(String field, Number value) {
        if (field != null && value != null) {
            query = start(query) + field + greaterEqual + value;
        }
        return this;
    }

    public Dsl lessThan(String field, Number value) {
        if (field != null && value != null) {
            query = start(query) + field + less + value;
        }
        return this;
    }

    public Dsl lessThanOrEquals(String field, Number value) {
        if (field != null && value != null) {
            query = start(query) + field + lessEqual + value;
        }
        return this;
    }

    public Dsl isNull(String field) {
        if (field != null) {
            query = start(query) + isNull + field;
        }
        return this;
    }

    public Dsl isNotNull(String field) {
        if (field != null) {
            query = start(query) + notNull + field;
        }
        return this;
    }

    public Dsl like(String field, String filter) {
        if (field != null && filter != null) {
            query = start(query) + field + like + filter.trim();
        }
        return this;
    }

    public Dsl fts(String filter) {
        return fts(tsvProperty, filter);
    }

    public Dsl fts(String field, String filter) {
        if (field != null && SQLInjectionSafe.throwElse(filter)) {
            query = start(query) + field + fts + filter.trim();
        }
        return this;
    }

    public List<String> getResultFields() {
        if (fields.length > 0) {
            return List.of(fields);
        }
        return new ArrayList<>();
    }

    public void setResultFields(List<String> fields) {
        this.fields = fields.toArray(new String[0]);
    }

    private String start(String string) {
        if (string.trim().isEmpty())
            return EMPTY;
        else
            return string + COMMA;
    }
}