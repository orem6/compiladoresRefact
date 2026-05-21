-- ============================================
-- SCRIPT DE PRUEBA PARA ANÁLISIS SEMÁNTICO
-- Motor: PostgreSQL
-- ============================================

CREATE SCHEMA IF NOT EXISTS compilador_prueba;
SET search_path TO compilador_prueba;

-- Tabla: clientes
CREATE TABLE clientes (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    email VARCHAR(150),
    estado INTEGER NOT NULL DEFAULT 1,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla: pedidos
CREATE TABLE pedidos (
    id SERIAL PRIMARY KEY,
    cliente_id INTEGER NOT NULL,
    total NUMERIC(10,2) NOT NULL,
    fecha_pedido TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (cliente_id) REFERENCES clientes(id)
);

-- Datos de prueba
INSERT INTO clientes (nombre, email, estado) VALUES
('Juan Perez', 'juan@email.com', 1),
('Maria Lopez', 'maria@email.com', 1),
('Carlos Ruiz', 'carlos@email.com', 0);

INSERT INTO pedidos (cliente_id, total) VALUES
(1, 150.00),
(1, 200.50),
(2, 99.99);
