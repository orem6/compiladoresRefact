package com.umg.model.dialect;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dialectos de base de datos soportados por el compilador.")
public enum SqlDialect {
    @Schema(description = "Dialecto comun/generico.")
    COMMON,
    @Schema(description = "MySQL.")
    MYSQL,
    @Schema(description = "PostgreSQL.")
    POSTGRESQL,
    @Schema(description = "SQL Server.")
    SQL_SERVER,
    @Schema(description = "Cassandra (CQL).")
    CASSANDRA,
    @Schema(description = "MongoDB (aggregation pipeline).")
    MONGODB
}
