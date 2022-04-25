package org.springframework.data.r2dbc.support

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.notNullValue
import org.hamcrest.Matchers.`is`
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
        val fields = FastMethodInvoker.reflectionStorage(String::class.java)
        assertThat(fields, notNullValue())
        assertThat(fields.size, `is`(9))
        assertThat(fields[0], `is`(String::class.java.getDeclaredField("value")))
    }

    @Test
    fun `method with parameter class instance should return true if class has field`() {
        val b = FastMethodInvoker.has(LocalDate.now(), "DAYS_0000_TO_1970")
        assertThat(b, `is`(true))
    }

    @Test
    fun `method with parameter class instance should return false if class not has field`() {
        val b = FastMethodInvoker.has(LocalDate.now(), "DAYS_0000_TO_1212")
        assertThat(b, `is`(false))
    }

    @Test
    fun `method with parameter is class type should return true if class has field`() {
        val b = FastMethodInvoker.has(LocalDate::class.java, "DAYS_0000_TO_1970")
        assertThat(b, `is`(true))
    }

    @Test
    fun `method with parameter is class type should return false if class not has field`() {
        val b = FastMethodInvoker.has(LocalDate::class.java, "DAYS_0000_TO_1133")
        assertThat(b, `is`(false))
    }

    @Test
    fun `should return field if class has this field`() {
        val days = FastMethodInvoker.getField(LocalDate::class.java, "DAYS_0000_TO_1970")
        assertThat(days, notNullValue())
        assertThat(days!!.name.contains("DAYS"), `is`(true))
    }

    @Test
    fun `method copy should return T target`() {
        class User(var age: Int)

        val user = User(4)
        val user2 = User(2)
        val copy = FastMethodInvoker.copy(user, user2)
        assertThat(copy.age, equalTo(user.age))
    }

    @Test
    fun `should return value of target field if source is null`() {
        class User {
            var age = 0

            constructor() {}
            constructor(age: Int?) {
                if (age != null) {
                    this.age = age
                }
            }
        }

        val user2 = User(3)
        val copyNotNull = FastMethodInvoker.copyNotNull(0, user2)
        assertThat(copyNotNull.age, equalTo(3))
    }

    @Test
    fun `should return value of source field if target is null`() {
        class User {
            var age = 0

            constructor() {}
            constructor(age: Int) {
                this.age = age
            }
        }

        class User1 {
            var age: Int? = null
        }

        val user = User(1)
        val user2 = User1()
        val copyIsNull = FastMethodInvoker.copyIsNull(user, user2)
        assertThat(copyIsNull.age, equalTo(1))
    }

    @Test
    fun `should return map from object`() {
        val abc = FastMethodInvoker.objectToMap(LocalDate.now())
        assertThat(abc, notNullValue())
        assertThat(abc.javaClass, equalTo(HashMap::class.java))
        assertThat(abc.size, equalTo(2))
        assertThat(abc.containsKey("month"), `is`(true))
    }

    @Test
    fun `should return map from two objects`() {
        val collection: MutableCollection<Any?> = ArrayList()
        collection.add(LocalDate.now())
        collection.add(LocalDate.now().minusYears(1).minusMonths(2))
        val map = FastMethodInvoker.objectsToMap(collection, "year", "month")
        assertThat(map, notNullValue())
        assertThat(map.javaClass, equalTo(HashMap::class.java))
        assertThat(map.keys.size, greaterThan(0))
        assertThat(map.values.contains(LocalDate.now().month), `is`(true))
    }

    @Test
    fun `setValue should return success`() {
        class User(var age: Int)

        val user = User(1)
        FastMethodInvoker.setValue(user, "age", "7")
        assertThat(user.age, equalTo(7))
    }

    @Test
    fun `setMapValues should return success`() {
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
    fun `getValue return object if fieldName equals name`() {
        val now = LocalDate.now()
        val month = FastMethodInvoker.getValue(now, "month")
        assertThat(month, notNullValue())
        assertThat(month, `is`(now.month))
    }

    @Test
    fun `getValue with parameter class return object if fieldName equals name`() {
        val now = LocalDate.now()
        val month: Any? = FastMethodInvoker.getValue<LocalDate>(now, "month", LocalDate::class.java)
        assertThat(month, notNullValue())
        assertThat(month, `is`(now.month))
    }

    @Test
    fun `should return object if parameter is string`() {
        val name = FastMethodInvoker.stringToObject("qqqq", String::class.java)
        assertThat(name, notNullValue())
        assertThat(name, equalTo("qqqq"))
    }

    @Test
    fun `should return object if parameter is BigInteger`() {
        val o = FastMethodInvoker.stringToObject("1", BigInteger::class.java)
        assertThat(o, equalTo(BigInteger(1.toString())))
    }

    @Test
    @Throws(NoSuchFieldException::class)
    fun `should return fields by annotation`() {
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
    fun `should return first field by annotation`() {
        class User(@field:Qualifier var name: String, @field:Qualifier var age: Int)

        val user = User("Slava", 32)
        val fieldByAnnotation = FastMethodInvoker.getFieldByAnnotation(
            user.javaClass,
            Qualifier::class.java
        )
        assertThat(fieldByAnnotation.get().name, equalTo("name"))
    }

    @Test
    fun `should return fields by name and annotation`() {
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
        assertThat(result[0].name, equalTo("id"))
        assertThat(result[1].name, equalTo("name"))
        assertThat(result[2].name, equalTo("age"))
    }

    @Test
    fun `should return BeanDefinitions if parameter is string`() {
        val classes = FastMethodInvoker.findClasses("org.springframework.data.r2dbc.support")
        assertThat(classes.toString().contains("FastMethodInvoker"), `is`(true))
        assertThat(classes.toString().contains("JsonUtils"), `is`(true))
        assertThat(classes.toString().contains("WordUtils"), `is`(true))
    }

    @Test
    fun `should return BeanDefinitions if parameter is class type`() {
        val classes = FastMethodInvoker.findClasses(FastMethodInvoker::class.java)
        assertThat(classes.toString().contains("JsonUtils"), `is`(true))
        assertThat(classes.toString().contains("WordUtils"), `is`(true))
    }

    @Test
    fun `method clone should return T target from sources`() {
        class User {
            var age = 0
            var name: String? = null

            constructor() {}
            constructor(age: Int, name: String?) {
                this.age = age
                this.name = name
            }
        }

        val user = User(10, "ABC")
        val user2 = User(101, "ABC1")
        val clone = FastMethodInvoker.clone(user,user2)
        assertThat(clone.age, equalTo(user2.age))
        assertThat(clone.name, equalTo(user2.name))

    }

}