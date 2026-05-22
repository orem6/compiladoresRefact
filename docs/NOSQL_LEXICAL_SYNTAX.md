# NoSQL Lexical/Syntax

## Endpoint
`POST /api/compiler/analyze/lexical-syntax`

## Dialectos NoSQL
- `MONGODB`
- `CASSANDRA_CQL`

## Reglas de modo
- Permitidos: `LEXICAL_ONLY`, `LEXICAL_SYNTAX`
- Rechazados (HTTP 400): `FULL`, `SEMANTIC_ONLY`

## Ejemplo MongoDB
```json
{
  "dialect": "MONGODB",
  "sql": "db.orders.aggregate([{ \"$match\": { \"status\": \"active\" } }])",
  "analysisMode": "LEXICAL_SYNTAX"
}
```

## Ejemplo Cassandra CQL
```json
{
  "dialect": "CASSANDRA_CQL",
  "sql": "SELECT id, nombre FROM clientes WHERE id = 1;",
  "analysisMode": "LEXICAL_SYNTAX"
}
```
