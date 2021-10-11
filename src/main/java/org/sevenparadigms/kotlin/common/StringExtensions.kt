package org.sevenparadigms.kotlin.common

import org.springframework.data.r2dbc.support.WordUtils

fun String.remove(regex: String): String = this.replace(regex.toRegex(), "")

fun String.camelToSql(camel: String): String = WordUtils.camelToSql(camel)

fun String.sqlToCamel(sqlName: String): String = WordUtils.sqlToCamel(sqlName)