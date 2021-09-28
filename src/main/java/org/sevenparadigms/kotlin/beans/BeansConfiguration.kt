package org.sevenparadigms.kotlin.beans

import com.fasterxml.jackson.databind.ObjectMapper
import io.r2dbc.spi.ConnectionFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.support.JsonUtils
import org.springframework.r2dbc.connection.R2dbcTransactionManager
import org.springframework.transaction.ReactiveTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement

@Configuration
@EnableTransactionManagement
@ComponentScan("org.sevenparadigms.kotlin.beans")
class BeansConfiguration {
    @Bean
    fun beans() = Beans()

    @Bean
    fun transactionManager(connectionFactory: ConnectionFactory) =
        R2dbcTransactionManager(connectionFactory) as ReactiveTransactionManager

    @Bean
    fun objectMapper(): ObjectMapper = JsonUtils.getMapper()
}