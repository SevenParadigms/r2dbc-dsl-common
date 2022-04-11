package org.sevenparadigms.kotlin.common

import org.springframework.data.r2dbc.expression.ExpressionParserCache
import org.springframework.expression.Expression

fun String.parseExpression(): Expression = ExpressionParserCache.INSTANCE.parseExpression(this)

fun Expression.getBoolean(context: Any): Boolean = this.getValue(context, Boolean::class.java)!!