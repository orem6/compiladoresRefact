-- ============================================
-- SCRIPT DE PRUEBA PARA ANÁLISIS SEMÁNTICO
-- Motor: MySQL
-- ============================================

CREATE DATABASE IF NOT EXISTS compilador_prueba;
USE compilador_prueba;

-- Tabla: clientes
CREATE TABLE clientes (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    email VARCHAR(150),
    estado TINYINT NOT NULL DEFAULT 1,
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Tabla: pedidos
CREATE TABLE pedidos (
    id INT PRIMARY KEY AUTO_INCREMENT,
    cliente_id INT NOT NULL,
    total DECIMAL(10,2) NOT NULL,
    fecha_pedido DATETIME DEFAULT CURRENT_TIMESTAMP,
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
