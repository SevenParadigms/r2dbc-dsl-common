package org.springframework.data.r2dbc.support;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;


@SpringBootTest(classes = Beans.class, properties = "spring.main.allow-bean-definition-overriding=true")
class BeansTest {

    @MockBean
    JsonFactory jsonFactory;
    @Autowired
    private Beans beans;

    @Test
    void shouldReturnBeanFromBeanType() {
        var result = Beans.of(beans.objectMapper().getClass());

        assertThat(result, notNullValue());
        assertThat(result.getClass().getName(), equalTo(ObjectMapper.class.getName()));
    }

    @Test
    void shouldReturnEmptyWhenParameterIsNull() {
        var result = Beans.getOrNull(null);

        assertThat(result, is(Optional.empty()));
    }

    @Test
    void shouldReturnEmptyWhenParameterIsNotNull() {
        var result = Beans.getOrNull(beans.objectMapper().getClass());

        assertThat(result.toString().contains("ObjectMapper"), is(true));
    }

    @Test
    void shouldPutBeanToCacheAndReturnThisBeanIfParameterNotNull() {
        var result = Beans.add(beans.objectMapper());

        assertThat(result, notNullValue());
        assertThat(result.getClass().getName(), equalTo(ObjectMapper.class.getName()));
    }

    @Test
    void shouldRegisterBeanInContextAndReturnThisBean() {
        var result = Beans.register(beans.objectMapper());

        assertThat(result, notNullValue());
        assertThat(result.getClass().getTypeName(), equalTo(ObjectMapper.class.getTypeName()));
    }

    @Test
    void shouldRegisterBenInContextAndReturnThisBeanWhenSecondParameterIsConstructorArgs() {
        Beans.register(ObjectMapper.class, jsonFactory);

        assert Beans.getApplicationContext() != null;
        assertThat(Beans.getApplicationContext().containsBean("objectMapper"), is(true));
    }

    @Test
    void shouldPutObjectToCacheAndReturnBean() {
        var result = Beans.putCache(new User(1, "A"));

        assertThat(result, notNullValue());
        assertThat(result.getClass().getTypeName(), equalTo(User.class.getTypeName()));
        assertThat(result.getId(), is(1));
        assertThat(result.getName(), is("A"));
    }

    static class User {
        int id;
        String name;

        public User() {
        }

        public User(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
