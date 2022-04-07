package org.sevenparadigms.kotlin.common

import org.springframework.data.r2dbc.support.WordUtils

fun String.remove(regex: String): String = this.replace(regex.toRegex(), "")

fun String.removeBefore(template: String): String =
    if (this.contains(template)) this.substring(this.lastIndexOf(template) + template.length) else this

fun String.removeAfter(template: String): String =
    if (this.contains(template)) this.substring(0, this.indexOf(template)) else this

fun String.camelToSql(camel: String): String = WordUtils.camelToSql(camel)

fun String.sqlToCamel(sqlName: String): String = WordUtils.sqlToCamel(sqlName)

fun String.binding(regex: String, vararg arr: String): String {
    var i = 0
    var result = this
    Regex(regex).findAll(this).map { it.value }.forEach { result = result.replace(it, arr[i++]) }
    return result
}