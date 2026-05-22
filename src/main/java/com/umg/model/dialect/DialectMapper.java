package com.umg.model.dialect;

public class DialectMapper {

    public static boolean isSql(CompilerDialect dialect) {
        return dialect == CompilerDialect.MYSQL
            || dialect == CompilerDialect.POSTGRESQL
            || dialect == CompilerDialect.SQL_SERVER;
    }

    public static boolean isNoSql(CompilerDialect dialect) {
        return dialect == CompilerDialect.MONGODB
            || dialect == CompilerDialect.CASSANDRA_CQL;
    }

    public static SqlDialect toSqlDialect(CompilerDialect dialect) {
        return switch (dialect) {
            case MYSQL -> SqlDialect.MYSQL;
            case POSTGRESQL -> SqlDialect.POSTGRESQL;
            case SQL_SERVER -> SqlDialect.SQL_SERVER;
            default -> throw new IllegalArgumentException("Dialecto no SQL: " + dialect);
        };
    }

    public static NoSqlDialect toNoSqlDialect(CompilerDialect dialect) {
        return switch (dialect) {
            case MONGODB -> NoSqlDialect.MONGODB;
            case CASSANDRA_CQL -> NoSqlDialect.CASSANDRA_CQL;
            default -> throw new IllegalArgumentException("Dialecto NoSQL invalido: " + dialect);
        };
    }

    public static CompilerDialect fromSqlDialect(SqlDialect sqlDialect) {
        return switch (sqlDialect) {
            case MYSQL -> CompilerDialect.MYSQL;
            case POSTGRESQL -> CompilerDialect.POSTGRESQL;
            case SQL_SERVER -> CompilerDialect.SQL_SERVER;
            default -> throw new IllegalArgumentException("SqlDialect desconocido: " + sqlDialect);
        };
    }
}
