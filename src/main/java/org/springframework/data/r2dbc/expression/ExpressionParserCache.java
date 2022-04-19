package org.springframework.data.r2dbc.expression;

import org.jetbrains.annotations.NotNull;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParseException;
import org.springframework.expression.ParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.ConcurrentReferenceHashMap;

import java.util.Map;

public class ExpressionParserCache implements ExpressionParser {
    public static final ExpressionParserCache INSTANCE = new ExpressionParserCache();

    private static final Map<String, Expression> CACHE = new ConcurrentReferenceHashMap<>(512);
    private static final ExpressionParser PARSER = new SpelExpressionParser();

    @Override
    @NotNull
    public Expression parseExpression(@NotNull String expressionString) throws ParseException {
        return CACHE.computeIfAbsent(expressionString, key -> PARSER.parseExpression(expressionString));
    }

    @Override
    @NotNull
    public Expression parseExpression(@NotNull String expressionString, @NotNull ParserContext context) throws ParseException {
        throw new UnsupportedOperationException("Parsing using ParserContext is not supported");
    }
}
