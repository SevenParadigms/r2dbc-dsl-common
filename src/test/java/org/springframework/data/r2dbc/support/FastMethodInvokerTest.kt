package org.springframework.data.r2dbc.support

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.notNullValue
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.greaterThan
import org.junit.jupiter.api.Test
import org.mockito.Spy
import org.springframework.beans.factory.annotation.Qualifier
import java.lang.reflect.Field
import java.math.BigInteger
import java.time.LocalDate

internal class FastMethodInvokerTest {

    @Test
    fun `reflection storage should return fields`() {
        val resultList = FastMethodInvoker.reflectionStorage(String::class.java)

        assertThat(resultList, notNullValue())
        assertThat(resultList.size, `is`(9))
        assertThat(resultList[0], `is`(String::class.java.getDeclaredField("value")))
    }

    @Test
    fun `method with parameter class instance should return true if class has field`() {
        val result = FastMethodInvoker.has(LocalDate.now(), "DAYS_0000_TO_1970")

        assertThat(result, `is`(true))
    }

    @Test
    fun `method with parameter class instance should return false if class not has field`() {
        val result = FastMethodInvoker.has(LocalDate.now(), "DAYS_0000_TO_1212")

        assertThat(result, `is`(false))
    }

    @Test
    fun `method with parameter is class type should return true if class has field`() {
        val result = FastMethodInvoker.has(LocalDate::class.java, "DAYS_0000_TO_1970")

        assertThat(result, `is`(true))
    }

    @Test
    fun `method with parameter is class type should return false if class not has field`() {
        val result = FastMethodInvoker.has(LocalDate::class.java, "DAYS_0000_TO_1133")

        assertThat(result, `is`(false))
    }

    @Test
    fun `should return field if class has this field`() {
        val result = FastMethodInvoker.getField(LocalDate::class.java, "DAYS_0000_TO_1970")

        assertThat(result, notNullValue())
        assertThat(result!!.name.contains("DAYS"), `is`(true))
    }

    @Test
    fun `method copy should return T target`() {
        val result = FastMethodInvoker.copy(User(4), User(2))

        assertThat(result.age, equalTo(4))
    }

    @Test
    fun `should return value of target field if source is null`() {
        val result = FastMethodInvoker.copyNotNull(0, User(3))

        assertThat(result.age, equalTo(3))
    }

    @Test
    fun `should return value of source field if target is null`() {
        class User1 {
            var age: Int? = null
        }

        val result = FastMethodInvoker.copyIsNull(User(1), User1())

        assertThat(result.age, equalTo(1))
    }

    @Test
    fun `should return map from object`() {
        val resultMap = FastMethodInvoker.objectToMap(LocalDate.now())

        assertThat(resultMap, notNullValue())
        assertThat(resultMap.javaClass, equalTo(HashMap::class.java))
        assertThat(resultMap.size, equalTo(2))
        assertThat(resultMap.containsKey("month"), `is`(true))
    }

    @Test
    fun `should return map from two objects`() {
        val collection: MutableCollection<Any?> = ArrayList()
        collection.add(LocalDate.now())
        collection.add(LocalDate.now().minusYears(1).minusMonths(2))
        val resultMap = FastMethodInvoker.objectsToMap(collection, "year", "month")

        assertThat(resultMap, notNullValue())
        assertThat(resultMap.javaClass, equalTo(HashMap::class.java))
        assertThat(resultMap.keys.size, greaterThan(0))
        assertThat(resultMap.values.contains(LocalDate.now().month), `is`(true))
    }

    @Test
    fun `should set value for user field with name equals age`() {
        val user = User(1)
        FastMethodInvoker.setValue(user, "age", "7")

        assertThat(user.age, equalTo(7))
    }

    @Test
    fun `should set values from map for user fields age and name`() {
        val user = User(1, "S")
        val map: MutableMap<String, Any?> = HashMap()
        map.put("age", 7)
        map.put("name", "test")
        FastMethodInvoker.setMapValues(user, map)

        assertThat(user.age, equalTo(7))
        assertThat(user.name, equalTo("test"))
    }

