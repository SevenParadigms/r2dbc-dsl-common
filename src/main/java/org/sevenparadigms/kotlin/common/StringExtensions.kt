package org.sevenparadigms.kotlin.common

import org.springframework.data.r2dbc.support.WordUtils

fun String.remove(regex: String) = this.replace(regex.toRegex(), "")

fun String.camelToSql(camel: String) = WordUtils.camelToSql(camel)

fun String.sqlToCamel(sqlName: String) = WordUtils.sqlToCamel(sqlName)