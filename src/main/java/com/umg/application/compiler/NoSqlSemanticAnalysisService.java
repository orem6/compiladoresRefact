package com.umg.application.compiler;

import com.umg.api.compiler.dto.CompilerAnalyzeRequest;
import com.umg.api.compiler.dto.SemanticResultDto;
import com.umg.api.compiler.mapper.CompilerResponseMapper;
import com.umg.model.dialect.CompilerDialect;
import com.umg.model.semantic.AnalizadorCql;
import com.umg.model.semantic.AnalizadorMongo;
import com.umg.model.semantic.config.ConexionBaseDatosConfig;
import com.umg.model.semantic.result.ResultadoSemantico;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class NoSqlSemanticAnalysisService {

    private final CompilerResponseMapper mapper;

    public NoSqlSemanticAnalysisService(CompilerResponseMapper mapper) {
        this.mapper = mapper;
    }

    public SemanticResultDto analyze(CompilerAnalyzeRequest request) {
        ConexionBaseDatosConfig config = buildNoSqlConfig(request);
        if (config == null || !config.esValida()) {
            SemanticResultDto invalid = new SemanticResultDto();
            invalid.setValid(false);
            invalid.setMessage("Configuracion de conexion invalida para analisis semantico NoSQL.");
            return invalid;
        }

        ResultadoSemantico resultado;
        if (request.getDialect() == CompilerDialect.MONGODB) {
            resultado = new AnalizadorMongo().analizar(request.getSql(), config);
        } else if (request.getDialect() == CompilerDialect.CASSANDRA_CQL) {
            resultado = new AnalizadorCql().analizar(request.getSql(), config);
        } else {
            SemanticResultDto unsupported = new SemanticResultDto();
            unsupported.setValid(false);
            unsupported.setMessage("Dialecto no soportado para analisis semantico NoSQL.");
            return unsupported;
        }

        SemanticResultDto dto = mapper.toSemanticResultDto(resultado);
        if (dto.getWarnings() == null) {
            dto.setWarnings(new ArrayList<>());
        }

        if (request.getDialect() == CompilerDialect.MONGODB
            && request.getSql() != null
            && request.getSql().trim().startsWith("[")
            && (request.getTargetCollection() == null || request.getTargetCollection().isBlank())) {
            dto.getWarnings().add("No se proporciono coleccion objetivo; no fue posible validar existencia de campos.");
        }

        Map<String, Object> validatedObjects = dto.getValidatedObjects();
        if (validatedObjects == null) {
            validatedObjects = new LinkedHashMap<>();
        }
        if (request.getDialect() == CompilerDialect.MONGODB) {
            List<Map<String, Object>> databases = new ArrayList<>();
            Map<String, Object> db = new LinkedHashMap<>();
            db.put("name", config.getBaseDatos());
            db.put("exists", true);
            db.put("message", "Base de datos accesible.");
            databases.add(db);
            validatedObjects.put("databases", databases);
        } else {
            List<Map<String, Object>> keyspaces = new ArrayList<>();
            Map<String, Object> ks = new LinkedHashMap<>();
            ks.put("name", config.getBaseDatos());
            ks.put("exists", true);
            ks.put("message", "Keyspace accesible.");
            keyspaces.add(ks);
            validatedObjects.put("keyspaces", keyspaces);
        }
        dto.setValidatedObjects(validatedObjects);
        return dto;
    }

    private ConexionBaseDatosConfig buildNoSqlConfig(CompilerAnalyzeRequest request) {
        com.umg.api.compiler.dto.ConnectionConfigDto dto = request.getConnectionConfig();
        if (dto == null) return null;
        ConexionBaseDatosConfig config = new ConexionBaseDatosConfig();
        config.setDialecto(
            request.getDialect() == CompilerDialect.CASSANDRA_CQL
                ? com.umg.model.dialect.SqlDialect.CASSANDRA
                : com.umg.model.dialect.SqlDialect.MONGODB
        );
        config.setHost(dto.getHost());
        config.setPuerto(dto.getPort() != null ? dto.getPort() : 0);
        config.setBaseDatos(dto.getDatabase());
        config.setEsquema(dto.getSchema());
        config.setUsuario(dto.getUsername());
        config.setPassword(dto.getPassword());
        config.setUrlJdbc(dto.getJdbcUrl());
        config.setUsarUrlJdbcDirecta(dto.getUseDirectJdbcUrl() != null ? dto.getUseDirectJdbcUrl() : false);
        config.setLocalDatacenter(dto.getLocalDatacenter());
        return config;
    }
}
