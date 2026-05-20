# PROMPT 02 PARA AGENTE PROGRAMADOR  
## Migración del analizador léxico/sintáctico a Spring Boot REST API

> **Proyecto:** Compilador SQL UMG  
> **Package raíz obligatorio:** `com.umg`  
> **JDK:** 17  
> **Gestor:** Maven  
> **Ruta base API:** `/api/compiler`  
> **Fase:** Migración del analizador léxico/sintáctico  
> **Alcance:** Lexer + Parser + respuesta JSON REST  
> **Fuera de alcance:** conexión a base de datos, JDBC, validación semántica, existencia de tablas/columnas  
> **Documento anterior obligatorio:** `prompt_universal_migracion_spring_com_umg.md`

---

# 1. Objetivo de esta fase

Implementar la migración del análisis léxico y sintáctico del proyecto actual hacia una API REST con Spring Boot.

El endpoint principal de esta fase debe recibir una sentencia SQL en formato JSON, ejecutar la lógica existente del analizador léxico y sintáctico, y devolver una respuesta JSON estructurada con:

- resultado general;
- consola;
- tokens;
- errores léxicos;
- errores sintácticos;
- resumen de análisis;
- motor SQL seleccionado;
- tipo de sentencia detectada;
- cláusulas detectadas.

Esta fase **no debe implementar análisis semántico**.

Esta fase **no debe conectarse a ninguna base de datos**.

---

# 2. Reglas obligatorias de esta fase

## 2.1 No implementar semántico

No implementar, modificar ni activar en esta fase:

```text
conexión JDBC
validación de tablas
validación de columnas
validación de schemas
validación de constraints
metadata de base de datos
análisis semántico completo
```

El campo `semanticResult` debe regresar como `null`.

El campo `connectionResult` debe regresar como `null`.

---

## 2.2 No requerir datos de conexión

El endpoint léxico/sintáctico no debe requerir:

```json
{
  "connection": {
    "host": "...",
    "port": 3306,
    "database": "...",
    "username": "...",
    "password": "..."
  }
}
```

Si el frontend envía `connection`, debe ignorarse en esta fase.

No debe validarse conexión.

No debe fallar porque no exista conexión.

---

## 2.3 Mantener package raíz

Todo debe estar bajo:

```java
package com.umg;
```

No debe existir ninguna referencia a:

```java
com.dataquery.sqlcompiler
com.umg.comdataquery.sqlcompiler
dataquery.sqlcompiler
```

Antes de terminar, revisar con búsqueda global.

---

## 2.4 No usar Swing

No crear nuevas dependencias con:

```java
javax.swing.*
org.netbeans.lib.awtextra.*
com.umg.view.*
```

Si existe lógica útil en el antiguo `ViewController`, debe migrarse a servicios Spring.

El endpoint REST no debe depender de clases Swing.

---

# 3. Clases actuales que deben revisarse antes de programar

Antes de implementar, abrir y revisar las firmas reales de estas clases:

```text
src/main/java/com/umg/model/lexer/AnalizadorSql.java
src/main/java/com/umg/model/lexer/ResultadoLexer.java
src/main/java/com/umg/model/lexer/Lexer.java
src/main/java/com/umg/model/lexer/Token.java
src/main/java/com/umg/model/lexer/TokenType.java
src/main/java/com/umg/model/parser/Parser.java
src/main/java/com/umg/model/ast/*
src/main/java/com/umg/model/dialect/SqlDialect.java
src/main/java/com/umg/model/dialect/KeywordRegistry.java
src/main/java/com/umg/model/dialect/FunctionRegistry.java
src/main/java/com/umg/model/error/*
src/main/java/com/umg/controller/ViewController.java
```

Regla importante:

```text
No inventar firmas.
Adaptarse a los métodos reales existentes.
```

Si `AnalizadorSql` ya tiene un método tipo:

```java
ResultadoLexer analizar(String sql);
```

o similar, debe reutilizarse.

Si el método requiere dialecto, usarlo.

Si no requiere dialecto, revisar cómo el lexer/parser detecta o utiliza el dialecto y adaptar el servicio sin romper lógica.

---

# 4. Arquitectura destino para esta fase

Crear o completar esta estructura:

