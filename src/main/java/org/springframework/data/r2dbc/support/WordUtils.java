package org.springframework.data.r2dbc.support;

import org.apache.commons.text.CharacterPredicates;
import org.apache.commons.text.RandomStringGenerator;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;

/**
 * Utilities for string interaction.
 *
 * @author Lao Tsing
 */
public abstract class WordUtils {
    @NonNull
    public static String sqlToCamel(@NonNull final String sqlName) {
        var parts = sqlName.split("_");
        var camelCaseString = new StringBuilder(parts[0]);
        if (parts.length > 1) {
            for (int i = 1; i < parts.length; i++) {
                if (parts[i] != null && parts[i].trim().length() > 0)
                    camelCaseString.append(StringUtils.capitalize(parts[i]));
            }
        }
        return camelCaseString.toString();
    }

    @NonNull
    public static String camelToSql(@NonNull final String camel) {
        return camel.replaceAll("[A-Z]", "_$0").toLowerCase();
    }

    @NonNull
    public static String trimInline(@NonNull final String text) {
        return text.replaceAll("(\\s+\\n|\\n|\\s+)", " ");
    }

    @NonNull
    public static String generateString(final int size) {
        return new RandomStringGenerator.Builder().withinRange('0', 'z')
                .filteredBy(CharacterPredicates.DIGITS, CharacterPredicates.LETTERS)
                .build().generate(size);
    }

    public static String lastOctet(@NonNull String fieldName) {
        return fieldName.contains(".") ? fieldName.substring(fieldName.lastIndexOf(".") + 1) : fieldName;
    }

    public static String removeAfter(@NonNull String source, @NonNull String template) {
        if (source.indexOf(template) > 0)
            return source.substring(0, source.indexOf(template));
        else
            return source;
    }
}
