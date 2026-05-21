package com.umg.model.dialect;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dialecto general del compilador: SQL y NoSQL.")
public enum CompilerDialect {
    MYSQL,
    POSTGRESQL,
    SQL_SERVER,
    MONGODB,
    CASSANDRA_CQL
}