```text
src/main/java/com/umg/
├── api/
│   └── compiler/
│       ├── CompilerController.java
│       ├── dto/
│       │   ├── CompilerAnalyzeRequest.java
│       │   ├── CompilerAnalyzeResponse.java
│       │   ├── CompilerOptionsRequest.java
│       │   ├── CompilerSummaryDto.java
│       │   ├── LexicalResultDto.java
│       │   ├── SyntaxResultDto.java
│       │   ├── TokenDto.java
│       │   └── CompilerErrorDto.java
│       └── mapper/
│           └── CompilerResponseMapper.java
├── application/
│   └── compiler/
│       ├── LexicalSyntaxAnalysisService.java
│       └── CompilerConsoleBuilder.java
└── exception/
    ├── GlobalExceptionHandler.java
    ├── InvalidCompilerRequestException.java
    └── UnsupportedDialectException.java
```

Si algunas clases ya fueron creadas por el primer Markdown, no duplicarlas.

Actualizar las existentes manteniendo coherencia.

---

# 5. Endpoint principal de esta fase

Implementar:

```http
POST /api/compiler/analyze/lexical-syntax
```

Responsabilidad:

```text
Ejecutar análisis léxico y sintáctico.
```

No debe ejecutar análisis semántico.

No debe abrir conexiones JDBC.

---

# 6. Endpoint complementario opcional

Si ya existe el endpoint de dialectos del primer Markdown, conservarlo:

```http
GET /api/compiler/dialects
```

Debe devolver:

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

Si no existe, crearlo.

---

# 7. Request JSON del endpoint léxico/sintáctico

El endpoint debe aceptar este JSON:

```json
{
  "requestId": "opcional-uuid-del-frontend",
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

## 7.1 Campos obligatorios

```text
dialect
sql
analysisMode
```

## 7.2 Valores válidos para dialect

```text
MYSQL
POSTGRESQL
SQL_SERVER
```

## 7.3 Valores válidos para analysisMode en esta fase

```text
LEXICAL_ONLY
LEXICAL_SYNTAX
```

Si llega:

```text
SEMANTIC_ONLY
FULL
```

el endpoint debe responder error controlado indicando que ese modo pertenece a la fase semántica.

No debe intentar ejecutar semántico.

---

# 8. Validaciones del request

Usar Bean Validation en DTOs.

Ejemplo:

```java
@NotBlank
private String sql;

@NotNull
private SqlDialect dialect;

