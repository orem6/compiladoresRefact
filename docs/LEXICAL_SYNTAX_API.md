# LEXICAL_SYNTAX API - Analizador Léxico/Sintáctico

## Descripción

Endpoint REST que ejecuta el análisis léxico y sintáctico sobre sentencias SQL utilizando la lógica existente del proyecto compilador.

## URL

```
POST http://localhost:8080/api/compiler/analyze/lexical-syntax
```

## Cómo probar con curl

### Consulta SELECT válida

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

### Solo análisis léxico

```bash
curl -X POST http://localhost:8080/api/compiler/analyze/lexical-syntax \
  -H "Content-Type: application/json" \
  -d '{
    "dialect": "POSTGRESQL",
    "sql": "SELECT id FROM usuarios;",
    "analysisMode": "LEXICAL_ONLY"
  }'
```

### Con dialecto SQL Server

```bash
curl -X POST http://localhost:8080/api/compiler/analyze/lexical-syntax \
  -H "Content-Type: application/json" \
  -d '{
    "dialect": "SQL_SERVER",
    "sql": "SELECT GETDATE();",
    "analysisMode": "LEXICAL_SYNTAX"
  }'
```

## Casos soportados

- SELECT (con WHERE, JOIN, GROUP BY, ORDER BY, HAVING, LIMIT, UNION, CTE)
- INSERT
- UPDATE
- DELETE
- CREATE TABLE
- ALTER TABLE
- DROP TABLE
- TRUNCATE TABLE
- Comentarios SQL (-- y /* */)
- Identificadores delimitados (backtick, brackets, comillas dobles)
- Funciones SQL (COUNT, SUM, AVG, NOW, GETDATE, etc.)
- Placeholders y parámetros

## Limitaciones de esta fase

- No se valida existencia real de tablas o columnas.
- No se requiere conexión a base de datos.
- El análisis semántico completo no está disponible.
- Los modos `SEMANTIC_ONLY` y `FULL` retornan error.

## Pruebas

```bash
mvn clean test
```
