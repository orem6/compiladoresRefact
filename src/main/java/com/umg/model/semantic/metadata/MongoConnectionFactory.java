package com.umg.model.semantic.metadata;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.umg.model.semantic.config.ConexionBaseDatosConfig;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class MongoConnectionFactory {

    public MongoClient crearConexion(ConexionBaseDatosConfig config) {
        if (config == null || !config.esValida()) {
            throw new IllegalArgumentException("Configuracion de conexion invalida");
        }

        if (config.getUrlJdbc() != null && !config.getUrlJdbc().isEmpty()) {
            return MongoClients.create(config.getUrlJdbc());
        }

        String host = config.getHost() != null ? config.getHost() : "localhost";
        int port = config.getPuerto() > 0 ? config.getPuerto() : 27017;

        MongoClientSettings settings = MongoClientSettings.builder()
            .applyToClusterSettings(builder ->
                builder.hosts(java.util.Collections.singletonList(new ServerAddress(host, port))))
            .applyToConnectionPoolSettings(builder ->
                builder.maxConnectionIdleTime(10, TimeUnit.SECONDS))
            .build();

        return MongoClients.create(settings);
    }
}
