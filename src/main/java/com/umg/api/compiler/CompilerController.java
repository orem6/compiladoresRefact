package com.umg.api.compiler;

import com.umg.api.compiler.dto.*;
import com.umg.application.compiler.ConnectionValidationService;
import com.umg.application.compiler.DialectAnalysisRouter;
import com.umg.model.dialect.SqlDialect;
import com.umg.model.semantic.config.ConexionBaseDatosConfig;
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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/compiler")
@Tag(
    name = "Compiler API",
    description = "Endpoints para analisis lexico, sintactico y semantico de sentencias SQL, CQL y MongoDB."
)
public class CompilerController {

    private final DialectAnalysisRouter analysisRouter;
    private final ConnectionValidationService connectionValidationService;

    public CompilerController(DialectAnalysisRouter analysisRouter,
                               ConnectionValidationService connectionValidationService) {
        this.analysisRouter = analysisRouter;
        this.connectionValidationService = connectionValidationService;
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
        description = "Retorna los motores SQL soportados por el compilador y los dialectos planeados para futuras versiones."
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
            "supportedDialects", List.of("MYSQL", "POSTGRESQL", "SQL_SERVER"),
            "futureDialects", List.of("MONGODB", "CASSANDRA")
        ));
    }

    @Operation(
        summary = "Analizar sentencia SQL a nivel lexico y sintactico",
        description = """
                Ejecuta el analizador lexico y sintactico sobre una sentencia SQL.

                No requiere conexion a base de datos.
                No ejecuta analisis semantico.
                No valida existencia real de tablas ni columnas.
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
                        )
                    }
                )
            )
            CompilerAnalyzeRequest request) {
        if (request.getAnalysisMode() == AnalysisMode.SEMANTIC_ONLY
            || request.getAnalysisMode() == AnalysisMode.FULL) {
            request.setAnalysisMode(AnalysisMode.LEXICAL_SYNTAX);
        }
        return ResponseEntity.ok(analysisRouter.route(request));
    }

    @Operation(
        summary = "Ejecutar analisis completo de sentencia SQL",
        description = """
                Ejecuta analisis lexico, sintactico y semantico.

                Requiere datos de conexion a base de datos.
                Valida tablas, columnas, aliases y funciones segun el motor seleccionado.
                No ejecuta la sentencia SQL del usuario.
                Utiliza metadatos JDBC para la validacion semantica.
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
        if (request.getAnalysisMode() == AnalysisMode.LEXICAL_ONLY
            || request.getAnalysisMode() == AnalysisMode.LEXICAL_SYNTAX) {
            request.setAnalysisMode(AnalysisMode.FULL);
        }
        if (request.getDialect() == com.umg.model.dialect.CompilerDialect.MONGODB
            || request.getDialect() == com.umg.model.dialect.CompilerDialect.CASSANDRA_CQL) {
            CompilerAnalyzeResponse resp = new CompilerAnalyzeResponse();
            resp.setRequestId(request.getRequestId());
            resp.setDialect(request.getDialect());
            resp.setAnalysisMode(request.getAnalysisMode());
            resp.setValid(false);
            resp.setMessage("FULL mode no esta soportado para dialectos NoSQL.");
            resp.setExecutionStatus(ExecutionStatus.UNSUPPORTED_DIALECT);
            return ResponseEntity.badRequest().body(resp);
        }
        return ResponseEntity.ok(analysisRouter.route(request));
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
    @PostMapping("/connection/validate")
    public ResponseEntity<Map<String, Object>> validateConnection(
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

        Map<String, Object> connectionResult = connectionValidationService.testConnection(dialectToUse, config);
        return ResponseEntity.ok(connectionResult);
    }

    @Operation(
        summary = "[Deprecado] Validar conexion a base de datos",
        description = """
                Endpoint deprecado. Use /api/compiler/connection/validate en su lugar.

                Valida si los datos de conexion enviados permiten conectarse al motor seleccionado.
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
    @Deprecated
    public ResponseEntity<Map<String, Object>> testConnection(
            @Valid @RequestBody ConnectionConfigDto connectionConfig) {
        return validateConnection(connectionConfig);
    }
}
