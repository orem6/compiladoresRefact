package com.umg.model.semantic.metadata;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import com.umg.model.semantic.config.ConexionBaseDatosConfig;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;

@Component
public class CqlConnectionFactory {

    public CqlSession crearConexion(ConexionBaseDatosConfig config) {
        if (config == null || !config.esValida()) {
            throw new IllegalArgumentException("Configuracion de conexion invalida");
        }

        String host = config.getHost() != null ? config.getHost() : "localhost";
        int port = config.getPuerto() > 0 ? config.getPuerto() : 9042;
        String keyspace = config.getBaseDatos();
        String localDatacenter = config.getLocalDatacenter() != null && !config.getLocalDatacenter().isBlank()
            ? config.getLocalDatacenter() : "datacenter1";

        CqlSessionBuilder builder = CqlSession.builder()
            .addContactPoint(new InetSocketAddress(host, port))
            .withLocalDatacenter(localDatacenter);

        if (config.getUsuario() != null && !config.getUsuario().isBlank()) {
            builder.withAuthCredentials(config.getUsuario(), config.getPassword() != null ? config.getPassword() : "");
        }

        if (keyspace != null && !keyspace.isEmpty()) {
            builder.withKeyspace(keyspace);
        }

        return builder.build();
    }
}
