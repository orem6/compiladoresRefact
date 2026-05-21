# API Compiler - Documentacion de Endpoints

## GET /api/compiler/health

Health check del servicio.

**Response:**
```json
{
  "status": "UP",
  "service": "compiler-api",
  "version": "1.0.0"
}
```

---

## GET /api/compiler/dialects

Motores de base de datos soportados.

**Response:**
```json
{
  "supportedDialects": [
    "MYSQL",
    "POSTGRESQL",
    "SQL_SERVER",
    "CASSANDRA",
    "MONGODB"
  ]
}
```

---

## POST /api/compiler/analyze/lexical-syntax

Analisis lexico y sintactico de una sentencia SQL, CQL o MongoDB. Soporta los modos `LEXICAL_ONLY` y `LEXICAL_SYNTAX`.

- **SQL** (`MYSQL`, `POSTGRESQL`, `SQL_SERVER`): Lexer SQL + Parser SQL
- **CQL** (`CASSANDRA`): Lexer SQL + CqlParser (sintaxis Cassandra)
- **MongoDB** (`MONGODB`): MongoLexer (JSON) + MongoParser (aggregation pipeline)

### Campos del Request

| Campo | Tipo | Requerido | Descripcion |
|-------|------|-----------|-------------|
| requestId | String | No | Identificador unico opcional |
| dialect | SqlDialect | Si | MYSQL, POSTGRESQL, SQL_SERVER, CASSANDRA o MONGODB |
| sql | String | Si | Sentencia a analizar (SQL, CQL o JSON pipeline MongoDB) |
| analysisMode | AnalysisMode | Si | LEXICAL_ONLY o LEXICAL_SYNTAX |
| options | CompilerOptionsRequest | No | Opciones de configuracion |
| connectionConfig | ConnectionConfigDto | No | Configuracion de BD (ignorada en este endpoint) |

**Request SQL:**
```json
{
  "requestId": "opcional-uuid",
  "dialect": "MYSQL",
  "sql": "SELECT id, nombre FROM clientes WHERE estado = 1;",
  "analysisMode": "LEXICAL_SYNTAX",
  "options": {
    "includeCommentsAsTokens": true,
    "stopOnLexicalError": false,
    "stopOnSyntaxError": false,
    "returnTokenList": true,
    "returnConsoleOutput": true
  }
}
```

**Request CQL (Cassandra):**
```json
{
  "dialect": "CASSANDRA",
  "sql": "SELECT id, nombre FROM usuarios WHERE edad > 18 ALLOW FILTERING;",
  "analysisMode": "LEXICAL_SYNTAX"
}
```

**Request MongoDB (aggregation pipeline):**
```json
{
  "dialect": "MONGODB",
  "sql": "[{\"$match\": {\"status\": \"active\"}}, {\"$group\": {\"_id\": \"$category\", \"total\": {\"$sum\": 1}}}]",
  "analysisMode": "LEXICAL_SYNTAX"
}
```

**Response exitoso:**
```json
{
  "requestId": null,
  "dialect": "MYSQL",
  "analysisMode": "LEXICAL_SYNTAX",
  "valid": true,
  "message": "La sentencia SQL es valida a nivel lexico y sintactico.",
  "executionStatus": "SUCCESS",
  "summary": {
    "tokenCount": 10,
    "lexicalErrorCount": 0,
    "syntaxErrorCount": 0,
    "semanticErrorCount": 0,
    "warningCount": 0,
    "analyzedAt": "2026-05-20T23:15:00"
  },
  "connectionResult": null,
  "lexicalResult": {
    "valid": true,
    "message": "Analisis lexico finalizado correctamente.",
    "tokens": [...],
    "errors": []
  },
  "syntaxResult": {
    "valid": true,
    "message": "La estructura de la sentencia SQL es correcta.",
    "statementType": "SELECT",
    "detectedClauses": ["SELECT", "FROM", "WHERE"],
    "errors": []
  },
  "semanticResult": null,
  "errors": null,
  "console": ["[INFO] ..."]
}
```

---

## POST /api/compiler/analyze/full

Analisis completo: lexico + sintactico + semantico contra base de datos real.

Requiere `connectionConfig` con credenciales de BD. Si se envia `LEXICAL_ONLY` o `LEXICAL_SYNTAX` como `analysisMode`, se sobreescribe automaticamente a `FULL`.

### Campos de ConnectionConfigDto

