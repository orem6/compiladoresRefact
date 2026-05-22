# NoSQL Semantic API

## Objetivo
Definir el flujo de conexion dinamica y analisis semantico para `MONGODB` y `CASSANDRA_CQL`.

## Endpoints
- `POST /api/compiler/connection/validate`
- `POST /api/compiler/analyze/full`

## Dialectos soportados
- SQL: `MYSQL`, `POSTGRESQL`, `SQL_SERVER`
- NoSQL: `MONGODB`, `CASSANDRA_CQL`

## Reglas
- El backend no ejecuta queries del usuario.
- `/analyze/full` exige `analysisMode=FULL`.
- `/analyze/lexical-syntax` solo permite `LEXICAL_ONLY` y `LEXICAL_SYNTAX`.
- `password` es write-only y no se retorna.

## Defaults de puerto
- MongoDB: `27017`
- Cassandra CQL: `9042`
- MySQL: `3306`
- PostgreSQL: `5432`
- SQL Server: `1433`

## MongoDB
- Formato completo: `db.orders.aggregate([...])`
- Pipeline puro: `[{"$match": {...}}]` + `targetCollection`
- Si falta `targetCollection` en pipeline puro, se agrega warning semantico.

## Cassandra CQL
- Se valida conexion con `SELECT release_version FROM system.local`.
- Se usa `localDatacenter` (default `datacenter1`).

## Ejemplo curl (MongoDB full)
```bash
curl -X POST http://localhost:8080/api/compiler/analyze/full \
  -H "Content-Type: application/json" \
  -d '{
    "dialect":"MONGODB",
    "sql":"db.orders.find({ status: \"active\" })",
    "analysisMode":"FULL",
    "connectionConfig":{
      "dialect":"MONGODB",
      "host":"localhost",
      "port":27017,
      "database":"orders_db"
    }
  }'
```
