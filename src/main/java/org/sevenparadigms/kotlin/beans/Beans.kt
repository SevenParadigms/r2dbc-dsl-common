package org.sevenparadigms.kotlin.beans

import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.NoSuchBeanDefinitionException
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.context.support.GenericApplicationContext
import org.springframework.data.r2dbc.support.FastMethodInvoker
import org.springframework.util.ConcurrentReferenceHashMap
import java.lang.reflect.Constructor
import java.util.concurrent.Callable
import java.util.concurrent.ConcurrentMap
import java.util.function.Supplier

class Beans : ApplicationContextAware {
    override fun setApplicationContext(applicationContext: ApplicationContext) {
        Companion.applicationContext = applicationContext
    }

    companion object {
        private val OBJECTS_CACHE: ConcurrentMap<Class<*>, Any> = ConcurrentReferenceHashMap(720)
        private var applicationContext: ApplicationContext? = null

        @JvmStatic
        fun <T> of(beanType: Class<T>): T? = cache(beanType) {
            getApplicationContext()!!.getBean(beanType)
        }

        @JvmStatic
        fun <T> register(bean: T, vararg args: Any?) {
            val context = of(GenericApplicationContext::class.java)
            context?.registerBean(bean!!::class.java, args)
        }

        @JvmStatic
        fun <T> register(bean: T) {
            val context = of(GenericApplicationContext::class.java)
            context?.registerBean(bean!!::class.java, Supplier<T> { bean })
        }

        @JvmStatic
        fun getProperty(name: String, vararg defaultValue: String): String {
            return getApplicationContext()?.environment?.getProperty(name) ?: if (defaultValue.isNotEmpty())
                defaultValue.first()
            else
                StringUtils.EMPTY
        }

        @JvmStatic
        fun <T> getProperty(name: String, target: Class<T>, vararg defaultValue: T): T {
            return try {
                getApplicationContext()!!.environment.getProperty(name, target)!!
            } catch (e: Exception) {
                return if (defaultValue.isNotEmpty()) defaultValue.first()
                else
                    when (target) {
                        String::class.java -> StringUtils.EMPTY as T
                        Number::class.java -> -1 as T
                        Boolean::class.java -> false as T
                        else -> try {
                            target.getConstructor().newInstance()
                        } catch (e: java.lang.Exception) {
                            throw RuntimeException(e)
                        }
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

        @JvmStatic
        fun getApplicationContext() = applicationContext
    }
}