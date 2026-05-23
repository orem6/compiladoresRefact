package com.umg.application.compiler;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.umg.model.dialect.CompilerDialect;
import com.umg.model.dialect.DialectMapper;
import com.umg.model.dialect.SqlDialect;
import com.umg.model.semantic.config.ConexionBaseDatosConfig;
import com.umg.model.semantic.metadata.CqlConnectionFactory;
import com.umg.model.semantic.metadata.JdbcConnectionFactory;
import com.umg.model.semantic.metadata.MongoConnectionFactory;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class ConnectionValidationService {

    private final JdbcConnectionFactory jdbcConnectionFactory;
    private final CqlConnectionFactory cqlConnectionFactory;
    private final MongoConnectionFactory mongoConnectionFactory;

    public ConnectionValidationService(JdbcConnectionFactory jdbcConnectionFactory,
                                       CqlConnectionFactory cqlConnectionFactory,
                                       MongoConnectionFactory mongoConnectionFactory) {
        this.jdbcConnectionFactory = jdbcConnectionFactory;
        this.cqlConnectionFactory = cqlConnectionFactory;
        this.mongoConnectionFactory = mongoConnectionFactory;
    }

    public Map<String, Object> testConnection(CompilerDialect dialect, ConexionBaseDatosConfig config) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("connected", false);

        if (config == null || !config.esValida()) {
            result.put("message", "Configuracion de conexion invalida.");
            result.put("executionStatus", "INVALID_REQUEST");
            return result;
        }

        if (dialect == CompilerDialect.CASSANDRA_CQL) {
            return testCqlConnection(config, dialect);
        }

        if (dialect == CompilerDialect.MONGODB) {
            return testMongoConnection(config, dialect);
        }

        SqlDialect sqlDialect = DialectMapper.toSqlDialect(dialect);
        return testJdbcConnection(config, dialect, sqlDialect);
    }

    private Map<String, Object> testJdbcConnection(ConexionBaseDatosConfig config, CompilerDialect dialect, SqlDialect sqlDialect) {
        Map<String, Object> result = new LinkedHashMap<>();
        try (Connection conexion = jdbcConnectionFactory.crearConexion(config)) {
            boolean isValid = conexion.isValid(10);
            result.put("connected", isValid);
            result.put("message", isValid ? "Conexion validada correctamente." : "La conexion no respondio correctamente.");
            result.put("executionStatus", isValid ? "SUCCESS" : "CONNECTION_ERROR");
            result.put("dialect", dialect.name());
            result.put("database", config.getBaseDatos());
            result.put("schema", config.getEsquemaParaBusqueda());
            result.put("host", config.getHost());
            result.put("port", config.getPuerto());
            result.put("driver", sqlDialect.name() + " JDBC Driver");
        } catch (SQLException e) {
            result.put("connected", false);
            result.put("message", "Error de conexion: " + e.getMessage());
            result.put("executionStatus", "CONNECTION_ERROR");
            result.put("error", e.getMessage());
        }
        return result;
    }

    private Map<String, Object> testCqlConnection(ConexionBaseDatosConfig config, CompilerDialect dialect) {
        Map<String, Object> result = new LinkedHashMap<>();
        try (com.datastax.oss.driver.api.core.CqlSession session = cqlConnectionFactory.crearConexion(config)) {
            session.execute("SELECT release_version FROM system.local");
            boolean isValid = !session.isClosed();
            result.put("connected", isValid);
            result.put("message", isValid ? "Conexion validada correctamente." : "La conexion no respondio correctamente.");
            result.put("executionStatus", isValid ? "SUCCESS" : "CONNECTION_ERROR");
            result.put("dialect", dialect.name());
            result.put("database", config.getBaseDatos());
            result.put("host", config.getHost());
            result.put("port", config.getPuerto());
            result.put("driver", "Cassandra Java Driver");
        } catch (Exception e) {
            result.put("connected", false);
            result.put("message", "Error de conexion Cassandra: " + e.getMessage());
            result.put("executionStatus", "CONNECTION_ERROR");
            result.put("error", e.getMessage());
        }
        return result;
    }

    private Map<String, Object> testMongoConnection(ConexionBaseDatosConfig config, CompilerDialect dialect) {
        Map<String, Object> result = new LinkedHashMap<>();
        try (MongoClient mongoClient = mongoConnectionFactory.crearConexion(config)) {
            MongoDatabase db = mongoClient.getDatabase(
                config.getBaseDatos() != null ? config.getBaseDatos() : "admin");
            db.runCommand(new org.bson.Document("ping", 1));
            result.put("connected", true);
            result.put("message", "Conexion validada correctamente.");
            result.put("executionStatus", "SUCCESS");
            result.put("dialect", dialect.name());
            result.put("database", config.getBaseDatos());
            result.put("host", config.getHost());
            result.put("port", config.getPuerto());
            result.put("driver", "MongoDB Java Driver");
        } catch (Exception e) {
            result.put("connected", false);
            result.put("message", "Error de conexion MongoDB: " + e.getMessage());
            result.put("executionStatus", "CONNECTION_ERROR");
            result.put("error", e.getMessage());
        }
        return result;
    }
}
