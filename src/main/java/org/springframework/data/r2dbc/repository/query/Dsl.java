package org.springframework.data.r2dbc.repository.query;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Model for web-querying and criteria dsl building
 *
 * @author Lao Tsing
 */
public class Dsl implements Serializable {
    public static final String idProperty = "id";
    public static final String comma = ",";
    public static final String space = " ";

    public static Dsl create() {
        return new Dsl(null, null, null, null, null, null);
    }

    public Dsl(final String query, final Integer page, final Integer size, final String sort, final String lang, final String fields) {
        this.query = query != null ? query : "";
        this.page = page != null ? page : -1;
        this.size = size != null ? size : -1;
        this.sort = sort != null ? sort : "";
        this.lang = lang != null ? lang : "";
        this.fields = fields != null ? fields.split(comma) : new String[0];
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

    public boolean isSorted() {
        return sort != null && !sort.isEmpty() && sort.contains(":");
    }

    public Dsl sorting(String field, String ascDesc) {
        if (!sort.isEmpty()) {
            sort += comma;
        }
        sort += (field + ":" + ascDesc);
        return this;
    }

    public Dsl fields(List<String> fields) {
        if (fields != null) {
            this.fields = fields.toArray(new String[0]);
        }
        return this;
    }

    public Dsl fields(String...fields) {
        if (fields != null) {
            this.fields = fields;
        }
        return this;
    }

    public Dsl in(String field, Long...ids) {
        if (field != null && ids != null && ids.length > 0) {
            query = start(query) + field + "##" + Stream.of(ids).map(Object::toString).collect(Collectors.joining(space));
        }
        return this;
    }

    public Dsl notIn(String field, Long...ids) {
        if (field != null  && ids != null && ids.length > 0) {
            query = start(query) + field + "!#" + Stream.of(ids).map(Object::toString).collect(Collectors.joining(space));
        }
        return this;
    }

    public Dsl id(UUID id) {
        return equals(idProperty, id);
    }

    public Dsl id(Long id) {
        return equals(idProperty, id);
    }

    public Dsl id(Integer id) {
        return equals(idProperty, id);
    }

    public Dsl equals(String field, Object value) {
        if (field != null) {
            query = start(query) + field + "==" + value;
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
            query = start(query) + "!" + field;
        }
        return this;
    }

    public Dsl notEquals(String field, Object value) {
        if (field != null) {
            query = start(query) + field + "!=" + value;
        }
        return this;
    }

    public Dsl greaterThan(String field, Long value) {
        if (field != null) {
            query = start(query) + field + ">>" + value;
        }
        return this;
    }

    public Dsl greaterThanOrEquals(String field, Long value) {
        if (field != null) {
            query = start(query) + field + ">=" + value;
        }
        return this;
    }

    public Dsl lessThan(String field, Long value) {
        if (field != null) {
            query = start(query) + field + "<<" + value;
        }
        return this;
    }

    public Dsl lessThanOrEquals(String field, Long value) {
        if (field != null) {
            query = start(query) + field + "<=" + value;
        }
        return this;
    }

    public Dsl isNull(String field) {
        query = start(query) + "@" + field;
        return this;
    }

    public Dsl isNotNull(String field) {
        query = start(query) + "!@" + field;
        return this;
    }

    public Dsl like(String field, String filter) {
        if (field != null && filter != null) {
            query = start(query) + field + "~~" + filter.trim();
        }
        return this;
    }

    public Dsl fts(String filter) {
        if (filter != null) {
            query = start(query) + "tsv@@" + filter.trim();
        }
        return this;
    }

    public Dsl fts(String field, String filter) {
        if (filter != null) {
            query = start(query) + field + "@@" + filter.trim();
        }
        return this;
    }

    private String start(String string) {
        if (string.trim().isEmpty())
            return "";
        else
            return string + comma;
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
}