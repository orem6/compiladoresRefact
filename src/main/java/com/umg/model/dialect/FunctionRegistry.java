package com.umg.model.dialect;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class FunctionRegistry {

    private static final Set<String> COMMON_FUNCTIONS = new HashSet<>(Arrays.asList(
        "COUNT", "SUM", "AVG", "MIN", "MAX",
        "CONCAT", "COALESCE", "NULLIF",
        "UPPER", "LOWER", "TRIM", "LTRIM", "RTRIM",
        "SUBSTRING", "LENGTH",
        "ROUND", "ABS",
        "NOW", "CURRENT_DATE", "CURRENT_TIME", "CURRENT_TIMESTAMP",
        "CAST", "CONVERT",
        "ISNULL",
        "LEN"
    ));

    private static final Set<String> MYSQL_FUNCTIONS = new HashSet<>(Arrays.asList(
        "DATE_FORMAT", "IFNULL", "IF", "DATABASE", "UUID",
        "STR_TO_DATE", "CHAR_LENGTH",
        "LAST_INSERT_ID", "FOUND_ROWS",
        "ROW_COUNT", "UUID_SHORT",
        "GROUP_CONCAT", "CONCAT_WS",
        "UNIX_TIMESTAMP", "FROM_UNIXTIME",
        "DATE_ADD", "DATE_SUB", "DATEDIFF",
        "DAY", "MONTH", "YEAR", "HOUR", "MINUTE", "SECOND",
        "WEEK", "QUARTER", "DAYNAME", "MONTHNAME",
        "CURDATE", "CURTIME", "SYSDATE",
        "RAND", "POW", "POWER", "SQRT", "CEILING", "FLOOR",
        "MOD", "TRUNCATE", "GREATEST", "LEAST",
        "MD5", "SHA1", "SHA2", "AES_ENCRYPT", "AES_DECRYPT",
        "COMPRESS", "UNCOMPRESS",
        "JSON_EXTRACT", "JSON_UNQUOTE", "JSON_KEYS",
        "JSON_ARRAY", "JSON_OBJECT",
        "BIT_LENGTH", "OCTET_LENGTH",
        "ORD", "CONV", "BIN", "HEX", "OCT",
        "ASCII", "CHAR", "SPACE", "REPLACE",
        "REVERSE", "INSERT", "LOCATE", "POSITION",
        "INSTR", "LPAD", "RPAD", "LEFT", "RIGHT",
        "REPEAT", "SUBSTRING_INDEX",
        "WEIGHT_STRING", "MATCH", "AGAINST"
    ));

    private static final Set<String> POSTGRESQL_FUNCTIONS = new HashSet<>(Arrays.asList(
        "TO_CHAR", "TO_DATE", "DATE_TRUNC", "STRING_AGG", "ARRAY_AGG",
        "GEN_RANDOM_UUID", "LENGTH",
        "POSITION", "SUBSTR",
        "SPLIT_PART", "REGEXP_REPLACE", "REGEXP_MATCHES",
        "REGEXP_SPLIT_TO_ARRAY", "REGEXP_SPLIT_TO_TABLE",
        "STRING_TO_ARRAY", "ARRAY_TO_STRING",
        "UNNEST", "GENERATE_SERIES",
        "NOW", "STATEMENT_TIMESTAMP", "CLOCK_TIMESTAMP",
        "AGE", "EXTRACT", "DATE_PART", "DATE_TRUNC",
        "MAKE_DATE", "MAKE_TIME", "MAKE_TIMESTAMP",
        "TO_TIMESTAMP", "TO_NUMBER",
        "GREATEST", "LEAST",
        "RANDOM", "SETSEED",
        "PG_CANCEL_BACKEND", "PG_TERMINATE_BACKEND",
        "PG_RELOAD_CONF",
        "CURRENT_DATABASE", "CURRENT_SCHEMA",
        "PG_TABLE_IS_VISIBLE",
        "PG_GET_SERIAL_SEQUENCE",
        "FORMAT", "QUERY_TO_XML", "TABLE_TO_XML",
        "XMLCOMMENT", "XMLCONCAT", "XMLELEMENT",
        "XMLFOREST", "XMLPARSE", "XMLPI", "XMLROOT",
        "XMLSERIALIZE", "XMLTABLE",
        "JSON_BUILD_OBJECT", "JSON_BUILD_ARRAY",
        "JSON_OBJECT", "JSON_ARRAY",
        "JSON_TO_RECORD", "JSON_TO_RECORDSET",
        "ROW_TO_JSON", "JSON_AGG",
        "JSONB_BUILD_OBJECT", "JSONB_BUILD_ARRAY",
        "JSONB_OBJECT", "JSONB_ARRAY",
        "JSONB_TO_RECORD", "JSONB_TO_RECORDSET",
        "ROW_TO_JSONB", "JSONB_AGG",
        "TO_JSON", "TO_JSONB",
        "JSON_EACH", "JSON_EACH_TEXT",
        "JSON_OBJECT_KEYS", "JSON_TYPEof",
        "JSONB_EACH", "JSONB_EACH_TEXT",
        "JSONB_OBJECT_KEYS", "JSONB_TYPEof",
        "PG_SLEEP", "PG_SLEEP_FOR"
    ));

    private static final Set<String> CASSANDRA_FUNCTIONS = new HashSet<>(Arrays.asList(
        "TOKEN", "WRITETIME", "TTL",
        "NOW", "MINITIMEUUID", "MAXTIMEUUID",
        "DATEOF", "UNIXTIMESTAMPOF",
        "BLOBAS", "BIGINTASBLOB", "BOOLEANASBLOB",
        "DATEASBLOB", "DECIMALASBLOB", "DOUBLEASBLOB",
        "FLOATASBLOB", "INETASBLOB", "INTASBLOB",
        "TEXTASBLOB", "TIMESTAMPASBLOB", "TIMEUUIDASBLOB",
        "TINYINTASBLOB", "SMALLINTASBLOB", "UUIDASBLOB",
        "VARCHARASBLOB", "VARINTASBLOB",
        "BLOOBIGINT", "BLOBTOBOOLEAN", "BLOBTODATE",
        "BLOBTODECIMAL", "BLOBTODOUBLE", "BLOBTOFLOAT",
        "BLOBTOINET", "BLOBTOINT", "BLOBTOTEXT",
        "BLOBTOTIMESTAMP", "BLOBTOTIMEUUID", "BLOBTOTINYINT",
        "BLOBTOSMALLINT", "BLOBTOUUID", "BLOBTOVARCHAR",
        "BLOBTOVARINT",
        "CURRENT_DATE", "CURRENT_TIME", "CURRENT_TIMESTAMP",
        "CURRENTUSER", "LOGIN",
        "TOUNIXTIMESTAMP",
        "TOTIMESTAMP", "TODATE",
        "COUNT", "SUM", "AVG", "MIN", "MAX",
        "NOW", "UUID", "TIMEUUID"
    ));

    private static final Set<String> SQL_SERVER_FUNCTIONS = new HashSet<>(Arrays.asList(
        "GETDATE", "ISNULL", "LEN", "DATEADD", "DATEDIFF",
        "FORMAT", "NEWID", "SYSDATETIME",
        "DATE_BUCKET", "DATEDIFF_BIG",
        "DATEFROMPARTS", "DATETIMEFROMPARTS",
        "SMALLDATETIMEFROMPARTS", "TIMEFROMPARTS",
        "DATENAME", "DATEPART", "DAY", "MONTH", "YEAR",
        "EOMONTH", "SWITCHOFFSET", "TODATETIMEOFFSET",
        "SYSDATETIMEOFFSET", "SYSUTCDATETIME",
        "CURRENT_TIMESTAMP", "CURRENT_TIMEZONE",
        "CURRENT_TIMEZONE_ID",
        "CHARINDEX", "PATINDEX",
        "LEFT", "RIGHT", "SUBSTRING", "LEN", "DATALENGTH",
        "CHAR", "NCHAR", "UNICODE", "ASCII",
        "SPACE", "STR", "REPLICATE", "REVERSE",
        "REPLACE", "STUFF",
        "LOWER", "UPPER", "TRIM", "LTRIM", "RTRIM",
        "CONCAT", "CONCAT_WS", "STRING_AGG",
        "STRING_SPLIT", "STRING_ESCAPE",
        "SOUNDEX", "DIFFERENCE",
        "ABS", "CEILING", "FLOOR", "POWER", "SQRT",
        "SQUARE", "ROUND", "SIGN",
        "RAND", "RANDBETWEEN",
        "EXP", "LOG", "LOG10", "PI",
        "SIN", "COS", "TAN", "ASIN", "ACOS", "ATAN",
        "DEGREES", "RADIANS",
        "SUM", "AVG", "MIN", "MAX", "COUNT",
        "STDEV", "STDEVP", "VAR", "VARP",
        "GROUPING", "GROUPING_ID",
        "CHECKSUM_AGG",
        "CAST", "CONVERT", "PARSE", "TRY_CAST",
        "TRY_CONVERT", "TRY_PARSE",
        "ISDATE", "ISNUMERIC",
        "COALESCE", "NULLIF", "ISNULL",
        "IIF", "CHOOSE",
        "HASHBYTES",
        "SESSION_USER", "CURRENT_USER", "SYSTEM_USER",
        "USER_NAME", "ORIGINAL_LOGIN",
        "SUSER_NAME", "SUSER_SNAME", "SUSER_ID",
        "DB_NAME", "DB_ID", "OBJECT_NAME", "OBJECT_ID",
        "SCHEMA_NAME", "SCHEMA_ID",
        "COL_NAME", "COL_LENGTH",
        "IDENT_CURRENT", "IDENT_INCR", "IDENT_SEED",
        "ROWCOUNT_BIG",
        "MIN_ACTIVE_ROWVERSION",
        "ERROR_LINE", "ERROR_MESSAGE", "ERROR_NUMBER",
        "ERROR_PROCEDURE", "ERROR_SEVERITY", "ERROR_STATE",
        "FORMATMESSAGE",
        "OPENJSON", "JSON_VALUE", "JSON_QUERY",
        "JSON_MODIFY",
        "COMPRESS", "DECOMPRESS",
        "SESSION_CONTEXT",
        "RANK", "DENSE_RANK", "ROW_NUMBER", "NTILE",
        "LEAD", "LAG", "FIRST_VALUE", "LAST_VALUE",
        "CUME_DIST", "PERCENT_RANK",
        "PERCENTILE_CONT", "PERCENTILE_DISC"
    ));

    public static boolean isCommonFunction(String word) {
        return COMMON_FUNCTIONS.contains(word.toUpperCase());
    }

    public static boolean isMySqlFunction(String word) {
        return MYSQL_FUNCTIONS.contains(word.toUpperCase());
    }

    public static boolean isPostgreSqlFunction(String word) {
        return POSTGRESQL_FUNCTIONS.contains(word.toUpperCase());
    }

    public static boolean isSqlServerFunction(String word) {
        return SQL_SERVER_FUNCTIONS.contains(word.toUpperCase());
    }

    public static boolean isCassandraFunction(String word) {
        return CASSANDRA_FUNCTIONS.contains(word.toUpperCase());
    }

    public static boolean isFunctionForDialect(String word, SqlDialect dialect) {
        String upper = word.toUpperCase();
        if (COMMON_FUNCTIONS.contains(upper)) return true;
        return switch (dialect) {
            case MYSQL -> MYSQL_FUNCTIONS.contains(upper);
            case POSTGRESQL -> POSTGRESQL_FUNCTIONS.contains(upper);
            case SQL_SERVER -> SQL_SERVER_FUNCTIONS.contains(upper);
            case CASSANDRA -> CASSANDRA_FUNCTIONS.contains(upper);
            default -> false;
        };
    }

    public static boolean isKnownFunction(String word) {
        String upper = word.toUpperCase();
        return COMMON_FUNCTIONS.contains(upper)
            || MYSQL_FUNCTIONS.contains(upper)
            || POSTGRESQL_FUNCTIONS.contains(upper)
            || SQL_SERVER_FUNCTIONS.contains(upper)
            || CASSANDRA_FUNCTIONS.contains(upper);
    }
}
