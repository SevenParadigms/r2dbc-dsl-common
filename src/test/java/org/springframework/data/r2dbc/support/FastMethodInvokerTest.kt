package org.springframework.data.r2dbc.support

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Test
import org.mockito.Spy
import org.springframework.beans.factory.annotation.Qualifier
import java.lang.reflect.Field
import java.math.BigInteger
import java.time.LocalDate

internal class FastMethodInvokerTest {
    @Test
    fun reflectionStorage() {
        val fields = FastMethodInvoker.reflectionStorage(String::class.java);
        assertThat(fields, notNullValue());
        assertThat(fields.size, `is`(9))
        assertThat(fields[0], `is`(String::class.java.getDeclaredField("value")))
    }

    @Test
    fun has() {
        val b = FastMethodInvoker.has(LocalDate.now(), "DAYS_0000_TO_1970")
        assertThat(b, `is`(true))
    }

    @Test
    fun hasWithParameterIsClass() {
        val b = FastMethodInvoker.has(LocalDate::class.java, "DAYS_0000_TO_1970")
        assertThat(b, `is`(true))
    }

    @Test
    fun getField() {
        val days = FastMethodInvoker.getField(LocalDate::class.java, "DAYS_0000_TO_1970")
        assertThat(days, notNullValue())
        assertThat(days!!.name.contains("DAYS"), `is`(true))
    }

    @Test
    fun objectToMap() {
        val abc = FastMethodInvoker.objectToMap(LocalDate.now())
        assertThat(abc, notNullValue())
        assertThat(
            abc.javaClass, equalTo<Class<out MutableMap<*, *>?>>(HashMap::class.java)
        )
        assertThat(abc.size, equalTo(2))
        assertThat(abc.containsKey("month"), `is`(true))
    }

    @Test
    fun objectsToMap() {
        val collection: MutableCollection<Any?> = ArrayList()
        collection.add(LocalDate.now())
        collection.add(LocalDate.now().minusYears(1).minusMonths(2))
        val map = FastMethodInvoker.objectsToMap(collection, "year", "month")
        assertThat(map, notNullValue())
        assertThat(map.javaClass, equalTo<Class<out MutableMap<*, *>?>>(HashMap::class.java))
        assertThat(map.keys.size, greaterThan(0))
        assertThat(map.values.contains(LocalDate.now().month), `is`(true))
    }

    @Test
    fun setValue() {
        class User(var age: Int)

        val user = User(1)
        FastMethodInvoker.setValue(user, "age", "7")
        assertThat(user.age, equalTo(7))
    }

    @Test
    fun setMapValues() {
        class User(var age: Int, var name: String)

        val user = User(1, "S")
        val map: MutableMap<String, Any?> = HashMap()
        map.put("age", 7)
        map.put("name", "Slava")
        FastMethodInvoker.setMapValues(user, map)
        assertThat(user.age, equalTo(7))
        assertThat(user.name, equalTo("Slava"))
    }

    @Test
    fun getValue() {
        val now = LocalDate.now()
        val month = FastMethodInvoker.getValue(now, "month")
        assertThat(month, notNullValue())
        assertThat(month, `is`(now.month))
    }

    @Test
    fun getValueWithParameterClass() {
        val now = LocalDate.now()
        val month: Any? = FastMethodInvoker.getValue<LocalDate>(now, "month", LocalDate::class.java)
        assertThat(month, notNullValue())
        assertThat(month, `is`(now.month))
    }

    @Test
    fun stringToObjectIfParameterIsString() {
        val name = FastMethodInvoker.stringToObject("qqqq", String::class.java)
        assertThat(name, notNullValue())
        assertThat(name, equalTo("qqqq"))
    }

    @Test
    fun stringToObjectIfParameterIsBigInteger() {
        val o = FastMethodInvoker.stringToObject("1", BigInteger::class.java)
        assertThat(o, equalTo(BigInteger(1.toString())))
    }

    @Test
    @Throws(NoSuchFieldException::class)
    fun getFieldsByAnnotation() {
        class User(@field:Qualifier var name: String, @field:Qualifier var age: Int)

        val user = User("Slava", 32)
        val fieldsByAnnotation = FastMethodInvoker.getFieldsByAnnotation(
            user.javaClass,
            Qualifier::class.java
        )
        assertThat(fieldsByAnnotation, notNullValue())
        assertThat(fieldsByAnnotation[0].name, equalTo("name"))
        assertThat(fieldsByAnnotation[1].name, equalTo("age"))
    }

    @Test
    fun getFieldByAnnotation() {
        class User(@field:Qualifier var name: String, @field:Qualifier var age: Int)

        val user = User("Slava", 32)
        val fieldByAnnotation = FastMethodInvoker.getFieldByAnnotation(
            user.javaClass,
            Qualifier::class.java
        )
        assertThat(fieldByAnnotation.get().name, equalTo("name"))
    }

    @Test
    fun getFields() {
        class User(var id: Int, @field:Qualifier var name: String, @field:Spy var age: Int)

        val user = User(1, "Slava", 32)
        val fields = FastMethodInvoker.getFields(
            user.javaClass, "id",
            Qualifier::class.java,
            Spy::class.java
        )
        val result: List<Field> = ArrayList(fields)
        assertThat(fields, notNullValue())
        assertThat(fields.size, `is`(3))
        assertThat(result[0].name, equalTo("name"))
        assertThat(result[1].name, equalTo("id"))
        assertThat(result[2].name, equalTo("age"))
    }


}