| Campo | Tipo | Requerido | Descripcion |
|-------|------|-----------|-------------|
| dialect | SqlDialect | Si | MYSQL, POSTGRESQL, SQL_SERVER, CASSANDRA o MONGODB |
| host | String | Si* | Host de la BD |
| port | Integer | Si* | Puerto de la BD |
| database | String | Si* | Nombre de la base de datos / keyspace Cassandra |
| schema | String | No | Esquema (default: postgres=public, sqlserver=dbo) |
| username | String | Si* | Usuario de BD (solo SQL) |
| password | String | No | Contrasena de BD (solo SQL) |
| jdbcUrl | String | Si* | URL JDBC directa (alternativa a host+port+database, solo SQL) |
| useDirectJdbcUrl | Boolean | No | true si se usa jdbcUrl directa |

*Obligatorios a menos que se use `useDirectJdbcUrl: true` con `jdbcUrl`. Para **Cassandra** y **MongoDB** solo se requieren host, port y database.

### Request SQL (MySQL)

```json
{
  "requestId": "full-001",
  "dialect": "MYSQL",
  "sql": "SELECT id, nombre FROM usuarios WHERE edad > 18;",
  "analysisMode": "FULL",
  "connectionConfig": {
    "dialect": "MYSQL",
    "host": "localhost",
    "port": 3306,
    "database": "mi_bd",
    "schema": "public",
    "username": "root",
    "password": "mi_password"
  }
}
```

### Request CQL (Cassandra)

```json
{
  "dialect": "CASSANDRA",
  "sql": "SELECT id, nombre FROM usuarios WHERE edad > 18 ALLOW FILTERING;",
  "analysisMode": "FULL",
  "connectionConfig": {
    "dialect": "CASSANDRA",
    "host": "localhost",
    "port": 9042,
    "database": "mi_keyspace"
  }
}
```

### Request MongoDB (Aggregation Pipeline)

```json
{
  "dialect": "MONGODB",
  "sql": "[{\"$match\": {\"status\": \"active\"}}, {\"$group\": {\"_id\": \"$category\", \"total\": {\"$sum\": 1}}}]",
  "analysisMode": "FULL",
  "connectionConfig": {
    "dialect": "MONGODB",
    "host": "localhost",
    "port": 27017,
    "database": "mi_db"
  }
}
```

### Response exitoso (todo valido)

```json
{
  "requestId": "full-001",
  "dialect": "MYSQL",
  "analysisMode": "FULL",
  "valid": true,
  "message": "La sentencia SQL es valida a nivel lexico, sintactico y semantico.",
  "executionStatus": "SUCCESS",
  "summary": {
    "tokenCount": 12,
    "lexicalErrorCount": 0,
    "syntaxErrorCount": 0,
    "semanticErrorCount": 0,
    "warningCount": 0,
    "analyzedAt": "2026-05-20T23:15:00"
  },
  "lexicalResult": { ... },
  "syntaxResult": {
    "valid": true,
    "statementType": "SELECT",
    "detectedClauses": ["SELECT", "FROM", "WHERE"],
    "errors": []
  },
  "semanticResult": {
    "valid": true,
    "message": "Analisis semantico completado sin errores",
    "errors": [],
    "warnings": []
  },
  "console": ["[INFO] ..."]
}
```

### Response con error semantico (tabla no existe)

```json
{
  "valid": false,
  "executionStatus": "SEMANTIC_ERROR",
  "message": "Se encontraron errores semanticos",
  "lexicalResult": { "valid": true },
  "syntaxResult": { "valid": true },
  "semanticResult": {
    "valid": false,
    "message": "Se encontraron errores semanticos",
    "errors": [
      {
        "stage": "SEMANTIC",
        "code": "SEM_TABLE_NOT_FOUND",
        "message": "La tabla 'usuarios' no existe",
        "line": 1,
        "column": 24,
        "lexeme": "usuarios",
        "severity": "ERROR"
      }
    ],
    "warnings": []
  },
  "console": ["[INFO] ...", "[ERROR] La tabla 'usuarios' no existe en linea 1, columna 24"]
}
```

### Response con error de conexion

```json
{
  "valid": false,
  "executionStatus": "SEMANTIC_ERROR",
  "message": "Error de conexion a la base de datos",
  "lexicalResult": { "valid": true },
  "syntaxResult": { "valid": true },
  "semanticResult": {
    "valid": false,
    "message": "Error de conexion a la base de datos",
    "errors": [
      {
        "stage": "SEMANTIC",
        "code": "SEM_CONNECTION_ERROR",
        "message": "No fue posible conectar con la base de datos: Unknown database 'mi_bd'",
        "line": 0,
        "column": 0,
        "lexeme": null,
        "severity": "ERROR"
      }
    ],
    "warnings": []
  }
}
```

