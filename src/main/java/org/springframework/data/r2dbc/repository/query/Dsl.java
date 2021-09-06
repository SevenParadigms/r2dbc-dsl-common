package org.springframework.data.r2dbc.repository.query;

import org.springframework.data.r2dbc.support.FastMethodInvoker;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Dsl {
    public static final String idProperty = "id";

    public static Dsl create() {
        return new Dsl("");
    }

    public Dsl(final String query) {
        this.query = query;
    }

    public String query;
    public String lang = "english";
    public String[] fields = new String[0];
    public Integer page = -1;
    public Integer size = -1;
    public String sort = "";

    public String getQuery() {
        String decodedQuery = null;
        try {
            decodedQuery = URLDecoder.decode(query, UTF_8.displayName()).trim();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return decodedQuery;
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
        return !sort.isEmpty() && sort.contains(":");
    }

    public Dsl sorting(String field, String ascDesc) {
        if (!sort.isEmpty()) {
            sort += ",";
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

    public Dsl in(String field, Set<Long> ids) {
        if (field != null && !ids.isEmpty()) {
            query = start(query) + field + "##" + ids.stream().map(Object::toString).collect(Collectors.joining(" "));
        }
        return this;
    }

    public Dsl notIn(String field, Set<Long> ids) {
        if (field != null && !ids.isEmpty()) {
            query = start(query) + field + "!#" + ids.stream().map(Object::toString).collect(Collectors.joining(" "));
        }
        return this;
    }

    public Dsl id(String id) {
        Object object = FastMethodInvoker.convertObject(id);
        if (object instanceof UUID) return id((UUID) object);
        if (object instanceof Number) return id(((Number) object).longValue());
        return this;
    }

    public Dsl id(UUID id) {
        if (id != null) {
            query = start(query) + idProperty + "=='" + id + "'::uuid";
        }
        return this;
    }

    public Dsl id(Long id) {
        if (id != null) {
            query = start(query) + idProperty + "==" + id;
        }
        return this;
    }

    public Dsl isTrue(String field) {
        if (field != null) {
            query = start(query) + field;
        }
        return this;
    }

    public Dsl equals(String field, Object value) {
        if (field != null) {
            query = start(query) + field + "==";
            if (value instanceof Number) query += value;
            else query += value;
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

    private String start(String string) {
        if (string.trim().isEmpty())
            return "";
        else
            return string + ",";
    }

    public List<String> getResultFields() {
        if (fields.length > 0) {
            return Arrays.asList(fields);
        }
        return new ArrayList<>();
    }

    public void setResultFields(List<String> fields) {
        this.fields = fields.toArray(new String[0]);
    }
}