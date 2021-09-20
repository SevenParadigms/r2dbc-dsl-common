package org.sevenparadigms.kotlin.beans

import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.NoSuchBeanDefinitionException
import org.springframework.beans.factory.support.GenericBeanDefinition
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.util.ConcurrentReferenceHashMap
import org.springframework.web.context.support.GenericWebApplicationContext
import java.util.concurrent.Callable
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantReadWriteLock

class Beans : ApplicationContextAware {
    override fun setApplicationContext(applicationContext: ApplicationContext) {
        Companion.applicationContext = applicationContext
    }

    companion object {
        private val lock = ReentrantReadWriteLock();
        private val OBJECTS_CACHE: ConcurrentMap<Class<*>, Any> = ConcurrentReferenceHashMap(720)
        private var applicationContext: ApplicationContext? = null
        private const val awaitDelay = 50

        fun <T> of(beanType: Class<T>): T? = cache(beanType, Callable<T> {
            var bean: T? = getApplicationContext().getBean(beanType)
            val counter = AtomicInteger(awaitDelay)
            var isLocked = false
            if (applicationContext == null) {
                lock.readLock().lock()
                isLocked = true
            }
            try {
                while (bean == null && counter.getAndDecrement() > 0) {
                    TimeUnit.MILLISECONDS.sleep(100)
                    bean = getApplicationContext().getBean(beanType)
                }
            } finally {
                if (isLocked) {
                    lock.readLock().unlock()
                }
            }
            bean
        })

        fun <T> register(beanType: Class<T>, vararg args: Any?) {
            val context = of(GenericWebApplicationContext::class.java)!!
            val definition = GenericBeanDefinition()
            definition.setBeanClass(beanType)
            if (args.isNotEmpty()) {
                val chunkedList = args.asList().chunked(2)
                for (chunk in chunkedList) {
                    definition.propertyValues.add(chunk.first() as String, chunk.last())
                }
            }
            context.registerBeanDefinition(beanType.simpleName, definition)
        }

        fun getProperty(name: String, vararg defaultValue: String): String {
            lock.readLock().lock()
            try {
                return getApplicationContext().environment.getProperty(name) ?: if (defaultValue.isNotEmpty())
                    defaultValue.first()
                else
                    StringUtils.EMPTY
            } finally {
                lock.readLock().unlock()
            }
        }

        fun <T> getProperty(name: String, target: Class<T>, vararg defaultValue: T): T {
            lock.readLock().lock()
            return try {
                getApplicationContext().environment.getProperty(name, target)!!
            } finally {
                lock.readLock().unlock()
                if (defaultValue.isNotEmpty()) defaultValue.first()
                else
                    when (target) {
                        String::class.java -> StringUtils.EMPTY
                        Int::class.java -> 0
                        Boolean::class.java -> false
                    }

            }
        }

        private fun <T> cache(requiredType: Class<T>, callable: Callable<T>): T? {
            return OBJECTS_CACHE[requiredType] as T? ?: try {
                OBJECTS_CACHE[requiredType] = callable.call()
                OBJECTS_CACHE[requiredType] as T?
            } catch (e: NoSuchBeanDefinitionException) {
                throw RuntimeException(e)
            }
        }

        private fun getApplicationContext(): ApplicationContext {
            if (applicationContext == null) {
                val counter = AtomicInteger(awaitDelay * 2)
                lock.writeLock().lock()
                try {
                    while (applicationContext == null && counter.getAndDecrement() > 0) {
                        TimeUnit.MILLISECONDS.sleep(100)
                    }
                } finally {
                    lock.writeLock().unlock()
                }
                if (applicationContext == null) {
                    throw RuntimeException("Spring context not initialized")
                }
            }
            return applicationContext!!
        }
    }
}