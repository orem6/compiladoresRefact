# API Compiler - Documentación de Endpoints

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

Análisis léxico y sintáctico de una sentencia SQL.

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

**Notas:**
- No usa conexión a base de datos.
- `semanticResult` siempre es `null` en esta fase.
- `connectionResult` siempre es `null` en esta fase.
- `analysisMode` acepta `LEXICAL_ONLY` y `LEXICAL_SYNTAX` en esta fase.
- Los modos `SEMANTIC_ONLY` y `FULL` retornan error controlado.

---

## POST /api/compiler/connection/test (Pendiente para siguiente fase)

Prueba de conexión a base de datos.

---

## POST /api/compiler/analyze/full (Pendiente para siguiente fase)

Análisis completo (léxico + sintáctico + semántico).
