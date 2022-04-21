package org.sevenparadigms.kotlin.common

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.util.ConcurrentReferenceHashMap
import java.util.concurrent.Callable
import java.util.logging.Level
import java.util.logging.Level.*

object LogExtensions {
    private val loggerCache = ConcurrentReferenceHashMap<Class<*>, Logger>()

    fun getLogger(cls: Class<*>, level: Level): Logger? {
        val logger = loggerCache.computeIfAbsent(cls) { LoggerFactory.getLogger(cls) }
        return when (level) {
            INFO -> if (logger.isInfoEnabled) logger else null
            WARNING -> if (logger.isWarnEnabled) logger else null
            SEVERE -> if (logger.isErrorEnabled) logger else null
            FINE -> if (logger.isDebugEnabled) logger else null
            FINEST -> if (logger.isTraceEnabled) logger else null
            else -> null
        }
    }
}

fun Any.info(message: String, vararg args: Any) = LogExtensions.getLogger(javaClass, INFO)?.info(message, args)

fun Any.warn(message: String, vararg args: Any) = LogExtensions.getLogger(javaClass, WARNING)?.warn(message, args)

fun Any.error(message: String, vararg args: Any) = LogExtensions.getLogger(javaClass, SEVERE)?.error(message, args)

fun Any.debug(message: String, vararg args: Any) = LogExtensions.getLogger(javaClass, FINE)?.debug(message, args)

fun Any.trace(message: String, vararg args: Any) = LogExtensions.getLogger(javaClass, FINEST)?.trace(message, args)

fun Any.isInfoEnabled() = LogExtensions.getLogger(javaClass, INFO) != null

fun Any.isWarnEnabled() = LogExtensions.getLogger(javaClass, WARNING) != null

fun Any.isErrorEnabled() = LogExtensions.getLogger(javaClass, SEVERE) != null

fun Any.isDebugEnabled() = LogExtensions.getLogger(javaClass, FINE) != null

fun Any.isTraceEnabled() = LogExtensions.getLogger(javaClass, FINEST) != null

fun Any.info(messageCallable: Callable<String>) = LogExtensions.getLogger(javaClass, INFO)?.also {
    it.info(messageCallable.call())
}

fun Any.warn(messageCallable: Callable<String>) = LogExtensions.getLogger(javaClass, WARNING)?.also {
    it.warn(messageCallable.call())
}

fun Any.error(messageCallable: Callable<String>) = LogExtensions.getLogger(javaClass, SEVERE)?.also {
    it.error(messageCallable.call())
}

fun Any.debug(messageCallable: Callable<String>) = LogExtensions.getLogger(javaClass, FINE)?.also {
    it.debug(messageCallable.call())
}

fun Any.trace(messageCallable: Callable<String>) = LogExtensions.getLogger(javaClass, FINEST)?.also {
    it.trace(messageCallable.call())
}