@NotNull
private AnalysisMode analysisMode;
```

Validaciones obligatorias:

1. `sql` no debe ser `null`.
2. `sql` no debe estar vacío.
3. `dialect` debe ser uno de los soportados.
4. `analysisMode` debe ser `LEXICAL_ONLY` o `LEXICAL_SYNTAX`.
5. Si el SQL tiene solo espacios en blanco, responder `INVALID_REQUEST`.

---

# 9. Response JSON esperado

Respuesta exitosa ejemplo:

```json
{
  "requestId": "uuid-generado-o-recibido",
  "dialect": "MYSQL",
  "analysisMode": "LEXICAL_SYNTAX",
  "valid": true,
  "message": "La sentencia SQL es válida a nivel léxico y sintáctico.",
  "executionStatus": "SUCCESS",
  "summary": {
    "tokenCount": 12,
    "lexicalErrorCount": 0,
    "syntaxErrorCount": 0,
    "semanticErrorCount": 0,
    "warningCount": 0,
    "analyzedAt": "2026-05-20T23:15:00"
  },
  "connectionResult": null,
  "lexicalResult": {
    "valid": true,
    "message": "Análisis léxico finalizado correctamente.",
    "tokens": [
      {
        "type": "KEYWORD",
        "lexeme": "SELECT",
        "line": 1,
        "column": 1,
        "dialect": "COMMON_SQL"
      },
      {
        "type": "IDENTIFIER",
        "lexeme": "id",
        "line": 1,
        "column": 8,
        "dialect": "COMMON_SQL"
      }
    ],
    "errors": []
  },
  "syntaxResult": {
    "valid": true,
    "message": "La estructura de la sentencia SQL es correcta.",
    "statementType": "SELECT",
    "detectedClauses": [
      "SELECT",
      "FROM",
      "WHERE"
    ],
    "errors": []
  },
  "semanticResult": null,
  "errors": [],
  "console": [
    "[INFO] Iniciando análisis SQL.",
    "[INFO] Dialecto seleccionado: MYSQL.",
    "[INFO] Modo de análisis: LEXICAL_SYNTAX.",
    "[INFO] Análisis léxico finalizado sin errores.",
    "[INFO] Análisis sintáctico finalizado sin errores.",
    "[SUCCESS] Sentencia válida a nivel léxico y sintáctico."
  ]
}
```

---

# 10. Response cuando hay error léxico

Ejemplo:

```json
{
  "requestId": "uuid",
  "dialect": "MYSQL",
  "analysisMode": "LEXICAL_SYNTAX",
  "valid": false,
  "message": "La sentencia contiene errores léxicos.",
  "executionStatus": "LEXICAL_ERROR",
  "summary": {
    "tokenCount": 5,
    "lexicalErrorCount": 1,
    "syntaxErrorCount": 0,
    "semanticErrorCount": 0,
    "warningCount": 0,
    "analyzedAt": "2026-05-20T23:20:00"
  },
  "connectionResult": null,
  "lexicalResult": {
    "valid": false,
    "message": "Se detectaron errores léxicos.",
    "tokens": [],
    "errors": [
      {
        "stage": "LEXICAL",
        "code": "UNRECOGNIZED_CHARACTER",
        "message": "Carácter no reconocido '@'.",
        "line": 1,
        "column": 15,
        "lexeme": "@",
        "severity": "ERROR"
      }
    ]
  },
  "syntaxResult": null,
  "semanticResult": null,
  "errors": [
    {
      "stage": "LEXICAL",
      "code": "UNRECOGNIZED_CHARACTER",
      "message": "Carácter no reconocido '@'.",
      "line": 1,
      "column": 15,
      "lexeme": "@",
      "severity": "ERROR"
    }
  ],
  "console": [
    "[INFO] Iniciando análisis SQL.",
    "[ERROR] Carácter no reconocido '@' en línea 1, columna 15.",
    "[FAILED] Análisis detenido por error léxico."
  ]
}
```

---

# 11. Response cuando hay error sintáctico

Ejemplo:

```json
{
  "requestId": "uuid",
  "dialect": "MYSQL",
  "analysisMode": "LEXICAL_SYNTAX",
  "valid": false,
  "message": "La sentencia contiene errores sintácticos.",
  "executionStatus": "SYNTAX_ERROR",
  "summary": {
    "tokenCount": 4,
    "lexicalErrorCount": 0,
    "syntaxErrorCount": 1,
    "semanticErrorCount": 0,
    "warningCount": 0,
    "analyzedAt": "2026-05-20T23:25:00"
  },
  "connectionResult": null,
  "lexicalResult": {
    "valid": true,
    "message": "Análisis léxico finalizado correctamente.",
    "tokens": [
      {
        "type": "KEYWORD",
        "lexeme": "SELECT",
        "line": 1,
        "column": 1,
        "dialect": "COMMON_SQL"
      },
      {
        "type": "KEYWORD",
        "lexeme": "FROM",
        "line": 1,
        "column": 8,
        "dialect": "COMMON_SQL"
      }
    ],
    "errors": []
  },
  "syntaxResult": {
    "valid": false,
    "message": "La estructura de la sentencia SQL no es válida.",
    "statementType": "SELECT",
    "detectedClauses": [
      "SELECT",
      "FROM"
    ],
    "errors": [
      {
        "stage": "SYNTAX",
        "code": "EXPECTED_IDENTIFIER",
        "message": "Se esperaba un identificador después de SELECT.",
        "line": 1,
        "column": 8,
        "lexeme": "FROM",
        "severity": "ERROR"
      }
    ]
  },
  "semanticResult": null,
  "errors": [
    {
      "stage": "SYNTAX",
      "code": "EXPECTED_IDENTIFIER",
      "message": "Se esperaba un identificador después de SELECT.",
      "line": 1,
      "column": 8,
      "lexeme": "FROM",
      "severity": "ERROR"
    }
  ],
  "console": [
    "[INFO] Iniciando análisis SQL.",
    "[INFO] Análisis léxico finalizado sin errores.",
    "[ERROR] Se esperaba un identificador después de SELECT.",
    "[FAILED] Sentencia inválida a nivel sintáctico."
  ]
}
```

---

# 12. Flujo interno obligatorio del servicio

Crear o actualizar:

```java
com.umg.application.compiler.LexicalSyntaxAnalysisService
```

Flujo esperado:

```text
1. Recibir CompilerAnalyzeRequest.
2. Validar dialect.
3. Validar analysisMode.
4. Validar SQL no vacío.
5. Invocar lógica existente del lexer.
6. Si analysisMode = LEXICAL_ONLY:
   6.1 Mapear tokens.
   6.2 Mapear errores léxicos.
   6.3 No ejecutar parser.
