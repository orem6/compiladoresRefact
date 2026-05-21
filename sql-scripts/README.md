# Scripts SQL de Prueba

Estos scripts crean la base de datos y tablas necesarias para probar el analisis semantico del compilador.

## Estructura

- `clientes` (id, nombre, email, estado, fecha_creacion)
- `pedidos` (id, cliente_id, total, fecha_pedido)

## Consulta de prueba

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
