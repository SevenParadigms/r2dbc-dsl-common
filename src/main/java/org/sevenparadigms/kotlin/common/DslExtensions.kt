package org.sevenparadigms.kotlin.common

import org.springframework.data.r2dbc.repository.query.Dsl
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZonedDateTime
import java.util.*

fun Dsl.order(name: Enum<*>, ascDesc: Enum<*>): Dsl = order(name.name, ascDesc)

fun Dsl.fields(vararg fields: Enum<*>): Dsl = fields(fields.map { it.name }.toList())

fun Dsl.`in`(name: Enum<*>, vararg fields: UUID): Dsl = `in`(name.name, fields.toList())

fun Dsl.notIn(name: Enum<*>, vararg fields: UUID): Dsl = notIn(name.name, fields.toList())

fun Dsl.equals(name: Enum<*>, value: String): Dsl = equals(name.name, value)

fun Dsl.equals(name: Enum<*>, value: UUID): Dsl = equals(name.name, value)

fun Dsl.equals(name: Enum<*>, value: Number): Dsl = equals(name.name, value)

fun Dsl.equals(name: Enum<*>, value: Any): Dsl = equals(name.name, value)

fun Dsl.isTrue(name: Enum<*>): Dsl = isTrue(name.name)

fun Dsl.isFalse(name: Enum<*>): Dsl = isFalse(name.name)

fun Dsl.notEquals(name: Enum<*>, value: String): Dsl = notEquals(name.name, value)

fun Dsl.notEquals(name: Enum<*>, value: UUID): Dsl = notEquals(name.name, value)

fun Dsl.notEquals(name: Enum<*>, value: Number): Dsl = notEquals(name.name, value)

fun Dsl.notEquals(name: Enum<*>, value: Any): Dsl = notEquals(name.name, value)

fun Dsl.greaterThan(name: Enum<*>, value: Number): Dsl = greaterThan(name.name, value)

fun Dsl.greaterThanOrEquals(name: Enum<*>, value: Number): Dsl = greaterThanOrEquals(name.name, value)

fun Dsl.lessThan(name: Enum<*>, value: Number): Dsl = lessThan(name.name, value)

fun Dsl.lessThanOrEquals(name: Enum<*>, value: Number): Dsl = lessThanOrEquals(name.name, value)

fun Dsl.greaterThan(name: Enum<*>, value: LocalDate): Dsl = greaterThan(name.name, value)

fun Dsl.greaterThanOrEquals(name: Enum<*>, value: LocalDate): Dsl = greaterThanOrEquals(name.name, value)

fun Dsl.lessThan(name: Enum<*>, value: LocalDate): Dsl = lessThan(name.name, value)

fun Dsl.lessThanOrEquals(name: Enum<*>, value: LocalDate): Dsl = lessThanOrEquals(name.name, value)

fun Dsl.greaterThan(name: Enum<*>, value: LocalDateTime): Dsl = greaterThan(name.name, value)

fun Dsl.greaterThanOrEquals(name: Enum<*>, value: LocalDateTime): Dsl = greaterThanOrEquals(name.name, value)

fun Dsl.lessThan(name: Enum<*>, value: LocalDateTime): Dsl = lessThan(name.name, value)

fun Dsl.lessThanOrEquals(name: Enum<*>, value: LocalDateTime): Dsl = lessThanOrEquals(name.name, value)

fun Dsl.greaterThan(name: Enum<*>, value: ZonedDateTime): Dsl = greaterThan(name.name, value)

fun Dsl.greaterThanOrEquals(name: Enum<*>, value: ZonedDateTime): Dsl = greaterThanOrEquals(name.name, value)

fun Dsl.lessThan(name: Enum<*>, value: ZonedDateTime): Dsl = lessThan(name.name, value)

fun Dsl.lessThanOrEquals(name: Enum<*>, value: ZonedDateTime): Dsl = lessThanOrEquals(name.name, value)

fun Dsl.greaterThan(name: Enum<*>, value: OffsetDateTime): Dsl = greaterThan(name.name, value)

fun Dsl.greaterThanOrEquals(name: Enum<*>, value: OffsetDateTime): Dsl = greaterThanOrEquals(name.name, value)

fun Dsl.lessThan(name: Enum<*>, value: OffsetDateTime): Dsl = lessThan(name.name, value)

fun Dsl.lessThanOrEquals(name: Enum<*>, value: OffsetDateTime): Dsl = lessThanOrEquals(name.name, value)

fun Dsl.isNull(name: Enum<*>): Dsl = isNull(name.name)

fun Dsl.isNotNull(name: Enum<*>): Dsl = isNotNull(name.name)

fun Dsl.like(name: Enum<*>, value: String): Dsl = like(name.name, value)

fun Dsl.fts(name: Enum<*>, value: String): Dsl = fts(name.name, value)

fun Dsl.lang(name: Enum<*>): Dsl = lang(name.name)

fun Dsl.get(name: Enum<*>): String? = criteriaMap[name.name]

fun Dsl.get(name: String): String? = criteriaMap[name]