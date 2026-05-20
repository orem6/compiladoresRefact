package com.umg.api.compiler.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.umg.model.dialect.SqlDialect;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Configuracion de conexion dinamica a base de datos.")
public class ConnectionConfigDto {

    @Schema(description = "Motor SQL de la conexion.", example = "MYSQL")
    private SqlDialect dialect;

    @Schema(description = "Host o IP del servidor de base de datos.", example = "localhost")
    private String host;

    @Schema(description = "Puerto del motor de base de datos.", example = "3306")
    private Integer port;

    @Schema(description = "Nombre de la base de datos.", example = "mi_base")
    private String database;

    @Schema(description = "Schema opcional. En PostgreSQL puede ser public; en SQL Server puede ser dbo.", example = "public")
    private String schema;

    @Schema(description = "Usuario de conexion.", example = "root")
    private String username;

    @Schema(
        description = "Contrasena de la base de datos. Solo se recibe en request; nunca se retorna en responses.",
        example = "********",
        accessMode = Schema.AccessMode.WRITE_ONLY
    )
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Schema(description = "URL JDBC directa para conexion alternativa.", example = "jdbc:mysql://localhost:3306/mi_base")
    private String jdbcUrl;

    @Schema(description = "Indica si se debe usar jdbcUrl directa en lugar de campos individuales.", example = "false")
    private Boolean useDirectJdbcUrl;

    public ConnectionConfigDto() {
    }

    public SqlDialect getDialect() {
        return dialect;
    }

    public void setDialect(SqlDialect dialect) {
        this.dialect = dialect;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public Boolean getUseDirectJdbcUrl() {
        return useDirectJdbcUrl;
    }

    public void setUseDirectJdbcUrl(Boolean useDirectJdbcUrl) {
        this.useDirectJdbcUrl = useDirectJdbcUrl;
    }
}
