package org.springframework.data.r2dbc.support;

import org.junit.jupiter.api.Test;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Qualifier;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class FastMethodInvokerTest {

    @Test
    void reflectionStorageShouldReturnFields() throws NoSuchFieldException {
        var resultList = FastMethodInvoker.reflectionStorage(String.class);

        assertThat(resultList, notNullValue());
        assertThat(resultList.size(), is(9));
        assertThat(resultList.get(0), is(String.class.getDeclaredField("value")));
    }

    @Test
    void methodWithParameterClassInstanceShouldReturnTrueIfClassHasField() {
        var result = FastMethodInvoker.has(LocalDate.now(), "DAYS_0000_TO_1970");

        assertThat(result, is(true));
    }

    @Test
    void methodWithParameterClassInstanceShouldReturnTrueIfClassNotHasField() {
        var result = FastMethodInvoker.has(LocalDate.class, "DAYS_0000_TO_1970");

        assertThat(result, is(true));
    }

    @Test
    void methodWithParameterClassTypeShouldReturnTrueIfClassHasField() {
        var result = FastMethodInvoker.has(LocalDate.class, "DAYS_0000_TO_1970");

        assertThat(result, is(true));
    }

    @Test
    void methodWithParameterClassTypeShouldReturnTrueIfClassNotHasField() {
        var result = FastMethodInvoker.has(LocalDate.class, "DAYS_0000_TO_1912");

        assertThat(result, is(false));
    }

    @Test
    void shouldReturnFieldIfClassHasThisField() {
        var result = FastMethodInvoker.getField(LocalDate.class, "DAYS_0000_TO_1970");

        assertThat(result, notNullValue());
        assertThat(result.getName().contains("DAYS"), is(true));
    }

    @Test
    void methodCopyShouldReturnTarget() {
        var result = FastMethodInvoker.copy(new User(4), new User(2));

        assertThat(result.age, equalTo(4));
    }

    @Test
    void shouldReturnValueOfTargetFieldIfSourceIsNull() {
        var result = FastMethodInvoker.copyNotNull(0, new User(3));

        assertThat(result.age, equalTo(3));
    }

    @Test
    void shouldReturnValueOfSourceFieldIfTargetIsNull() {
        class User1 {
            int age;

            public void setAge(int age) {
                this.age = age;
            }
        }

        var result = FastMethodInvoker.copyIsNull(new User(1), new User1());

        assertThat(result.age, equalTo(1));
    }

    @Test
    void shouldReturnMapFromObject() {
        var resultMap = FastMethodInvoker.objectToMap(LocalDate.now());

        assertThat(resultMap, notNullValue());
        assertThat(resultMap.getClass(), equalTo(HashMap.class));
        assertThat(resultMap.size(), equalTo(2));
        assertThat(resultMap.containsKey("month"), is(true));
    }

    @Test
    void shouldReturnMapFromTwoObjects() {
        var collection = new ArrayList<>();
        collection.add(LocalDate.now());
        collection.add(LocalDate.now().minusYears(1).minusMonths(2));
        var resultMap = FastMethodInvoker.objectsToMap(collection, new String("year"), new String("month"));

        assertThat(resultMap, notNullValue());
        assertThat(resultMap.getClass(), equalTo(HashMap.class));
        assertThat(resultMap.keySet().size(), greaterThan(0));
        assertThat(resultMap.containsValue(LocalDate.now().getMonth()), is(true));
    }

    @Test
    void shouldSetValueForUserFieldWithNameEqualsAge() {
        var user = new User(1);
        FastMethodInvoker.setValue(user, "age", "7");

        assertThat(user.getAge(), equalTo(7));
    }

    @Test
    void shouldSetValuesFromMapForUserFieldsAgeAndName() {
        var user = new User(1, "S");
        Map<String, Object> map = new HashMap<>();
        map.put("age", 7);
        map.put("name", "test");
        FastMethodInvoker.setMapValues(user, map);

        assertThat(user.getAge(), equalTo(7));
        assertThat(user.getName(), equalTo("test"));
    }

    @Test
    void shouldReturnObjectIfFieldNameEqualsName() {
        var localDate = LocalDate.now();
        var result = FastMethodInvoker.getValue(localDate, "month");

        assertThat(result, notNullValue());
        assertThat(result, is(localDate.getMonth()));
    }

    @Test
    void methodWithParameterClassTypeReturnObjectIfFieldNameEqualsName() {
        var localDate = LocalDate.now();
        var result = FastMethodInvoker.getValue(localDate, "month", Month.class);

        assertThat(result, notNullValue());
        assertThat(result, is(localDate.getMonth()));
    }

    @Test
    void shouldReturnObjectIfParameterIsString() {
        var result = FastMethodInvoker.stringToObject("test", String.class);

        assertThat(result, notNullValue());
        assertThat(result, equalTo("test"));
    }

    @Test
    void shouldReturnObjectIfParameterIsBigInteger() {
        var result = FastMethodInvoker.stringToObject("1", BigInteger.class);

        assertThat(result, equalTo(new BigInteger(String.valueOf(1))));
    }

    @Test
    void shouldReturnFieldsByAnnotation() {
        class User {
            @Qualifier
            String name;
            @Qualifier
            int age;

            public User(String name, int age) {
                this.name = name;
                this.age = age;
            }
        }

        var user = new User("test", 32);
        List<Field> resultList = FastMethodInvoker.getFieldsByAnnotation(user.getClass(), Qualifier.class);

        assertThat(resultList, notNullValue());
        assertThat(resultList.get(0).getName(), equalTo("name"));
        assertThat(resultList.get(1).getName(), equalTo("age"));
    }

    @Test
    void shouldReturnFirstFieldByAnnotation() {
        class User {
            @Qualifier
            String name;
            @Qualifier
            int age;

            public User(String name, int age) {
                this.name = name;
                this.age = age;
            }
        }
        var user = new User("test", 32);
        Optional<Field> result = FastMethodInvoker.getFieldByAnnotation(user.getClass(), Qualifier.class);

        assertThat(result.get().getName(), equalTo("name"));
    }

    @Test
    void shouldReturnFieldsByNameAndAnnotation() {
        class User {
            int id;
            @Qualifier
            String name;
            @Spy
            int age;

            public User(int id, String name, int age) {
                this.id = id;
                this.name = name;
                this.age = age;
            }
        }

        Set<Field> fields = FastMethodInvoker.getFields(User.class, "id", Qualifier.class, Spy.class);
        List<Field> result = new ArrayList<>(fields);

        assertThat(fields, notNullValue());
        assertThat(fields.size(), is(3));
        assertThat(result.get(0).getName(), equalTo("age"));
        assertThat(result.get(1).getName(), equalTo("id"));
        assertThat(result.get(2).getName(), equalTo("name"));
    }

    @Test
    void shouldReturnBeanDefinitionsIfParameterIsString() {
        var resultSet = FastMethodInvoker.findClasses("org.springframework.data.r2dbc.support");

        assertThat(resultSet.toString().contains("FastMethodInvoker"), is(true));
        assertThat(resultSet.toString().contains("JsonUtils"), is(true));
        assertThat(resultSet.toString().contains("WordUtils"), is(true));
    }

    @Test
    void shouldReturnBeanDefinitionsIfParameterIsClassType() {
        var resultSet = FastMethodInvoker.findClasses(FastMethodInvoker.class);

        assertThat(resultSet.toString().contains("JsonUtils"), is(true));
        assertThat(resultSet.toString().contains("WordUtils"), is(true));
    }

    @Test
    void methodCloneShouldReturnTargetFromSources() {
        var user = new User(10, "ABC");
        var user2 = new User(101, "ABC1");
        var result = FastMethodInvoker.clone(user, user2);

        assertThat(result.age, equalTo(user2.age));
        assertThat(result.name, equalTo(user2.name));

    }

    static class User {
        int id;
        String name;
        int age;

        public User() {
        }

        public User(int age) {
            this.age = age;
        }

        public User(int age, String name) {
            this.age = age;
            this.name = name;
        }

        public User(int id, String name, int age) {
            this.id = id;
            this.name = name;
            this.age = age;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}