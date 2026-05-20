# PROMPT 04 PARA AGENTE PROGRAMADOR
## Implementación de documentación Swagger/OpenAPI en Spring Boot

> **Proyecto:** Compilador SQL UMG  
> **Package raíz obligatorio:** `com.umg`  
> **JDK:** 17  
> **Gestor:** Maven  
> **Arquitectura actual:** Spring Boot REST API  
> **Ruta base API:** `/api/compiler`  
> **Objetivo:** Implementar documentación Swagger/OpenAPI profesional para los endpoints del compilador SQL.  
> **Documentos previos obligatorios:**  
> - `prompt_universal_migracion_spring_com_umg.md`
> - `prompt_02_migracion_lexico_sintactico_spring_com_umg.md`
> - `prompt_03_migracion_semantico_spring_com_umg.md`

---

# 1. Validación previa del proyecto recibido

Antes de implementar Swagger/OpenAPI, el agente debe confirmar que el proyecto mantiene esta estructura:

```text
src/main/java/com/umg/
├── CompiladoresRefactApplication.java
├── api/
│   └── compiler/
│       ├── CompilerController.java
│       ├── dto/
│       └── mapper/
├── application/
│   └── compiler/
├── config/
├── exception/
└── model/
    ├── ast/
    ├── dialect/
    ├── error/
    ├── lexer/
    ├── parser/
    └── semantic/
```

En la versión revisada existen archivos bajo:

```text
src/main/java/com/umg/api/compiler/
src/main/java/com/umg/application/compiler/
src/main/java/com/umg/config/
src/main/java/com/umg/exception/
src/main/java/com/umg/model/
```

No deben existir rutas bajo:

```text
src/main/java/com/dataquery/
src/main/java/com/umg/comdataquery/
src/main/java/com/umg/view/
```

Antes de finalizar, ejecutar búsqueda global:

```bash
grep -R "com.dataquery" src pom.xml
grep -R "com.umg.comdataquery" src pom.xml
grep -R "dataquery.sqlcompiler" src pom.xml
grep -R "javax.swing" src/main/java
grep -R "org.netbeans.lib.awtextra" src/main/java
```

Si aparece cualquiera de esas referencias, corregirlas o reportar por qué existen.

---

# 2. Objetivo de esta fase

Implementar documentación automática y navegable para la API REST del compilador SQL usando **springdoc-openapi** y Swagger UI.

Esta fase debe permitir que cualquier integrante del equipo pueda abrir una URL en el navegador y probar/documentar los endpoints del backend sin necesidad de Postman.

La documentación debe incluir:

- descripción general de la API;
- endpoints disponibles;
- request bodies;
- response bodies;
- ejemplos JSON;
- códigos HTTP;
- posibles errores;
- DTOs documentados;
- ocultamiento de datos sensibles como `password`;
- agrupación por tags;
- configuración personalizada de rutas Swagger.

---

# 3. Reglas obligatorias

## 3.1 Package raíz

Todo debe mantenerse bajo:

```java
package com.umg;
```

No debe existir ninguna referencia nueva ni residual a:

```java
com.dataquery.sqlcompiler
com.umg.comdataquery.sqlcompiler
dataquery.sqlcompiler
```

---

## 3.2 No modificar lógica de negocio

Esta fase es únicamente de documentación.

Permitido:

- agregar anotaciones Swagger/OpenAPI;
- agregar configuración OpenAPI;
- agregar dependencia Maven;
- documentar DTOs;
- agregar ejemplos JSON;
- agregar pruebas de documentación.

No permitido:

- cambiar reglas del lexer;
- cambiar reglas del parser;
- cambiar reglas del semántico;
- cambiar estructura funcional de respuestas;
- cambiar endpoints ya definidos;
- cambiar nombres de paquetes;
- reintroducir Swing;
- configurar un datasource fijo.

---

## 3.3 No exponer credenciales

Swagger/OpenAPI no debe mostrar `password` como parte de ninguna respuesta.

En request, `password` puede aparecer porque el usuario debe enviarlo para validar conexión, pero debe documentarse como campo sensible.

En el DTO de conexión, marcar `password` así:

```java
@Schema(
    description = "Contraseña de la base de datos. Solo se recibe en request; nunca se retorna en responses.",
    example = "********",
    accessMode = Schema.AccessMode.WRITE_ONLY
)
@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
private String password;
```

