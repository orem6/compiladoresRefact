package com.umg.model.dialect;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dialectos NoSQL soportados.")
public enum NoSqlDialect {
    MONGODB,
    CASSANDRA_CQL
}
