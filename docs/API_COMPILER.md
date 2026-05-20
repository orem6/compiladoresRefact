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

Dialectos SQL soportados.

**Response:**
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

## POST /api/compiler/analyze/lexical-syntax

Analisis lexico y sintactico de una sentencia SQL. Soporta los modos `LEXICAL_ONLY` y `LEXICAL_SYNTAX`.

### Campos del Request

| Campo | Tipo | Requerido | Descripcion |
|-------|------|-----------|-------------|
| requestId | String | No | Identificador unico opcional |
| dialect | SqlDialect | Si | MYSQL, POSTGRESQL o SQL_SERVER |
| sql | String | Si | Sentencia SQL a analizar |
| analysisMode | AnalysisMode | Si | LEXICAL_ONLY o LEXICAL_SYNTAX |
| options | CompilerOptionsRequest | No | Opciones de configuracion |
| connectionConfig | ConnectionConfigDto | No | Configuracion de BD (ignorada en este endpoint) |

**Request:**
```json
{
  "requestId": "opcional-uuid",
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
| dialect | SqlDialect | Si | MYSQL, POSTGRESQL o SQL_SERVER |
| host | String | Si* | Host de la BD |
| port | Integer | Si* | Puerto de la BD |
| database | String | Si* | Nombre de la base de datos |
| schema | String | No | Esquema (default: postgres=public, sqlserver=dbo) |
| username | String | Si* | Usuario de BD |
| password | String | No | Contrasena de BD |
| jdbcUrl | String | Si* | URL JDBC directa (alternativa a host+port+database) |
| useDirectJdbcUrl | Boolean | No | true si se usa jdbcUrl directa |

*Obligatorios a menos que se use `useDirectJdbcUrl: true` con `jdbcUrl`.

### Request con configuracion por campos

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

### Request con URL JDBC directa

```json
{
  "requestId": "full-002",
  "dialect": "POSTGRESQL",
  "sql": "SELECT * FROM usuarios;",
  "analysisMode": "FULL",
  "connectionConfig": {
    "dialect": "POSTGRESQL",
    "jdbcUrl": "jdbc:postgresql://localhost:5432/mi_bd",
    "username": "postgres",
    "password": "mi_password",
    "useDirectJdbcUrl": true
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

Prueba de conexion a base de datos sin analizar SQL.

**Request:**
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
  "message": "Error de conexion: Unknown database 'mi_bd'",
  "status": "CONNECTION_ERROR",
  "error": "Unknown database 'mi_bd'"
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

## Codigos de error semantico

| Codigo | Descripcion |
|--------|-------------|
| SEM_TABLE_NOT_FOUND | La tabla referenciada no existe en la BD |
| SEM_COLUMN_NOT_FOUND | La columna referenciada no existe en la tabla |
| SEM_AMBIGUOUS_COLUMN | La columna existe en multiples tablas (ambiguo) |
| SEM_ALIAS_NOT_FOUND | El alias o tabla no esta declarado en la consulta |
| SEM_TABLE_ALREADY_EXISTS | La tabla ya existe (CREATE TABLE sin IF NOT EXISTS) |
| SEM_CONNECTION_ERROR | No se pudo conectar a la base de datos |
