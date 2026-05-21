-- Script de prueba CQL para Cassandra
-- Compatible con Cassandra 3.x y 4.x

-- Crear keyspace
CREATE KEYSPACE IF NOT EXISTS tienda
WITH REPLICATION = {
  'class': 'SimpleStrategy',
  'replication_factor': 1
}
AND DURABLE_WRITES = true;

-- Usar keyspace
USE tienda;

-- Crear tabla con clave primaria compuesta
CREATE TABLE IF NOT EXISTS clientes (
    id UUID PRIMARY KEY,
    nombre text,
    email text,
    fecha_registro timestamp,
    activo boolean
);

-- Crear tabla con clustering columns
CREATE TABLE IF NOT EXISTS pedidos (
    cliente_id UUID,
    pedido_id UUID,
    fecha timestamp,
    total decimal,
    estado text,
    PRIMARY KEY (cliente_id, fecha, pedido_id)
) WITH CLUSTERING ORDER BY (fecha DESC, pedido_id ASC);

-- Crear tabla con lista y mapa
CREATE TABLE IF NOT EXISTS productos (
    id UUID PRIMARY KEY,
    nombre text,
    categoria text,
    precio decimal,
    stock int,
    etiquetas list<text>,
    atributos map<text, text>
);

-- Insertar datos
INSERT INTO clientes (id, nombre, email, fecha_registro, activo)
VALUES (uuid(), 'Juan Perez', 'juan@email.com', toTimestamp(now()), true)
USING TTL 86400;

INSERT INTO clientes (id, nombre, email, fecha_registro, activo)
VALUES (uuid(), 'Maria Garcia', 'maria@email.com', toTimestamp(now()), true);

-- Insertar con USING TIMESTAMP
INSERT INTO productos (id, nombre, categoria, precio, stock)
VALUES (uuid(), 'Laptop', 'Electronica', 15000.00, 10)
USING TIMESTAMP 1234567890;

-- Consultas SELECT
SELECT * FROM clientes;

SELECT nombre, email FROM clientes WHERE activo = true;

SELECT * FROM pedidos WHERE cliente_id = 123e4567-e89b-12d3-a456-426614174000
ORDER BY fecha DESC LIMIT 10;

SELECT * FROM productos WHERE categoria IN ('Electronica', 'Ropa')
ALLOW FILTERING;

-- SELECT con JSON
SELECT JSON * FROM clientes;

-- UPDATE con USING TTL
UPDATE clientes USING TTL 3600
SET email = 'juan.nuevo@email.com'
WHERE id = 123e4567-e89b-12d3-a456-426614174000;

-- UPDATE con contador
UPDATE clientes SET nombre = 'Juan Actualizado'
WHERE id = 123e4567-e89b-12d3-a456-426614174000;

-- DELETE con USING TIMESTAMP
DELETE FROM clientes
USING TIMESTAMP 9876543210
WHERE id = 123e4567-e89b-12d3-a456-426614174000;

-- DELETE condicional
DELETE FROM productos WHERE id = 123e4567-e89b-12d3-a456-426614174000
IF EXISTS;

-- TRUNCATE
TRUNCATE TABLE productos;

-- Crear indice secundario
CREATE INDEX IF NOT EXISTS idx_clientes_email ON clientes (email);

-- Crear indice en mapa
CREATE INDEX IF NOT EXISTS idx_productos_categoria ON productos (categoria);

-- ALTER TABLE
ALTER TABLE clientes ADD telefono text;

-- DROP TABLE
DROP TABLE IF EXISTS pedidos;

-- Batch statements
BEGIN BATCH
    INSERT INTO clientes (id, nombre, email) VALUES (uuid(), 'Pedro Lopez', 'pedro@email.com');
    INSERT INTO clientes (id, nombre, email) VALUES (uuid(), 'Ana Martinez', 'ana@email.com');
    UPDATE clientes SET activo = true WHERE id = 123e4567-e89b-12d3-a456-426614174000;
APPLY BATCH;

-- Materialized view (requiere tabla base con todas las columnas NOT NULL)
CREATE MATERIALIZED VIEW IF NOT EXISTS clientes_activos AS
    SELECT * FROM clientes
    WHERE activo = true AND id IS NOT NULL
    PRIMARY KEY (activo, id);

-- Eliminar keyspace (solo si es necesario)
-- DROP KEYSPACE IF EXISTS tienda;
