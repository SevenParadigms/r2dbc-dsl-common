package org.springframework.data.r2dbc.support;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class WordUtilsTest {

    @Test
    void shouldReturnCamelFromSql() {
        String sqlName = "first_column_name";
        var result = WordUtils.sqlToCamel(sqlName);

        assertNotNull(result);
        assertEquals(result, "firstColumnName");
    }

    @Test
    void shouldReturnSqlFromCamel() {
        String camel = "firstColumnName";
        var result = WordUtils.camelToSql(camel);

        assertNotNull(result);
        assertEquals(result, "first_column_name");
    }

    @Test
    void shouldTrimTextAndReturnOneLine() {
        var text = "Test text" + " \n" + "\n" + "!!!";
        var result = WordUtils.trimInline(text);

        assertNotNull(result);
        assertEquals(result, "Test text !!!");
    }

    @Test
    void shouldGenerateRandomStringWithLengthEqualParameter() {
        var result = WordUtils.generateString(5);

        assertNotNull(result);
        assertThat(result.length(),
                allOf(greaterThan(4), lessThanOrEqualTo(7), not(equalTo(6))));
    }

    @Test
    void shouldReturnLastPartAfterDot() {
        String fieldName = "first.Field";
        String result = WordUtils.lastOctet(fieldName);

        assertThat(result, notNullValue());
        assertThat(result, equalTo("Field"));
    }

    @Test
    void shouldRemovePartAfterFirstTemplate() {
        String source = "Ivan, Petr, Anna";
        String template = ",";
        String result = WordUtils.removeAfter(source, template);

        assertThat(result, notNullValue());
        assertThat(result, equalTo("Ivan"));
    }

    @Test
    void shouldTransferDateTimeToString() {
        var dateTimeToString = WordUtils.dateTimeToString("2022-04-24T11:50:35.131963Z + [qqq]");

        assertThat(dateTimeToString, equalTo("2022-04-24T11:50:35.131963 "));
    }
}