Si no está usando Jackson directamente en ese DTO, al menos usar `@Schema(accessMode = Schema.AccessMode.WRITE_ONLY)`.

---

# 4. Dependencia Maven requerida

Agregar al `pom.xml`:

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.8.17</version>
</dependency>
```

Reglas:

- Usar `springdoc-openapi-starter-webmvc-ui`, porque el proyecto usa Spring Web MVC.
- No usar Springfox.
- No usar dependencias antiguas de Swagger 2.
- No agregar Spring Security solo para Swagger.
- No agregar WebFlux.

---

# 5. Configuración de rutas Swagger

Actualizar:

```text
src/main/resources/application.properties
```

Agregar:

```properties
# Swagger / OpenAPI
springdoc.swagger-ui.path=/api/compiler/docs
springdoc.api-docs.path=/api/compiler/openapi
springdoc.swagger-ui.operations-sorter=method
springdoc.swagger-ui.tags-sorter=alpha
springdoc.packages-to-scan=com.umg.api.compiler
springdoc.paths-to-match=/api/compiler/**
```

Con esto, las rutas esperadas serán:

```text
Swagger UI:
http://localhost:8080/api/compiler/docs

OpenAPI JSON:
http://localhost:8080/api/compiler/openapi

OpenAPI YAML:
http://localhost:8080/api/compiler/openapi.yaml
```

Si Swagger UI redirige a `/api/compiler/docs/swagger-ui/index.html` o equivalente, documentar la URL final que funcione en `docs/SWAGGER_OPENAPI.md`.

---

# 6. Clase de configuración OpenAPI

Crear:

```text
src/main/java/com/umg/config/OpenApiConfig.java
```

Contenido base esperado:

```java
package com.umg.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI compilerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("UMG SQL Compiler API")
                        .description("""
                                API REST para análisis de sentencias SQL.

                                Soporta análisis léxico, sintáctico y semántico.
                                Motores SQL soportados inicialmente:
                                - MySQL
                                - PostgreSQL
                                - SQL Server

                                Preparado arquitectónicamente para futura expansión a motores NoSQL.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Equipo Compiladores UMG"))
                        .license(new License()
                                .name("Uso académico")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Servidor local de desarrollo")
                ))
                .externalDocs(new ExternalDocumentation()
                        .description("Documentación interna del proyecto")
                        .url("/docs/API_COMPILER.md"));
    }
}
```

Notas:

- No colocar URLs externas falsas.
- No colocar correos personales si el equipo no los autorizó.
- Mantener el título y descripción relacionados con el proyecto.

---

# 7. Controller que debe documentarse

Actualizar:

```text
src/main/java/com/umg/api/compiler/CompilerController.java
```

Agregar anotación a nivel clase:

```java
@Tag(
    name = "Compiler API",
    description = "Endpoints para análisis léxico, sintáctico y semántico de sentencias SQL."
)
```

Ejemplo:

```java
@RestController
@RequestMapping("/api/compiler")
@Tag(
    name = "Compiler API",
    description = "Endpoints para análisis léxico, sintáctico y semántico de sentencias SQL."
)
public class CompilerController {
    // endpoints
}
```

---

# 8. Endpoints que deben quedar documentados

Documentar todos los endpoints existentes del proyecto.

Como mínimo, si ya existen en `CompilerController`:

```http
GET /api/compiler/health
GET /api/compiler/dialects
POST /api/compiler/analyze/lexical-syntax
POST /api/compiler/connection/validate
POST /api/compiler/analyze/full
```

Regla importante:

- Si algún endpoint todavía no existe en la rama actual, no inventarlo solo para Swagger.
- Si el endpoint corresponde a una fase ya implementada, documentarlo.
- Si no está implementado todavía, documentarlo únicamente en `docs/API_COMPILER.md` como pendiente, no en Swagger.

---

# 9. Documentación del endpoint health

Para:

```http
GET /api/compiler/health
```

Agregar:

```java
@Operation(
    summary = "Verificar estado del servicio",
    description = "Retorna el estado básico de disponibilidad de la API del compilador."
)
@ApiResponses(value = {
    @ApiResponse(
        responseCode = "200",
        description = "Servicio disponible"
    )
})
```

La respuesta debe describir algo similar a:

```json
{
  "status": "UP",
  "service": "compiler-api",
  "version": "1.0.0"
}
```

---

# 10. Documentación del endpoint dialects

Para:

```http
GET /api/compiler/dialects
```

Agregar:

```java
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
```

Debe documentar:

```json
{
  "supportedDialects": [
    "MYSQL",
    "POSTGRESQL",
    "SQL_SERVER"
  ],
  "futureDialects": [
    "MONGODB"
  ]
}
```

---

# 11. Documentación del endpoint léxico/sintáctico

Para:

```http
POST /api/compiler/analyze/lexical-syntax
```

Agregar:

```java
@Operation(
    summary = "Analizar sentencia SQL a nivel léxico y sintáctico",
    description = """
            Ejecuta el analizador léxico y sintáctico sobre una sentencia SQL.

            No requiere conexión a base de datos.
            No ejecuta análisis semántico.
            No valida existencia real de tablas ni columnas.
            """
)
@ApiResponses(value = {
    @ApiResponse(
        responseCode = "200",
        description = "Análisis procesado correctamente. La sentencia puede ser válida o inválida según el campo valid."
    ),
    @ApiResponse(
        responseCode = "400",
        description = "Request inválido"
    ),
    @ApiResponse(
        responseCode = "500",
        description = "Error interno no controlado"
    )
})
```

Agregar ejemplo de request:

```json
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
```

---

# 12. Documentación del endpoint de conexión

Para:

```http
POST /api/compiler/connection/validate
```

Agregar:

```java
@Operation(
    summary = "Validar conexión a base de datos",
    description = """
            Valida si los datos de conexión enviados permiten conectarse al motor seleccionado.

            No ejecuta análisis SQL.
            No ejecuta sentencias del usuario.
            No retorna la contraseña en la respuesta.
            """
)
@ApiResponses(value = {
    @ApiResponse(
        responseCode = "200",
        description = "Validación procesada correctamente. La conexión puede ser válida o inválida según el campo valid."
    ),
    @ApiResponse(
        responseCode = "400",
        description = "Request inválido"
    ),
    @ApiResponse(
        responseCode = "500",
        description = "Error interno no controlado"
    )
})
```

Ejemplo de request:

```json
{
  "dialect": "MYSQL",
  "connection": {
    "host": "localhost",
    "port": 3306,
    "database": "mi_base",
    "username": "root",
    "password": "123456",
    "schema": null
  }
}
```

Ejemplo de response:

```json
{
  "requestId": "uuid",
  "dialect": "MYSQL",
  "valid": true,
  "message": "Conexión validada correctamente.",
  "executionStatus": "SUCCESS",
  "connectionResult": {
    "connected": true,
    "dialect": "MYSQL",
    "database": "mi_base",
    "schema": null,
    "driver": "MySQL Connector/J",
    "jdbcUrlMasked": "jdbc:mysql://localhost:3306/mi_base",
    "message": "Conexión establecida correctamente."
  },
  "errors": [],
  "console": [
    "[INFO] Validando conexión para MYSQL.",
    "[SUCCESS] Conexión establecida correctamente."
  ]
}
```

---

# 13. Documentación del endpoint full

Para:

```http
POST /api/compiler/analyze/full
```

Agregar:

```java
@Operation(
    summary = "Ejecutar análisis completo de sentencia SQL",
    description = """
            Ejecuta análisis léxico, sintáctico y semántico.

            Requiere datos de conexión a base de datos.
            Valida tablas, columnas, aliases y funciones según el motor seleccionado.
            No ejecuta la sentencia SQL del usuario.
            Utiliza metadatos JDBC para la validación semántica.
            """
)
@ApiResponses(value = {
    @ApiResponse(
        responseCode = "200",
        description = "Análisis procesado correctamente. La sentencia puede ser válida o inválida según el campo valid."
    ),
    @ApiResponse(
        responseCode = "400",
        description = "Request inválido"
    ),
    @ApiResponse(
        responseCode = "500",
        description = "Error interno no controlado"
    )
})
```

Ejemplo de request:

```json
{
  "dialect": "MYSQL",
  "sql": "SELECT c.id, c.nombre FROM clientes c WHERE c.estado = 1;",
  "analysisMode": "FULL",
  "connection": {
    "host": "localhost",
    "port": 3306,
    "database": "mi_base",
    "username": "root",
    "password": "123456",
    "schema": null
  },
  "options": {
    "includeCommentsAsTokens": true,
    "validateSemantic": true,
    "stopOnLexicalError": true,
    "stopOnSyntaxError": true,
    "returnTokenList": true,
    "returnConsoleOutput": true
  }
}
```

---

# 14. Uso de anotaciones OpenAPI en métodos

Usar estas anotaciones donde corresponda:

```java
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
```

Ejemplo para request body:

```java
@io.swagger.v3.oas.annotations.parameters.RequestBody(
    description = "Solicitud para análisis léxico y sintáctico",
    required = true,
    content = @Content(
        mediaType = "application/json",
        schema = @Schema(implementation = CompilerAnalyzeRequest.class),
        examples = {
            @ExampleObject(
                name = "Consulta SELECT MySQL",
                summary = "Análisis léxico/sintáctico de SELECT",
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
```

---

# 15. Documentación de DTOs

Agregar `@Schema` a los DTOs principales:

```text
CompilerAnalyzeRequest
CompilerAnalyzeResponse
CompilerOptionsRequest
CompilerSummaryDto
ConnectionConfigDto
CompilerErrorDto
LexicalResultDto
SyntaxResultDto
SemanticResultDto
TokenDto
```

Si existen otros DTOs de conexión o semántico, documentarlos también.

No cambiar nombres de DTOs solo por documentación.

---

# 16. DTO CompilerAnalyzeRequest

Agregar descripciones y ejemplos.

Ejemplo orientativo:

```java
@Schema(description = "Request principal para análisis SQL.")
public class CompilerAnalyzeRequest {

    @Schema(
        description = "Identificador opcional enviado por el frontend para trazabilidad.",
        example = "REQ-001"
    )
    private String requestId;

    @Schema(
        description = "Motor SQL seleccionado.",
        example = "MYSQL",
        allowableValues = {"MYSQL", "POSTGRESQL", "SQL_SERVER"}
    )
    private SqlDialect dialect;

    @Schema(
        description = "Sentencia SQL que será analizada. El backend no ejecuta esta sentencia.",
        example = "SELECT id, nombre FROM clientes WHERE estado = 1;"
    )
    private String sql;

    @Schema(
        description = "Modo de análisis solicitado.",
        example = "LEXICAL_SYNTAX",
        allowableValues = {"LEXICAL_ONLY", "LEXICAL_SYNTAX", "FULL"}
    )
    private AnalysisMode analysisMode;

    @Schema(description = "Datos de conexión requeridos solo para análisis semántico o full.")
    private ConnectionConfigDto connection;

    @Schema(description = "Opciones de ejecución del analizador.")
    private CompilerOptionsRequest options;
}
```

Adaptar tipos reales según el código actual.

No inventar DTOs si ya existen con otros nombres.

---

# 17. DTO ConnectionConfigDto

Documentar campos:

```java
@Schema(description = "Configuración de conexión dinámica a base de datos.")
public class ConnectionConfigDto {

    @Schema(description = "Host o IP del servidor de base de datos.", example = "localhost")
    private String host;

    @Schema(description = "Puerto del motor de base de datos.", example = "3306")
    private Integer port;

    @Schema(description = "Nombre de la base de datos.", example = "mi_base")
    private String database;

    @Schema(description = "Usuario de conexión.", example = "root")
    private String username;

    @Schema(
        description = "Contraseña de conexión. Campo sensible, solo permitido en request.",
        example = "********",
        accessMode = Schema.AccessMode.WRITE_ONLY
    )
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Schema(description = "Schema opcional. En PostgreSQL puede ser public; en SQL Server puede ser dbo.", example = "public")
    private String schema;
}
```

Si el DTO tiene otro nombre, aplicar la misma lógica.

---

# 18. DTO CompilerAnalyzeResponse

Documentar:

```java
@Schema(description = "Respuesta unificada del compilador SQL.")
public class CompilerAnalyzeResponse {
    // campos
}
```

Cada campo debe tener `@Schema`.

Ejemplo:

```java
@Schema(description = "Indica si la sentencia fue válida según el modo de análisis ejecutado.", example = "true")
private boolean valid;

@Schema(description = "Estado técnico de ejecución.", example = "SUCCESS")
private ExecutionStatus executionStatus;

@Schema(description = "Resultado del análisis léxico.")
private LexicalResultDto lexicalResult;

@Schema(description = "Resultado del análisis sintáctico.")
private SyntaxResultDto syntaxResult;

@Schema(description = "Resultado del análisis semántico. Será null cuando no aplique.")
private SemanticResultDto semanticResult;
```

---

# 19. DTO CompilerErrorDto

Documentar:

```java
@Schema(description = "Error detectado durante una fase del compilador.")
public class CompilerErrorDto {

    @Schema(description = "Fase donde ocurrió el error.", example = "SYNTAX")
    private String stage;

    @Schema(description = "Código interno del error.", example = "EXPECTED_IDENTIFIER")
    private String code;

    @Schema(description = "Mensaje legible del error.", example = "Se esperaba un identificador después de SELECT.")
    private String message;

    @Schema(description = "Línea donde ocurrió el error, si está disponible.", example = "1")
    private Integer line;

    @Schema(description = "Columna donde ocurrió el error, si está disponible.", example = "8")
    private Integer column;

    @Schema(description = "Lexema relacionado con el error.", example = "FROM")
    private String lexeme;

    @Schema(description = "Severidad del error.", example = "ERROR")
    private String severity;
}
```

---

# 20. Enums

Documentar enums con `@Schema` si aplica:

```text
AnalysisMode
ExecutionStatus
SqlDialect
```

Ejemplo:

```java
@Schema(description = "Modo de análisis solicitado para el compilador.")
public enum AnalysisMode {
    LEXICAL_ONLY,
    LEXICAL_SYNTAX,
    SEMANTIC_ONLY,
    FULL
}
```

Si `SEMANTIC_ONLY` existe pero no se usa aún, documentar que puede estar reservado.

---

# 21. Agrupación por tags

Usar un solo tag principal en esta fase:

```text
Compiler API
```

Opcionalmente, si el controller ya está muy dividido, usar:

```text
Compiler Health
Compiler Analysis
Compiler Connection
```

Recomendación para este proyecto:

```text
Compiler API
```

---

# 22. Seguridad de Swagger

No agregar autenticación en esta fase.

No agregar Spring Security.

Si en el futuro se agrega seguridad, documentarla después con:

```text
@SecurityScheme
@SecurityRequirement
```

Por ahora, Swagger debe funcionar libremente en local.

---

# 23. Configuración CORS

No modificar CORS salvo que Swagger UI tenga problemas para consumir endpoints.

Como Swagger UI corre dentro de la misma aplicación, normalmente no necesita cambios de CORS.

No cambiar `WebConfig` si no es necesario.

---

# 24. Pruebas obligatorias

Antes de entregar, ejecutar:

```bash
mvn clean test
```

Si falla, corregir antes de finalizar.

---

# 25. Tests mínimos requeridos

Crear o actualizar tests para comprobar Swagger/OpenAPI.

Ubicación sugerida:

```text
src/test/java/com/umg/api/compiler/OpenApiDocumentationTest.java
```

## 25.1 Verificar que OpenAPI JSON responde

Probar:

```http
GET /api/compiler/openapi
```

Debe responder:

```text
HTTP 200
Content-Type application/json compatible
```

El JSON debe contener:

```text
openapi
paths
/api/compiler
```

---

## 25.2 Verificar que Swagger UI responde

Probar:

```http
GET /api/compiler/docs
```

Puede responder:

```text
HTTP 200
```

o redirección:

```text
HTTP 3xx
```

Aceptar ambos si la configuración de springdoc redirige internamente.

---

## 25.3 Verificar endpoints documentados

El JSON OpenAPI debe contener, si existen en el controller:

```text
/api/compiler/health
/api/compiler/dialects
/api/compiler/analyze/lexical-syntax
/api/compiler/connection/validate
/api/compiler/analyze/full
```

No fallar por endpoints que aún no existan si la fase correspondiente todavía no fue aplicada, pero si existen en controller deben aparecer en OpenAPI.

---

## 25.4 Verificar que password no aparece como respuesta

El schema del password debe marcarse como:

```text
writeOnly: true
```

No debe aparecer como campo de response.

---

# 26. Documentación Markdown obligatoria

Crear:

```text
docs/SWAGGER_OPENAPI.md
```

Debe incluir:

1. Qué es Swagger/OpenAPI en el proyecto.
2. Dependencia agregada.
3. Ruta de Swagger UI.
4. Ruta de OpenAPI JSON.
5. Ruta de OpenAPI YAML.
6. Endpoints documentados.
7. Cómo probar desde navegador.
8. Cómo probar desde curl.
9. Nota de seguridad sobre password.
10. Nota de que Swagger no cambia la lógica del compilador.

Ejemplo mínimo:

```markdown
# Swagger / OpenAPI

## Swagger UI

Abrir:

http://localhost:8080/api/compiler/docs

## OpenAPI JSON

http://localhost:8080/api/compiler/openapi

## OpenAPI YAML

http://localhost:8080/api/compiler/openapi.yaml
```

Actualizar también:

```text
docs/API_COMPILER.md
```

Agregar sección:

```markdown
## Documentación Swagger/OpenAPI

La documentación interactiva está disponible en:

- Swagger UI: `http://localhost:8080/api/compiler/docs`
- OpenAPI JSON: `http://localhost:8080/api/compiler/openapi`
- OpenAPI YAML: `http://localhost:8080/api/compiler/openapi.yaml`
```

Actualizar:

```text
docs/SPRING_MIGRATION.md
```

Agregar que se implementó documentación Swagger/OpenAPI como fase adicional.

---

# 27. Criterios de aceptación

La implementación se considera correcta si:

1. El proyecto sigue usando package raíz `com.umg`.
2. No existe `com.dataquery`.
3. No se reintrodujo Swing.
4. El `pom.xml` incluye `springdoc-openapi-starter-webmvc-ui`.
5. Existe `OpenApiConfig`.
6. Swagger UI funciona en `/api/compiler/docs`.
7. OpenAPI JSON funciona en `/api/compiler/openapi`.
8. OpenAPI YAML funciona en `/api/compiler/openapi.yaml`.
9. `CompilerController` está documentado con `@Tag`, `@Operation` y `@ApiResponses`.
10. Los DTOs principales tienen `@Schema`.
11. Los request bodies tienen ejemplos claros.
12. Los response codes están documentados.
13. El campo `password` está marcado como `WRITE_ONLY`.
14. No se cambió la lógica del compilador.
15. No se configuró un datasource fijo.
16. No se agregó Spring Security innecesariamente.
17. Se actualizó `docs/SWAGGER_OPENAPI.md`.
18. Se actualizó `docs/API_COMPILER.md`.
19. Se ejecutó `mvn clean test`.
20. Las pruebas pasan.

---

# 28. Orden recomendado de trabajo

Ejecutar en este orden:

1. Revisar `pom.xml`.
2. Revisar `CompilerController.java`.
3. Revisar DTOs actuales en `com.umg.api.compiler.dto`.
4. Agregar dependencia `springdoc-openapi-starter-webmvc-ui`.
5. Configurar rutas en `application.properties`.
6. Crear `OpenApiConfig`.
7. Agregar `@Tag` al controller.
8. Agregar `@Operation` y `@ApiResponses` a cada endpoint.
9. Agregar ejemplos JSON a request bodies.
10. Agregar `@Schema` a DTOs.
11. Marcar `password` como `WRITE_ONLY`.
12. Crear tests de OpenAPI.
13. Crear `docs/SWAGGER_OPENAPI.md`.
14. Actualizar `docs/API_COMPILER.md`.
15. Actualizar `docs/SPRING_MIGRATION.md`.
16. Ejecutar `mvn clean test`.
17. Corregir fallos.
18. Verificar manualmente Swagger UI en navegador.
19. Entregar resumen técnico.

---

# 29. Resumen final para el agente

Implementa documentación Swagger/OpenAPI para la API Spring Boot del compilador SQL.

Usa:

```xml
org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.17
```

Mantén todo bajo:

```java
com.umg
```

Documenta los endpoints bajo:

```http
/api/compiler
```

Especialmente:

```http
GET /api/compiler/health
GET /api/compiler/dialects
POST /api/compiler/analyze/lexical-syntax
POST /api/compiler/connection/validate
POST /api/compiler/analyze/full
```

La documentación interactiva debe quedar en:

```http
http://localhost:8080/api/compiler/docs
```

El JSON OpenAPI debe quedar en:

```http
http://localhost:8080/api/compiler/openapi
```

No cambies lógica del compilador.

No expongas password.

No reintroduzcas Swing.

No agregues Spring Security.

Antes de finalizar, ejecutar:

```bash
mvn clean test
```

Si falla la compilación o los tests, no entregar como completado.
