# Scripts de Prueba

Estos scripts crean la base de datos, tablas y colecciones necesarias para probar el analisis semantico del compilador multi-motor.

## Estructura SQL

- `clientes` (id, nombre, email, estado, fecha_creacion)
- `pedidos` (id, cliente_id, total, fecha_pedido)

## Consulta de prueba SQL

```sql
SELECT c.id, c.nombre, COUNT(p.id) AS total_pedidos
FROM clientes c
LEFT JOIN pedidos p ON p.cliente_id = c.id
WHERE c.estado = 1
GROUP BY c.id, c.nombre
ORDER BY c.nombre ASC;
```

## Dialectos

| Archivo | Motor | Puerto default |
|---------|-------|---------------|
| `01-mysql.sql` | MySQL | 3306 |
| `02-postgresql.sql` | PostgreSQL | 5432 |
| `03-sqlserver.sql` | SQL Server | 1433 |
| `04-cassandra.cql` | Cassandra (CQL) | 9042 |

## MongoDB

MongoDB no utiliza scripts SQL. Para probar la conectividad, use el endpoint `/api/compiler/connection/test` con `dialect: "MONGODB"`. Los pipelines de agregacion se envian como JSON array al endpoint `/api/compiler/analyze/full`.
