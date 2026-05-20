package com.umg.api.compiler;

import com.umg.api.compiler.dto.*;
import com.umg.application.compiler.LexicalSyntaxAnalysisService;
import com.umg.model.dialect.SqlDialect;
import com.umg.model.semantic.config.ConexionBaseDatosConfig;
import com.umg.model.semantic.metadata.JdbcConnectionFactory;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/compiler")
public class CompilerController {

    private final LexicalSyntaxAnalysisService analysisService;

    public CompilerController(LexicalSyntaxAnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "compiler-api",
            "version", "1.0.0"
        ));
    }

    @GetMapping("/dialects")
    public ResponseEntity<Map<String, Object>> dialects() {
        return ResponseEntity.ok(Map.of(
            "supportedDialects", List.of("MYSQL", "POSTGRESQL", "SQL_SERVER"),
            "futureDialects", List.of("MONGODB")
        ));
    }

    @PostMapping("/analyze/lexical-syntax")
    public ResponseEntity<CompilerAnalyzeResponse> analyzeLexicalSyntax(
            @Valid @RequestBody CompilerAnalyzeRequest request) {
        return ResponseEntity.ok(analysisService.analyze(request));
    }

    @PostMapping("/analyze/full")
    public ResponseEntity<CompilerAnalyzeResponse> analyzeFull(
            @Valid @RequestBody CompilerAnalyzeRequest request) {
        if (request.getAnalysisMode() == AnalysisMode.LEXICAL_ONLY
            || request.getAnalysisMode() == AnalysisMode.LEXICAL_SYNTAX) {
            request.setAnalysisMode(AnalysisMode.FULL);
        }
        return ResponseEntity.ok(analysisService.analyze(request));
    }

    @PostMapping("/connection/test")
    public ResponseEntity<Map<String, Object>> testConnection(
            @Valid @RequestBody ConnectionConfigDto connectionConfig) {

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("valid", false);

        if (connectionConfig == null) {
            result.put("message", "Configuracion de conexion no proporcionada.");
            result.put("status", "INVALID_CONFIG");
            return ResponseEntity.badRequest().body(result);
        }

        SqlDialect dialectToUse = connectionConfig.getDialect();
        if (dialectToUse == null) {
            result.put("message", "Dialecto de base de datos no especificado.");
            result.put("status", "INVALID_CONFIG");
            return ResponseEntity.badRequest().body(result);
        }

        ConexionBaseDatosConfig config = new ConexionBaseDatosConfig();
        config.setDialecto(dialectToUse);
        config.setHost(connectionConfig.getHost());
        config.setPuerto(connectionConfig.getPort() != null ? connectionConfig.getPort() : 0);
        config.setBaseDatos(connectionConfig.getDatabase());
        config.setEsquema(connectionConfig.getSchema());
        config.setUsuario(connectionConfig.getUsername());
        config.setPassword(connectionConfig.getPassword());
        config.setUrlJdbc(connectionConfig.getJdbcUrl());
        config.setUsarUrlJdbcDirecta(connectionConfig.getUseDirectJdbcUrl() != null
            ? connectionConfig.getUseDirectJdbcUrl() : false);

        if (!config.esValida()) {
            result.put("message", "Configuracion de conexion invalida. Verifique los campos obligatorios.");
            result.put("status", "INVALID_CONFIG");
            return ResponseEntity.badRequest().body(result);
        }

        JdbcConnectionFactory factory = new JdbcConnectionFactory();
        try (Connection conexion = factory.crearConexion(config)) {
            boolean isValid = conexion.isValid(10);
            result.put("valid", isValid);
            result.put("message", isValid ? "Conexion exitosa a la base de datos." : "La conexion no respondio correctamente.");
            result.put("status", isValid ? "SUCCESS" : "CONNECTION_FAILED");
            result.put("dialect", dialectToUse.name());
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

        return ResponseEntity.ok(result);
    }
}