7. Si analysisMode = LEXICAL_SYNTAX:
   7.1 Ejecutar lexer.
   7.2 Si hay errores léxicos y stopOnLexicalError = true, detener.
   7.3 Ejecutar parser.
   7.4 Mapear errores sintácticos.
8. Construir CompilerAnalyzeResponse.
9. Construir consola.
10. Retornar response.
```

---

# 13. Reglas para `LEXICAL_ONLY`

Si el request indica:

```json
"analysisMode": "LEXICAL_ONLY"
```

entonces:

- Ejecutar solo lexer.
- No ejecutar parser.
- `syntaxResult` debe ser `null`.
- `semanticResult` debe ser `null`.
- `connectionResult` debe ser `null`.

---

# 14. Reglas para `LEXICAL_SYNTAX`

Si el request indica:

```json
"analysisMode": "LEXICAL_SYNTAX"
```

entonces:

- Ejecutar lexer.
- Ejecutar parser solo si corresponde según opciones.
- `syntaxResult` debe contener resultado.
- `semanticResult` debe ser `null`.
- `connectionResult` debe ser `null`.

---

# 15. Opciones del request

Crear DTO:

```java
com.umg.api.compiler.dto.CompilerOptionsRequest
```

Campos sugeridos:

```java
private Boolean includeCommentsAsTokens;
private Boolean stopOnLexicalError;
private Boolean stopOnSyntaxError;
private Boolean returnTokenList;
private Boolean returnConsoleOutput;
```

Valores por defecto si vienen `null`:

```text
includeCommentsAsTokens = true
stopOnLexicalError = true
stopOnSyntaxError = true
returnTokenList = true
returnConsoleOutput = true
```

No usar `boolean` primitivo si se necesita detectar `null`.

---

# 16. DTOs mínimos obligatorios

## 16.1 CompilerAnalyzeRequest

Ubicación:

```text
src/main/java/com/umg/api/compiler/dto/CompilerAnalyzeRequest.java
```

Campos:

```java
private String requestId;
private SqlDialect dialect;
private String sql;
private AnalysisMode analysisMode;
private CompilerOptionsRequest options;
```

No incluir `connection` como obligatorio en esta fase.

Si existe `connection` por compatibilidad con el JSON universal, puede estar como campo opcional, pero no debe usarse.

---

## 16.2 CompilerAnalyzeResponse

Ubicación:

```text
src/main/java/com/umg/api/compiler/dto/CompilerAnalyzeResponse.java
```

Campos:

```java
private String requestId;
private SqlDialect dialect;
private AnalysisMode analysisMode;
private boolean valid;
private String message;
private ExecutionStatus executionStatus;
private CompilerSummaryDto summary;
private Object connectionResult;
private LexicalResultDto lexicalResult;
private SyntaxResultDto syntaxResult;
private Object semanticResult;
private List<CompilerErrorDto> errors;
private List<String> console;
```

---

## 16.3 TokenDto

Campos:

```java
private String type;
private String lexeme;
private int line;
private int column;
private String dialect;
```

Debe mapear desde `com.umg.model.lexer.Token`.

---

## 16.4 CompilerErrorDto

Campos:

```java
private String stage;
private String code;
private String message;
private Integer line;
private Integer column;
private String lexeme;
private String severity;
```

Debe mapear errores léxicos y sintácticos existentes.

---

## 16.5 LexicalResultDto

Campos:

```java
private boolean valid;
private String message;
private List<TokenDto> tokens;
private List<CompilerErrorDto> errors;
```

---

## 16.6 SyntaxResultDto

Campos:

```java
private boolean valid;
private String message;
private String statementType;
private List<String> detectedClauses;
private List<CompilerErrorDto> errors;
```

---

## 16.7 CompilerSummaryDto

Campos:

```java
private int tokenCount;
private int lexicalErrorCount;
private int syntaxErrorCount;
private int semanticErrorCount;
private int warningCount;
private LocalDateTime analyzedAt;
```

En esta fase:

```text
semanticErrorCount = 0
```

---

# 17. Enums recomendados

Crear si no existen:

```text
com.umg.api.compiler.dto.AnalysisMode
com.umg.api.compiler.dto.ExecutionStatus
```

## AnalysisMode

```java
public enum AnalysisMode {
    LEXICAL_ONLY,
    LEXICAL_SYNTAX,
    SEMANTIC_ONLY,
    FULL
}
```

En esta fase solo aceptar:

```text
LEXICAL_ONLY
LEXICAL_SYNTAX
```

## ExecutionStatus

```java
public enum ExecutionStatus {
    SUCCESS,
    LEXICAL_ERROR,
    SYNTAX_ERROR,
    SEMANTIC_ERROR,
    CONNECTION_ERROR,
    UNSUPPORTED_DIALECT,
    INVALID_REQUEST,
    INTERNAL_ERROR
}
```

En esta fase se usarán principalmente:

```text
SUCCESS
LEXICAL_ERROR
SYNTAX_ERROR
UNSUPPORTED_DIALECT
INVALID_REQUEST
INTERNAL_ERROR
```

---

# 18. Controller REST

Crear o actualizar:

```java
com.umg.api.compiler.CompilerController
```

Debe tener:

```java
@RestController
@RequestMapping("/api/compiler")
public class CompilerController {
    // endpoints
}
```

Método principal:

```java
@PostMapping("/analyze/lexical-syntax")
public ResponseEntity<CompilerAnalyzeResponse> analyzeLexicalSyntax(
        @Valid @RequestBody CompilerAnalyzeRequest request
) {
    return ResponseEntity.ok(lexicalSyntaxAnalysisService.analyze(request));
}
```

Reglas:

- No colocar lógica del lexer/parser dentro del controller.
- El controller solo delega al service.
- Usar `@Valid`.
- Manejar errores con `GlobalExceptionHandler`.

---

# 19. Mapper

Crear o actualizar:

```java
com.umg.api.compiler.mapper.CompilerResponseMapper
```

Responsabilidad:

- Convertir `Token` a `TokenDto`.
- Convertir errores internos a `CompilerErrorDto`.
- Convertir resultado existente del lexer/parser a DTO REST.
- Evitar exponer objetos internos directamente.

No retornar directamente `ResultadoLexer` si su estructura no coincide con el JSON estándar.

---

# 20. Consola

Crear:

```java
com.umg.application.compiler.CompilerConsoleBuilder
```

Responsabilidad:

Generar mensajes de consola como lista de strings:

```java
List<String> console;
```

Ejemplo:

```text
[INFO] Iniciando análisis SQL.
[INFO] Dialecto seleccionado: MYSQL.
[INFO] Modo de análisis: LEXICAL_SYNTAX.
[INFO] Análisis léxico finalizado sin errores.
[INFO] Análisis sintáctico finalizado sin errores.
[SUCCESS] Sentencia válida a nivel léxico y sintáctico.
```

En caso de error:

```text
[ERROR] ...
[FAILED] ...
```

---

# 21. Errores HTTP

Criterio recomendado:

## 21.1 Request inválido

```http
400 Bad Request
```

Ejemplos:

- SQL vacío.
- Dialecto inválido.
- Modo no soportado para esta fase.
- JSON mal formado.

## 21.2 Error léxico o sintáctico

```http
200 OK
```

Aunque la sentencia sea inválida, el servicio sí procesó la solicitud correctamente.

El JSON debe indicar:

```json
"valid": false
```

y:

```json
"executionStatus": "LEXICAL_ERROR"
```

o:

```json
"executionStatus": "SYNTAX_ERROR"
```

## 21.3 Error interno no controlado

```http
500 Internal Server Error
```

Debe regresar JSON estándar de error.

---

# 22. Manejo global de excepciones

Actualizar o crear:

```text
src/main/java/com/umg/exception/GlobalExceptionHandler.java
```

Debe manejar:

```text
MethodArgumentNotValidException
HttpMessageNotReadableException
InvalidCompilerRequestException
UnsupportedDialectException
Exception
```

El error debe responder con estructura compatible con `CompilerAnalyzeResponse` o un error estándar del proyecto.

No devolver stack traces al cliente.

---

# 23. Sentencias mínimas que debe soportar esta fase

La lógica ya existente debe permitir analizar como mínimo:

```sql
SELECT id, nombre FROM clientes;
SELECT c.id, c.nombre FROM clientes c WHERE c.estado = 1;
SELECT c.id, COUNT(p.id) FROM clientes c LEFT JOIN pedidos p ON p.cliente_id = c.id GROUP BY c.id;
INSERT INTO clientes(nombre, estado) VALUES('Kevin', 1);
UPDATE clientes SET estado = 1 WHERE id = 10;
DELETE FROM clientes WHERE id = 10;
CREATE TABLE clientes (id INT PRIMARY KEY, nombre VARCHAR(100));
ALTER TABLE clientes ADD COLUMN estado INT;
DROP TABLE clientes;
TRUNCATE TABLE clientes;
WITH activos AS (SELECT id FROM clientes WHERE estado = 1) SELECT * FROM activos;
```

No validar existencia real de tablas ni columnas en esta fase.

---

# 24. Dialectos SQL

El endpoint debe aceptar los dialectos definidos:

```text
MYSQL
POSTGRESQL
SQL_SERVER
```

Debe usar los catálogos existentes:

```text
KeywordRegistry
FunctionRegistry
SqlDialect
```

No mezclar reglas de dialecto directamente en el controller.

---

# 25. Funciones SQL

Debe reconocer funciones ya soportadas por el lexer/parser actual, por ejemplo:

```text
COUNT
SUM
AVG
MIN
MAX
CONCAT
NOW
GETDATE
COALESCE
DATE_FORMAT
TO_CHAR
ISNULL
```

El reconocimiento depende del dialecto.

No aceptar cualquier palabra seguida de paréntesis como función válida si la lógica actual ya valida contra catálogo.

Si esta validación ya existe en `FunctionRegistry`, reutilizarla.

---

# 26. Comentarios SQL

Si la lógica actual soporta comentarios:

```sql
-- comentario de línea