    @Test
    fun `should return object if fieldName equals name`() {
        val localDate = LocalDate.now()
        val result = FastMethodInvoker.getValue(localDate, "month")

        assertThat(result, notNullValue())
        assertThat(result, `is`(localDate.month))
    }

    @Test
    fun `getValue with parameter class return object if fieldName equals name`() {
        val localDate = LocalDate.now()
        val result: Any? = FastMethodInvoker.getValue(localDate, "month", LocalDate::class.java)

        assertThat(result, notNullValue())
        assertThat(result, `is`(localDate.month))
    }

    @Test
    fun `should return object if parameter is string`() {
        val result = FastMethodInvoker.stringToObject("test", String::class.java)

        assertThat(result, notNullValue())
        assertThat(result, equalTo("test"))
    }

    @Test
    fun `should return object if parameter is BigInteger`() {
        val result = FastMethodInvoker.stringToObject("1", BigInteger::class.java)

        assertThat(result, equalTo(BigInteger(1.toString())))
    }

    @Test
    @Throws(NoSuchFieldException::class)
    fun `should return fields by annotation`() {
        class User(@field:Qualifier var name: String, @field:Qualifier var age: Int)

        val user = User("test", 32)
        val resultList = FastMethodInvoker.getFieldsByAnnotation(user.javaClass, Qualifier::class.java)

        assertThat(resultList, notNullValue())
        assertThat(resultList[0].name, equalTo("name"))
        assertThat(resultList[1].name, equalTo("age"))
    }

    @Test
    fun `should return first field by annotation`() {
        class User(@field:Qualifier var name: String, @field:Qualifier var age: Int)

        val user = User("test", 32)
        val result = FastMethodInvoker.getFieldByAnnotation(user.javaClass, Qualifier::class.java)

        assertThat(result.get().name, equalTo("name"))
    }

    @Test
    fun `should return fields by name and annotation`() {
        class User(var id: Int, @field:Qualifier var name: String, @field:Spy var age: Int)

        val user = User(1, "test", 32)
        val fields = FastMethodInvoker.getFields(
            user.javaClass, "id", Qualifier::class.java, Spy::class.java
        )
        val resultList: List<Field> = ArrayList(fields)

        assertThat(fields, notNullValue())
        assertThat(fields.size, `is`(3))
        assertThat(resultList[0].name, equalTo("id"))
        assertThat(resultList[1].name, equalTo("name"))
        assertThat(resultList[2].name, equalTo("age"))
    }

    @Test
    fun `should return BeanDefinitions if parameter is string`() {
        val resultSet = FastMethodInvoker.findClasses("org.springframework.data.r2dbc.support")

        assertThat(resultSet.toString().contains("FastMethodInvoker"), `is`(true))
        assertThat(resultSet.toString().contains("JsonUtils"), `is`(true))
        assertThat(resultSet.toString().contains("WordUtils"), `is`(true))
    }

    @Test
    fun `should return BeanDefinitions if parameter is class type`() {
        val resultSet = FastMethodInvoker.findClasses(FastMethodInvoker::class.java)

        assertThat(resultSet.toString().contains("JsonUtils"), `is`(true))
        assertThat(resultSet.toString().contains("WordUtils"), `is`(true))
    }

    @Test
    fun `method clone should return T target from sources`() {
        val user = User(10, "ABC")
        val user2 = User(101, "ABC1")
        val result = FastMethodInvoker.clone(user, user2)

        assertThat(result.age, equalTo(user2.age))
        assertThat(result.name, equalTo(user2.name))

    }

    internal class User {
        var id = 0
        var name: String? = null
        var age = 0

        constructor() {}
        constructor(age: Int) {
            this.age = age
        }

        constructor(id: Int, name: String?) {
            this.id = id
            this.name = name
        }
    }

}