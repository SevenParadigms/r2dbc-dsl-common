package org.sevenparadigms.kotlin.common

import org.slf4j.LoggerFactory.getLogger

fun Any.info(message: String, vararg args: Any) = getLogger(javaClass).info(message, args)

fun Any.warn(message: String, vararg args: Any) = getLogger(javaClass).warn(message, args)

fun Any.error(message: String, vararg args: Any) = getLogger(javaClass).error(message, args)

fun Any.debug(message: String, vararg args: Any) = getLogger(javaClass).debug(message, args)