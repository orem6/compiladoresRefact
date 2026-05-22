package com.umg.application.compiler;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
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

    public Map<String, Object> testConnection(SqlDialect sqlDialect, ConexionBaseDatosConfig config) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("valid", false);

        if (config == null || !config.esValida()) {
            result.put("message", "Configuracion de conexion invalida.");
            result.put("status", "INVALID_CONFIG");
            return result;
        }

        if (sqlDialect == SqlDialect.CASSANDRA) {
            return testCqlConnection(config, sqlDialect);
        }

        if (sqlDialect == SqlDialect.MONGODB) {
            return testMongoConnection(config, sqlDialect);
        }

        return testJdbcConnection(config, sqlDialect);
    }

    private Map<String, Object> testJdbcConnection(ConexionBaseDatosConfig config, SqlDialect dialect) {
        Map<String, Object> result = new LinkedHashMap<>();
        try (Connection conexion = jdbcConnectionFactory.crearConexion(config)) {
            boolean isValid = conexion.isValid(10);
            result.put("valid", isValid);
            result.put("message", isValid ? "Conexion exitosa a la base de datos." : "La conexion no respondio correctamente.");
            result.put("status", isValid ? "SUCCESS" : "CONNECTION_FAILED");
            result.put("dialect", dialect.name());
            result.put("database", config.getBaseDatos());
            result.put("schema", config.getEsquemaParaBusqueda());
            result.put("host", config.getHost());
            result.put("port", config.getPuerto());
        } catch (SQLException e) {
            result.put("valid", false);
            result.put("message", "Error de conexion: " + e.getMessage());
            result.put("status", "CONNECTION_ERROR");
            result.put("error", e.getMessage());
        }
        return result;
    }

    private Map<String, Object> testCqlConnection(ConexionBaseDatosConfig config, SqlDialect dialect) {
        Map<String, Object> result = new LinkedHashMap<>();
        try (com.datastax.oss.driver.api.core.CqlSession session = cqlConnectionFactory.crearConexion(config)) {
            boolean isValid = !session.isClosed();
            result.put("valid", isValid);
            result.put("message", isValid ? "Conexion exitosa a Cassandra." : "La conexion no respondio correctamente.");
            result.put("status", isValid ? "SUCCESS" : "CONNECTION_FAILED");
            result.put("dialect", dialect.name());
            result.put("database", config.getBaseDatos());
            result.put("host", config.getHost());
            result.put("port", config.getPuerto());
        } catch (Exception e) {
            result.put("valid", false);
            result.put("message", "Error de conexion Cassandra: " + e.getMessage());
            result.put("status", "CONNECTION_ERROR");
            result.put("error", e.getMessage());
        }
        return result;
    }

    private Map<String, Object> testMongoConnection(ConexionBaseDatosConfig config, SqlDialect dialect) {
        Map<String, Object> result = new LinkedHashMap<>();
        try (MongoClient mongoClient = mongoConnectionFactory.crearConexion(config)) {
            MongoDatabase db = mongoClient.getDatabase(
                config.getBaseDatos() != null ? config.getBaseDatos() : "admin");
            db.listCollectionNames().first();
            result.put("valid", true);
            result.put("message", "Conexion exitosa a MongoDB.");
            result.put("status", "SUCCESS");
            result.put("dialect", dialect.name());
            result.put("database", config.getBaseDatos());
            result.put("host", config.getHost());
            result.put("port", config.getPuerto());
        } catch (Exception e) {
            result.put("valid", false);
            result.put("message", "Error de conexion MongoDB: " + e.getMessage());
            result.put("status", "CONNECTION_ERROR");
            result.put("error", e.getMessage());
        }
        return result;
    }
}