/* comentario multilinea */
```

El endpoint debe respetar la opción:

```json
"includeCommentsAsTokens": true
```

Si `includeCommentsAsTokens = true`, los comentarios deben aparecer en tokens.

Si `includeCommentsAsTokens = false`, los comentarios pueden omitirse de la respuesta.

Si la lógica actual aún no soporta esta opción, implementarla sin romper comportamiento existente.

---

# 27. Pruebas obligatorias

Antes de entregar, ejecutar:

```bash
mvn clean test
```

Si falla, corregir.

No entregar como finalizado si:

- no compila;
- falla un test;
- Spring no arranca;
- el endpoint devuelve estructura distinta;
- el endpoint intenta conectarse a BD;
- hay dependencias de Swing en el flujo REST;
- aparece `com.dataquery`.

---

# 28. Tests mínimos requeridos

Crear o actualizar tests en:

```text
src/test/java/com/umg/api/compiler/
src/test/java/com/umg/application/compiler/
```

## 28.1 Test del endpoint léxico/sintáctico válido

Probar:

```http
POST /api/compiler/analyze/lexical-syntax
```

Con:

```json
{
  "dialect": "MYSQL",
  "sql": "SELECT id, nombre FROM clientes WHERE estado = 1;",
  "analysisMode": "LEXICAL_SYNTAX"
}
```

Debe responder:

```text
HTTP 200
valid = true
executionStatus = SUCCESS
lexicalResult != null
syntaxResult != null
semanticResult = null
connectionResult = null
```

---

## 28.2 Test de SQL vacío

Request:

```json
{
  "dialect": "MYSQL",
  "sql": "",
  "analysisMode": "LEXICAL_SYNTAX"
}
```

Debe responder:

```text
HTTP 400
executionStatus = INVALID_REQUEST
```

---

## 28.3 Test de modo no permitido en esta fase

Request:

```json
{
  "dialect": "MYSQL",
  "sql": "SELECT id FROM clientes;",
  "analysisMode": "FULL"
}
```

Debe responder:

```text
HTTP 400
message indicando que FULL pertenece a la fase semántica
```

---

## 28.4 Test de error sintáctico

Request:

```json
{
  "dialect": "MYSQL",
  "sql": "SELECT FROM WHERE;",
  "analysisMode": "LEXICAL_SYNTAX"
}
```

Debe responder:

```text
HTTP 200
valid = false
executionStatus = SYNTAX_ERROR
syntaxResult.errors.size > 0
semanticResult = null
```

---

## 28.5 Test de `LEXICAL_ONLY`

Request:

```json
{
  "dialect": "MYSQL",
  "sql": "SELECT id FROM clientes;",
  "analysisMode": "LEXICAL_ONLY"
}
```

Debe responder:

```text
HTTP 200
lexicalResult != null
syntaxResult = null
semanticResult = null
connectionResult = null
```

---

## 28.6 Test de dialectos soportados

Probar:

```http
GET /api/compiler/dialects
```

Debe contener:

```text
MYSQL
POSTGRESQL
SQL_SERVER
```

---

# 29. Documentación obligatoria

Actualizar:

```text
docs/API_COMPILER.md
```

Agregar documentación completa del endpoint:

```http
POST /api/compiler/analyze/lexical-syntax
```

Debe incluir:

- URL.
- Método.
- Descripción.
- Request válido.
- Response exitoso.
- Response con error léxico.
- Response con error sintáctico.
- Nota de que no usa conexión BD.
- Nota de que `semanticResult` es `null`.
- Nota de que `connectionResult` es `null`.

Actualizar o crear:

```text
docs/LEXICAL_SYNTAX_API.md
```

Debe incluir:

- Cómo probar con `curl`.
- Ejemplos por dialecto.
- Casos soportados.
- Limitaciones.
- Comando `mvn clean test`.

---

# 30. Ejemplo curl

Agregar en documentación:

```bash
curl -X POST http://localhost:8080/api/compiler/analyze/lexical-syntax \
  -H "Content-Type: application/json" \
  -d '{
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
  }'
