package org.springframework.data.r2dbc.support

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.greaterThan
import org.hamcrest.Matchers.lessThanOrEqualTo
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

internal class WordUtilsTest {
    @Test
    fun `should return Camel from Sql`() {
        val sqlName = "first_column_name"
        val result = WordUtils.sqlToCamel(sqlName)
        assertNotNull(result)
        assertEquals(result, "firstColumnName")
    }

    @Test
    fun `should return Sql from Camel`() {
        val camel = "firstColumnName"
        val result = WordUtils.camelToSql(camel)
        assertNotNull(result)
        assertEquals(result, "first_column_name")
    }

    @Test
    fun `should trim text and return one line`() {
        val text = "Test text" + " \n" + "\n" + "!!!"
        val result = WordUtils.trimInline(text)
        assertNotNull(result)
        assertEquals(result, "Test text !!!")
    }

    @Test
    fun `should generate random string with length equal parameter`() {
        val result = WordUtils.generateString(5)
        assertNotNull(result)
        assertThat(
            result.length, allOf(greaterThan(4), lessThanOrEqualTo(7), not(equalTo(6)))
        )
    }

    @Test
    fun `should return last part after dot`() {
        val fieldName = "first.Field"
        val result = WordUtils.lastOctet(fieldName)
        assertThat(result, notNullValue())
        assertThat(result, equalTo("Field"))
    }

    @Test
    fun `should remove part after first template`() {
        val source = "Ivan, Petr, Anna"
        val template = ","
        val result = WordUtils.removeAfter(source, template)
        assertThat(result, notNullValue())
        assertThat(result, equalTo("Ivan"))
    }

    @Test
    fun `should transfer dateTime to string`() {
        val dateTimeToString = WordUtils.dateTimeToString("2022-04-24T11:50:35.131963Z + [qqq]")
        assertThat(dateTimeToString, equalTo("2022-04-24T11:50:35.131963 "))
    }
}