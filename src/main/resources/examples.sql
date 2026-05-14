SELECT * FROM usuarios;
SELECT nombre, edad FROM empleados WHERE salario > 5000;
INSERT INTO clientes (nombre, email) VALUES ('Juan', 'juan@email.com');
UPDATE productos SET precio = 100 WHERE id = 1;
DELETE FROM logs WHERE fecha < '2025-01-01';
CREATE TABLE ordenes (id INT, cliente VARCHAR, total FLOAT);
DROP TABLE temporal;