```

---

# 31. Criterios de aceptación

Esta fase se considera completada si:

1. Existe endpoint `POST /api/compiler/analyze/lexical-syntax`.
2. El endpoint ejecuta lexer.
3. El endpoint ejecuta parser cuando `analysisMode = LEXICAL_SYNTAX`.
4. El endpoint no ejecuta semántico.
5. El endpoint no solicita ni usa conexión a base de datos.
6. `semanticResult` devuelve `null`.
7. `connectionResult` devuelve `null`.
8. La respuesta incluye consola.
9. La respuesta incluye tokens.
10. La respuesta incluye errores léxicos si existen.
11. La respuesta incluye errores sintácticos si existen.
12. El JSON respeta la estructura del Markdown universal.
13. Se reutilizan clases existentes del modelo.
14. No se usa Swing.
15. No existe referencia a `com.dataquery`.
16. Maven compila con Java 17.
17. `mvn clean test` pasa.
18. La documentación fue actualizada.

---

# 32. Orden recomendado de trabajo

Ejecutar en este orden:

1. Revisar el Markdown universal.
2. Revisar `pom.xml`.
3. Revisar `ViewController.java`.
4. Revisar `AnalizadorSql.java`.
5. Revisar `ResultadoLexer.java`.
6. Revisar `Lexer.java`.
7. Revisar `Parser.java`.
8. Revisar `SqlDialect`, `KeywordRegistry` y `FunctionRegistry`.
9. Crear/ajustar DTOs.
10. Crear/ajustar `LexicalSyntaxAnalysisService`.
11. Crear/ajustar mapper.
12. Crear/ajustar `CompilerController`.
13. Crear manejo de errores requerido.
14. Crear tests.
15. Actualizar documentación.
16. Ejecutar `mvn clean test`.
17. Corregir fallos.
18. Entregar resumen técnico.

---

# 33. Resumen final para el agente

Implementa únicamente la migración REST del analizador léxico/sintáctico.

No implementes semántico.

No implementes conexión a base de datos.

No uses Swing.

Reutiliza la lógica existente del proyecto bajo:

```text
com.umg.model.lexer
com.umg.model.parser
com.umg.model.ast
com.umg.model.dialect
com.umg.model.error
```

Toma como referencia el flujo del antiguo:

```text
com.umg.controller.ViewController
```

pero migra ese flujo a servicios Spring.

Endpoint principal:

```http
POST /api/compiler/analyze/lexical-syntax
```

Antes de finalizar, ejecutar:

```bash
mvn clean test
```

Si falla la compilación o las pruebas, no entregar como completado.
