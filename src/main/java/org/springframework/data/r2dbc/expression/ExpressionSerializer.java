package org.springframework.data.r2dbc.expression;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.expression.Expression;

import java.io.IOException;

public class ExpressionSerializer extends JsonSerializer<Expression> {
    @Override
    public void serialize(Expression expression, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        var stringValue = expression.getExpressionString();
        if(!stringValue.isEmpty() && !stringValue.equals("null")) {
            jsonGenerator.writeString(stringValue);
        } else {
            jsonGenerator.writeNull();
        }
    }

    @Override
    public Class<Expression> handledType() {
        return Expression.class;
    }
}
