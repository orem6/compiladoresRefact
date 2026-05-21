package com.umg.model.semantic.metadata;

import com.umg.model.dialect.SqlDialect;
import com.umg.model.semantic.config.ConexionBaseDatosConfig;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Component
public class JdbcConnectionFactory {

    public Connection crearConexion(ConexionBaseDatosConfig config) throws SQLException {
        if (config == null || !config.esValida()) {
            throw new SQLException("Configuracion de conexion invalida");
        }

        if (config.isUsarUrlJdbcDirecta()) {
            return DriverManager.getConnection(
                config.getUrlJdbc(), config.getUsuario(), config.getPassword());
        }

        String url = construirUrl(config);
        return DriverManager.getConnection(url, config.getUsuario(), config.getPassword());
    }

    public String construirUrl(ConexionBaseDatosConfig config) {
        if (config.isUsarUrlJdbcDirecta()) return config.getUrlJdbc();

        SqlDialect dialecto = config.getDialecto();
        String host = config.getHost();
        int puerto = config.getPuerto();
        String db = config.getBaseDatos();

        return switch (dialecto) {
            case MYSQL -> "jdbc:mysql://" + host + ":" + puerto + "/" + db
                + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
            case POSTGRESQL ->
                "jdbc:postgresql://" + host + ":" + puerto + "/" + db;
            case SQL_SERVER ->
                "jdbc:sqlserver://" + host + ":" + puerto
                + ";databaseName=" + db + ";encrypt=false;trustServerCertificate=true";
            default ->
                "jdbc:mysql://" + host + ":" + puerto + "/" + db;
        };
    }
}
