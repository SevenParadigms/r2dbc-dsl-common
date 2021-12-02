package org.springframework.data.r2dbc.support

import org.sevenparadigms.kotlin.common.remove
import org.springframework.util.ObjectUtils
import java.net.URLDecoder
import java.util.regex.Pattern

object SQLInjectionSafe {
    private val mnemonic = """TABLE|TABLESPACE|PROCEDURE|FUNCTION|TRIGGER|VIEW|LIBRARY|REFERENCES|FROM|
            SELECT|INSERT|UPDATE|DELETE|TRUNCATE|USAGE|DATABASE|INDEX|CONSTRAINT|TRIGGER|SET|
            USER|SCHEMA|SQL|WORK|TRANSACTION|OPTION|COMMENT|SYNONYM|TYPE|SESSION|USER|ROLE|
            PACKAGE|BODY|OPERATOR|CASCADE|SEQUENCE|RESTORE|POINT|FILE|CLASS|CURSOR|OBJECT|
            RULE|DATASET|STORE|COLUMN|FIELD|HTTP|NULL|SLEEP|VERSION|PRIVILEGES|PROGRAM""".remove("[\\r\\n]")

    private val regex = Pattern.compile(
        "(.*)(;|UNION)([\\s\\r\\n])(COPY|DBLINK|GRANT|LOCK|TRUNCATE|WITH|ALTER|" +
                "CREATE|DELETE|DROP|EXEC(UTE)?|INSERT|UPSERT|MERGE|SELECT|JOIN|UPDATE)(.*)('(.*)'|" +
                " \\* |\\(([\\s\\r\\n])'|${mnemonic})(.*)", Pattern.CASE_INSENSITIVE or Pattern.UNICODE_CASE
    )

    @JvmStatic
    fun throwElse(query: String): Boolean {
        if (!ObjectUtils.isEmpty(query)) {
            val cleanQuery = URLDecoder.decode(query, Charsets.UTF_8)
            if (regex.matcher(cleanQuery).matches()) {
                throw RuntimeException("SQL Injection Detected")
            }
        }
        return true
    }

    @JvmStatic
    fun check(query: String): String {
        throwElse(query)
        return query
    }
}