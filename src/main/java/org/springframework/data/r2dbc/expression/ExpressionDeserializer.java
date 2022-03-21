package org.springframework.data.r2dbc.expression;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.springframework.expression.Expression;

import java.io.IOException;

public class ExpressionDeserializer extends JsonDeserializer<Expression> {
    @Override
    public Expression deserialize(JsonParser jp, DeserializationContext ds) throws IOException {
        String value = jp.getValueAsString();
        if(value != null && !value.isEmpty() && !value.equals("null"))
            return ExpressionParserCache.INSTANCE.parseExpression(value);
        return null;
    }

    @Override
    public Class<Expression> handledType() {
        return Expression.class;
    }
}