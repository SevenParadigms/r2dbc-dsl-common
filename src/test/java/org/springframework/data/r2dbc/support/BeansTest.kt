package org.springframework.data.r2dbc.support

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import java.util.Optional

@SpringBootTest(classes = [Beans::class], properties = ["spring.main.allow-bean-definition-overriding=true"])
internal class BeansTest {

    @Autowired
    private val beans: Beans? = null

    @MockBean
    var jsonFactory: JsonFactory? = null

    @Test
    fun `should return bean from beanType`() {
        val result = Beans.of(beans!!.objectMapper().javaClass)

        assertThat(result, notNullValue())
        assertThat(result.javaClass.name, equalTo(ObjectMapper::class.java.name))
    }

    @Test
    fun `should return empty when parameter is null`() {
        class User(var age: Int, var name: String)

        val result: Optional<User> = Beans.getOrNull(User::class.java)

        assertThat(result, `is`(Optional.empty<Any>()))
    }

    @Test
    fun `should return bean when parameter is not null`() {
        val result: Optional<out ObjectMapper?> = Beans.getOrNull(beans!!.objectMapper().javaClass)

        assertThat(result.toString().contains("ObjectMapper"), `is`(true))
    }

    @Test
    fun `should put bean to cache and return this bean if parameter not null`() {
        val result = Beans.add(beans!!.objectMapper())

        assertThat(result, notNullValue())
        assertThat(result!!.javaClass.name, equalTo(ObjectMapper::class.java.name))
    }

    @Test
    fun `should register bean in context and return this bean`() {
        val result = Beans.register(beans!!.objectMapper())

        assertThat(result, notNullValue())
        assertThat(result.javaClass.typeName, equalTo(ObjectMapper::class.java.typeName))
    }

    @Test
    fun `should register bean in context and return this bean when second parameter is constructor args`() {
        Beans.register(ObjectMapper::class.java, jsonFactory)

        assert(Beans.getApplicationContext() != null)
        assertThat(Beans.getApplicationContext()!!.containsBean("objectMapper"), `is`(true))
    }

    @Test
    fun `should put object to cache and return bean`() {
        class User(var id: Int, var name: String)

        val user = User(1, "A")
        val result = Beans.putCache(user)

        assertThat(result, notNullValue())
        assertThat(result.javaClass.typeName, equalTo(User::class.java.typeName))
        assertThat(result.id, `is`(1))
        assertThat(result.name, `is`("A"))
    }
}