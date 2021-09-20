package org.sevenparadigms.kotlin.beans

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@ComponentScan("org.sevenparadigms.kotlin.beans")
class BeansConfiguration {
    @Bean
    fun beans() = Beans()
}