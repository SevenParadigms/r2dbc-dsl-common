package org.springframework.data.r2dbc.support

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

internal class WordUtilsTest {
    @Test
    fun sqlToCamel() {
        val sqlName = "first_column_name"
        val result = WordUtils.sqlToCamel(sqlName)
        assertNotNull(result)
        assertEquals(result, "firstColumnName")
    }

    @Test
    fun camelToSql() {
        val camel = "firstColumnName"
        val result = WordUtils.camelToSql(camel)
        assertNotNull(result)
        assertEquals(result, "first_column_name")
    }

    @Test
    fun trimInLine() {
        val text = "Test text" + " \n" + "\n" + "!!!"
        val result = WordUtils.trimInline(text)
        assertNotNull(result)
        assertEquals(result, "Test text !!!")
    }

    @Test
    fun generateString() {
        val result = WordUtils.generateString(5)
        assertNotNull(result)
        assertThat(
            result.length, allOf(greaterThan(4), lessThanOrEqualTo(7), not(equalTo(6)))
        )
    }

    @Test
    fun lastOctet() {
        val fieldName = "first.Field"
        val result = WordUtils.lastOctet(fieldName)
        assertThat(result, notNullValue())
        assertThat(result, equalTo("Field"))
    }

    @Test
    fun removeAfter() {
        val source = "Ivan, Petr, Anna"
        val template = ","
        val result = WordUtils.removeAfter(source, template)
        assertThat(result, notNullValue())
        assertThat(result, equalTo("Ivan"))
    }
}