---

## POST /api/compiler/connection/test

Prueba de conexion a base de datos sin analizar. Soporta SQL (JDBC), Cassandra (CQL) y MongoDB.

**Request MySQL:**
```json
{
  "dialect": "MYSQL",
  "host": "localhost",
  "port": 3306,
  "database": "mi_bd",
  "username": "root",
  "password": "mi_password"
}
```

**Request Cassandra:**
```json
{
  "dialect": "CASSANDRA",
  "host": "localhost",
  "port": 9042,
  "database": "mi_keyspace"
}
```

**Request MongoDB:**
```json
{
  "dialect": "MONGODB",
  "host": "localhost",
  "port": 27017,
  "database": "mi_db"
}
```

**Response exitoso:**
```json
{
  "valid": true,
  "message": "Conexion exitosa a la base de datos.",
  "status": "SUCCESS",
  "dialect": "MYSQL",
  "database": "mi_bd",
  "schema": "mi_bd",
  "host": "localhost",
  "port": 3306
}
```

**Response con error:**
```json
{
  "valid": false,
  "message": "Error de conexion: ...",
  "status": "CONNECTION_ERROR",
  "error": "Detalle del error"
}
```

**Response configuracion invalida:**
```json
{
  "valid": false,
  "message": "Configuracion de conexion invalida. Verifique los campos obligatorios.",
  "status": "INVALID_CONFIG"
}
```

---

## ExecutionStatus

| Estado | Significado |
|--------|-------------|
| SUCCESS | Analisis completado sin errores |
| LEXICAL_ERROR | Errores lexicos detectados |
| SYNTAX_ERROR | Errores sintacticos detectados |
| SEMANTIC_ERROR | Errores semanticos detectados o error de conexion |
| CONNECTION_ERROR | Fallo la conexion a la base de datos |
| UNSUPPORTED_DIALECT | Dialecto no soportado |
| INVALID_REQUEST | Request invalido (SQL vacio, config faltante, etc.) |
| INTERNAL_ERROR | Error interno del servidor |

---

## AnalysisMode

| Modo | Descripcion |
|------|-------------|
| LEXICAL_ONLY | Solo tokenizacion, sin parser ni semantica |
| LEXICAL_SYNTAX | Tokenizacion + parser, sin semantica |
| SEMANTIC_ONLY | Analisis completo pero solo devuelve resultado semantico |
| FULL | Analisis completo: lexico + sintactico + semantico |

---

## Documentacion Swagger/OpenAPI

La documentacion interactiva de la API esta disponible a traves de Swagger UI.

- Swagger UI: `http://localhost:8080/api/compiler/docs`
- OpenAPI JSON: `http://localhost:8080/api/compiler/openapi`
- OpenAPI YAML: `http://localhost:8080/api/compiler/openapi.yaml`

Ver `docs/SWAGGER_OPENAPI.md` para mas detalles.

## Codigos de error semantico

| Codigo | Descripcion |
|--------|-------------|
| SEM_TABLE_NOT_FOUND | La tabla/coleccion referenciada no existe en la BD |
| SEM_COLUMN_NOT_FOUND | La columna/campo referenciado no existe en la tabla |
| SEM_AMBIGUOUS_COLUMN | La columna existe en multiples tablas (ambiguo) |
| SEM_ALIAS_NOT_FOUND | El alias o tabla no esta declarado en la consulta |
| SEM_TABLE_ALREADY_EXISTS | La tabla ya existe (CREATE TABLE sin IF NOT EXISTS) |
| SEM_CONNECTION_ERROR | No se pudo conectar a la base de datos |

## Codigos de error MongoDB (sintacticos)

| Codigo | Descripcion |
|--------|-------------|
| E001 | Cada stage debe ser un objeto JSON {} |
| E002 | Stage vacio |
| E003 | Se esperaba ',' o ']' entre stages |
| E004 | Se esperaba nombre de operador (ej: $match) |
| E005 | Se esperaba ':' despues del operador |
| E006 | Los operadores de stage deben comenzar con '$' |
| E007 | Operador de stage no reconocido |
| E008 | Se esperaba '}' para cerrar el stage |
