package com.umg.api.compiler;

import com.umg.api.compiler.dto.*;
import com.umg.application.compiler.DialectAnalysisRouter;
import com.umg.application.compiler.LexicalSyntaxAnalysisService;
import com.umg.model.dialect.CompilerDialect;
import com.umg.model.dialect.DialectMapper;
import com.umg.model.dialect.SqlDialect;
import com.umg.model.semantic.config.ConexionBaseDatosConfig;
import com.umg.model.semantic.metadata.JdbcConnectionFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(
    name = "Compiler API",
    description = "Endpoints para analisis lexico, sintactico y semantico de sentencias SQL y NoSQL."
)
public class CompilerController {

    private final DialectAnalysisRouter router;
    private final LexicalSyntaxAnalysisService analysisService;

    public CompilerController(DialectAnalysisRouter router, LexicalSyntaxAnalysisService analysisService) {
        this.router = router;
        this.analysisService = analysisService;
    }

    @Operation(
        summary = "Verificar estado del servicio",
        description = "Retorna el estado basico de disponibilidad de la API del compilador."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Servicio disponible"
        )
    })
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "compiler-api",
            "version", "1.0.0"
        ));
    }

    @Operation(
        summary = "Listar dialectos soportados",
        description = "Retorna los motores SQL y NoSQL soportados por el compilador."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Listado de dialectos retornado correctamente"
        )
    })
    @GetMapping("/dialects")
    public ResponseEntity<Map<String, Object>> dialects() {
        return ResponseEntity.ok(Map.of(
            "supportedDialects", List.of("MYSQL", "POSTGRESQL", "SQL_SERVER", "MONGODB", "CASSANDRA_CQL"),
            "sqlDialects", List.of("MYSQL", "POSTGRESQL", "SQL_SERVER"),
            "noSqlDialects", List.of("MONGODB", "CASSANDRA_CQL"),
            "futureDialects", List.of()
        ));
    }

    @Operation(
        summary = "Analizar sentencia SQL/NoSQL a nivel lexico y sintactico",
        description = """
                Ejecuta el analizador lexico y sintactico sobre una sentencia SQL o instruccion NoSQL.

                No requiere conexion a base de datos.
                No ejecuta analisis semantico.
                No valida existencia real de tablas, columnas, colecciones ni campos.

                El campo 'sql' se conserva por compatibilidad con el frontend, pero para dialectos NoSQL
                representa la consulta o instruccion NoSQL enviada.
                """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Analisis procesado correctamente. La sentencia puede ser valida o invalida segun el campo valid."
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Request invalido"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno no controlado"
        )
    })
    @PostMapping("/analyze/lexical-syntax")
    public ResponseEntity<CompilerAnalyzeResponse> analyzeLexicalSyntax(
            @Valid @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Solicitud para analisis lexico y sintactico",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CompilerAnalyzeRequest.class),
                    examples = {
                        @ExampleObject(
                            name = "Consulta SELECT MySQL",
                            summary = "Analisis lexico/sintactico de SELECT",
                            value = """
                                    {
                                      "dialect": "MYSQL",
                                      "sql": "SELECT id, nombre FROM clientes WHERE estado = 1;",
                                      "analysisMode": "LEXICAL_SYNTAX",
                                      "options": {
                                        "includeCommentsAsTokens": true,
                                        "stopOnLexicalError": true,
                                        "stopOnSyntaxError": true,
                                        "returnTokenList": true,
                                        "returnConsoleOutput": true
                                      }
                                    }
                                    """
                        ),
                        @ExampleObject(
                            name = "MongoDB find",
                            summary = "Analisis lexico/sintactico de MongoDB find",
                            value = """
                                    {
                                      "dialect": "MONGODB",
                                      "sql": "db.clientes.find({ estado: 1 })",
                                      "analysisMode": "LEXICAL_SYNTAX"
                                    }
                                    """
                        ),
                        @ExampleObject(
                            name = "MongoDB aggregate",
                            summary = "Analisis lexico/sintactico de MongoDB aggregate",
                            value = """
                                    {
                                      "dialect": "MONGODB",
                                      "sql": "db.pedidos.aggregate([{ $match: { estado: 'ACTIVO' } }, { $group: { _id: '$clienteId', total: { $sum: '$monto' } } }])",
                                      "analysisMode": "LEXICAL_SYNTAX"
                                    }
                                    """
                        ),
                        @ExampleObject(
                            name = "Cassandra SELECT",
                            summary = "Analisis lexico/sintactico de Cassandra SELECT",
                            value = """
                                    {
                                      "dialect": "CASSANDRA_CQL",
                                      "sql": "SELECT id, nombre FROM clientes WHERE estado = 1 ALLOW FILTERING;",
                                      "analysisMode": "LEXICAL_SYNTAX"
                                    }
                                    """
                        ),
                        @ExampleObject(
                            name = "Cassandra CREATE TABLE",
                            summary = "Analisis lexico/sintactico de Cassandra CREATE TABLE",
                            value = """
                                    {
                                      "dialect": "CASSANDRA_CQL",
                                      "sql": "CREATE TABLE clientes (id UUID PRIMARY KEY, nombre TEXT, estado INT);",
                                      "analysisMode": "LEXICAL_SYNTAX"
                                    }
                                    """
                        )
                    }
                )
            )
            CompilerAnalyzeRequest request) {
        return ResponseEntity.ok(router.route(request));
    }

    @Operation(
        summary = "Ejecutar analisis completo de sentencia SQL/NoSQL",
        description = """
                Ejecuta analisis lexico, sintactico y semantico.

                Para SQL: requiere datos de conexion a base de datos.
                Valida tablas, columnas, aliases y funciones segun el motor seleccionado.
                No ejecuta la sentencia SQL del usuario.

                Para NoSQL: el analisis semantico queda pendiente para una fase posterior.
                """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Analisis procesado correctamente. La sentencia puede ser valida o invalida segun el campo valid."
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Request invalido"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno no controlado"
        )
    })
    @PostMapping("/analyze/full")
    public ResponseEntity<CompilerAnalyzeResponse> analyzeFull(
            @Valid @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Solicitud para analisis completo",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CompilerAnalyzeRequest.class),
                    examples = {
                        @ExampleObject(
                            name = "Analisis completo MySQL",
                            summary = "Analisis lexico, sintactico y semantico con conexion a BD",
                            value = """
                                    {
                                      "dialect": "MYSQL",
                                      "sql": "SELECT c.id, c.nombre FROM clientes c WHERE c.estado = 1;",
                                      "analysisMode": "FULL",
                                      "connectionConfig": {
                                        "dialect": "MYSQL",
                                        "host": "localhost",
                                        "port": 3306,
                                        "database": "mi_base",
                                        "username": "root",
                                        "password": "********",
                                        "schema": "public"
                                      },
                                      "options": {
                                        "includeCommentsAsTokens": true,
                                        "stopOnLexicalError": true,
                                        "stopOnSyntaxError": true,
                                        "returnTokenList": true,
                                        "returnConsoleOutput": true
                                      }
                                    }
                                    """
                        )
                    }
                )
            )
            CompilerAnalyzeRequest request) {
        if (DialectMapper.isSql(request.getDialect())) {
            if (request.getAnalysisMode() == AnalysisMode.LEXICAL_ONLY
                || request.getAnalysisMode() == AnalysisMode.LEXICAL_SYNTAX) {
                request.setAnalysisMode(AnalysisMode.FULL);
            }
        }
        return ResponseEntity.ok(router.route(request));
    }

    @Operation(
        summary = "Validar conexion a base de datos",
        description = """
                Valida si los datos de conexion enviados permiten conectarse al motor seleccionado.

                No ejecuta analisis SQL.
                No ejecuta sentencias del usuario.
                No retorna la contrasena en la respuesta.
                """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Validacion procesada correctamente. La conexion puede ser valida o invalida segun el campo valid."
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Request invalido"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno no controlado"
        )
    })
    @PostMapping("/connection/test")
    public ResponseEntity<Map<String, Object>> testConnection(
            @Valid @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Configuracion de conexion a base de datos",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ConnectionConfigDto.class),
                    examples = {
                        @ExampleObject(
                            name = "Conexion MySQL",
                            summary = "Prueba de conexion a MySQL",
                            value = """
                                    {
                                      "dialect": "MYSQL",
                                      "host": "localhost",
                                      "port": 3306,
                                      "database": "mi_base",
                                      "username": "root",
                                      "password": "********",
                                      "schema": "public"
                                    }
                                    """
                        )
                    }
                )
            )
            ConnectionConfigDto connectionConfig) {

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
