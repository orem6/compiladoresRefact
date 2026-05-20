# Guia completa de prueba de endpoints con curl

## Requisitos

- Servidor corriendo en `http://localhost:8080` (`mvn spring-boot:run`)
- Para analisis semantico: una base de datos MySQL, PostgreSQL o SQL Server accesible

---

## 1. Endpoints basicos

### 1.1 Health Check

```bash
curl -s http://localhost:8080/api/compiler/health | python3 -m json.tool
```

### 1.2 Dialectos soportados

```bash
curl -s http://localhost:8080/api/compiler/dialects | python3 -m json.tool
```

---

## 2. Analisis lexico y sintactico (sin BD)

Estos escenarios NO requieren base de datos.

### 2.1 SELECT valido

```bash
curl -s -X POST http://localhost:8080/api/compiler/analyze/lexical-syntax \
  -H "Content-Type: application/json" \
  -d '{
    "dialect": "MYSQL",
    "sql": "SELECT id, nombre FROM clientes WHERE estado = 1;",
    "analysisMode": "LEXICAL_SYNTAX"
  }' | python3 -m json.tool
```

### 2.2 Solo analisis lexico (sin parser)

```bash
curl -s -X POST http://localhost:8080/api/compiler/analyze/lexical-syntax \
  -H "Content-Type: application/json" \
  -d '{
    "dialect": "POSTGRESQL",
    "sql": "SELECT id FROM usuarios;",
    "analysisMode": "LEXICAL_ONLY"
  }' | python3 -m json.tool
```

### 2.3 INSERT con columnas

```bash
curl -s -X POST http://localhost:8080/api/compiler/analyze/lexical-syntax \
  -H "Content-Type: application/json" \
  -d '{
    "dialect": "MYSQL",
    "sql": "INSERT INTO clientes (id, nombre, email) VALUES (1, \"Juan\", \"juan@mail.com\");",
    "analysisMode": "LEXICAL_SYNTAX"
  }' | python3 -m json.tool
```

### 2.4 UPDATE con WHERE

```bash
curl -s -X POST http://localhost:8080/api/compiler/analyze/lexical-syntax \
  -H "Content-Type: application/json" \
  -d '{
    "dialect": "POSTGRESQL",
    "sql": "UPDATE productos SET precio = 99.99 WHERE id = 5;",
    "analysisMode": "LEXICAL_SYNTAX"
  }' | python3 -m json.tool
```

### 2.5 DELETE

```bash
curl -s -X POST http://localhost:8080/api/compiler/analyze/lexical-syntax \
  -H "Content-Type: application/json" \
  -d '{
    "dialect": "MYSQL",
    "sql": "DELETE FROM usuarios WHERE id = 10;",
    "analysisMode": "LEXICAL_SYNTAX"
  }' | python3 -m json.tool
```

### 2.6 CREATE TABLE

```bash
curl -s -X POST http://localhost:8080/api/compiler/analyze/lexical-syntax \
  -H "Content-Type: application/json" \
  -d '{
    "dialect": "MYSQL",
    "sql": "CREATE TABLE empleados (id INT PRIMARY KEY, nombre VARCHAR(100) NOT NULL, salario DECIMAL(10,2));",
    "analysisMode": "LEXICAL_SYNTAX"
  }' | python3 -m json.tool
```

### 2.7 SELECT con JOINs

```bash
curl -s -X POST http://localhost:8080/api/compiler/analyze/lexical-syntax \
  -H "Content-Type: application/json" \
  -d '{
    "dialect": "MYSQL",
    "sql": "SELECT u.id, u.nombre, p.descripcion FROM usuarios u INNER JOIN pedidos p ON u.id = p.usuario_id WHERE p.total > 100;",
    "analysisMode": "LEXICAL_SYNTAX"
  }' | python3 -m json.tool
```

### 2.8 SELECT con funciones

```bash
curl -s -X POST http://localhost:8080/api/compiler/analyze/lexical-syntax \
  -H "Content-Type: application/json" \
  -d '{
    "dialect": "MYSQL",
    "sql": "SELECT COUNT(*), AVG(precio), MAX(total) FROM ventas;",
    "analysisMode": "LEXICAL_SYNTAX"
  }' | python3 -m json.tool
```

### 2.9 WITH CTE

```bash
curl -s -X POST http://localhost:8080/api/compiler/analyze/lexical-syntax \
  -H "Content-Type: application/json" \
  -d '{
    "dialect": "POSTGRESQL",
    "sql": "WITH regional_sales AS (SELECT region, SUM(amount) AS total FROM sales GROUP BY region) SELECT region, total FROM regional_sales WHERE total > 1000;",
    "analysisMode": "LEXICAL_SYNTAX"
  }' | python3 -m json.tool
```

### 2.10 Identificadores delimitados por dialecto

```bash
# MySQL con backticks
curl -s -X POST http://localhost:8080/api/compiler/analyze/lexical-syntax \
  -H "Content-Type: application/json" \
  -d '{
    "dialect": "MYSQL",
    "sql": "SELECT `id`, `nombre completo` FROM `clientes`;",
    "analysisMode": "LEXICAL_SYNTAX"
  }' | python3 -m json.tool
```

```bash
# SQL Server con brackets
curl -s -X POST http://localhost:8080/api/compiler/analyze/lexical-syntax \
  -H "Content-Type: application/json" \
  -d '{
    "dialect": "SQL_SERVER",
    "sql": "SELECT [id], [nombre] FROM [usuarios];",
    "analysisMode": "LEXICAL_SYNTAX"
  }' | python3 -m json.tool
```

```bash
# PostgreSQL con comillas dobles
curl -s -X POST http://localhost:8080/api/compiler/analyze/lexical-syntax \
  -H "Content-Type: application/json" \
  -d '{
    "dialect": "POSTGRESQL",
    "sql": "SELECT \"id\", \"nombre\" FROM \"usuarios\";",
    "analysisMode": "LEXICAL_SYNTAX"
  }' | python3 -m json.tool
```

---

## 3. Escenarios de error (sin BD)

### 3.1 SQL con error lexico (caracter no reconocido)

```bash
curl -s -X POST http://localhost:8080/api/compiler/analyze/lexical-syntax \
  -H "Content-Type: application/json" \
  -d '{
    "dialect": "MYSQL",
    "sql": "SELECT id @ FROM users;",
    "analysisMode": "LEXICAL_SYNTAX"
  }' | python3 -m json.tool
```

### 3.2 SQL con error sintactico (FROM sin columnas)

```bash
curl -s -X POST http://localhost:8080/api/compiler/analyze/lexical-syntax \
  -H "Content-Type: application/json" \
  -d '{
    "dialect": "MYSQL",
    "sql": "SELECT FROM;",
    "analysisMode": "LEXICAL_SYNTAX"
  }' | python3 -m json.tool
```

### 3.3 SQL vacio

```bash
curl -s -X POST http://localhost:8080/api/compiler/analyze/lexical-syntax \
  -H "Content-Type: application/json" \
  -d '{
    "dialect": "MYSQL",
    "sql": "",
    "analysisMode": "LEXICAL_SYNTAX"
  }' | python3 -m json.tool
```

### 3.4 Dialecto invalido

```bash
curl -s -X POST http://localhost:8080/api/compiler/analyze/lexical-syntax \
  -H "Content-Type: application/json" \
  -d '{
    "dialect": "INVALIDO",
    "sql": "SELECT 1;",
    "analysisMode": "LEXICAL_SYNTAX"
  }' | python3 -m json.tool
```

---

## 4. Analisis semantico completo (requiere BD real)

Estos escenarios requieren una base de datos accesible. Reemplaza los valores de conexion por los tuyos.

### 4.1 FULL - SELECT valido contra BD

```bash
curl -s -X POST http://localhost:8080/api/compiler/analyze/full \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "sem-001",
    "dialect": "MYSQL",
    "sql": "SELECT id, nombre, email FROM usuarios WHERE activo = 1;",
    "analysisMode": "FULL",
    "connectionConfig": {
      "dialect": "MYSQL",
      "host": "localhost",
      "port": 3306,
      "database": "mi_bd",
      "username": "root",
      "password": ""
    }
  }' | python3 -m json.tool
```

### 4.2 FULL - tabla no existe (error semantico)

```bash
curl -s -X POST http://localhost:8080/api/compiler/analyze/full \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "sem-002",
    "dialect": "MYSQL",
    "sql": "SELECT * FROM tabla_inexistente;",
    "analysisMode": "FULL",
    "connectionConfig": {
      "dialect": "MYSQL",
      "host": "localhost",
      "port": 3306,
      "database": "mi_bd",
      "username": "root",
      "password": ""
    }
  }' | python3 -m json.tool
```

### 4.3 FULL - columna no existe

```bash
curl -s -X POST http://localhost:8080/api/compiler/analyze/full \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "sem-003",
    "dialect": "MYSQL",
    "sql": "SELECT columna_falsa FROM usuarios;",
    "analysisMode": "FULL",
    "connectionConfig": {
      "dialect": "MYSQL",
      "host": "localhost",
      "port": 3306,
      "database": "mi_bd",
      "username": "root",
      "password": ""
    }
  }' | python3 -m json.tool
```

### 4.4 FULL - INSERT en tabla existente

```bash
curl -s -X POST http://localhost:8080/api/compiler/analyze/full \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "sem-004",
    "dialect": "MYSQL",
    "sql": "INSERT INTO usuarios (id, nombre) VALUES (1, \"Test\");",
    "analysisMode": "FULL",
    "connectionConfig": {
      "dialect": "MYSQL",
      "host": "localhost",
      "port": 3306,
      "database": "mi_bd",
      "username": "root",
      "password": ""
    }
  }' | python3 -m json.tool
```

### 4.5 FULL - CREATE TABLE (tabla duplicada)

```bash
curl -s -X POST http://localhost:8080/api/compiler/analyze/full \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "sem-005",
    "dialect": "MYSQL",
    "sql": "CREATE TABLE usuarios (id INT PRIMARY KEY);",
    "analysisMode": "FULL",
    "connectionConfig": {
      "dialect": "MYSQL",
      "host": "localhost",
      "port": 3306,
      "database": "mi_bd",
      "username": "root",
      "password": ""
    }
  }' | python3 -m json.tool
```

### 4.6 FULL - DROP TABLE sin IF EXISTS

```bash
curl -s -X POST http://localhost:8080/api/compiler/analyze/full \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "sem-006",
    "dialect": "MYSQL",
    "sql": "DROP TABLE tabla_inexistente;",
    "analysisMode": "FULL",
    "connectionConfig": {
      "dialect": "MYSQL",
      "host": "localhost",
      "port": 3306,
      "database": "mi_bd",
      "username": "root",
      "password": ""
    }
  }' | python3 -m json.tool
```

### 4.7 FULL - DROP TABLE con IF EXISTS (no debe dar error)

```bash
curl -s -X POST http://localhost:8080/api/compiler/analyze/full \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "sem-007",
    "dialect": "MYSQL",
    "sql": "DROP TABLE IF EXISTS tabla_inexistente;",
    "analysisMode": "FULL",
    "connectionConfig": {
      "dialect": "MYSQL",
      "host": "localhost",
      "port": 3306,
      "database": "mi_bd",
      "username": "root",
      "password": ""
    }
  }' | python3 -m json.tool
```

### 4.8 FULL - UPDATE con columna valida

```bash
curl -s -X POST http://localhost:8080/api/compiler/analyze/full \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "sem-008",
    "dialect": "MYSQL",
    "sql": "UPDATE usuarios SET nombre = \"Nuevo\" WHERE id = 1;",
    "analysisMode": "FULL",
    "connectionConfig": {
      "dialect": "MYSQL",
      "host": "localhost",
      "port": 3306,
      "database": "mi_bd",
      "username": "root",
      "password": ""
    }
  }' | python3 -m json.tool
```

### 4.9 FULL - DELETE con WHERE

```bash
curl -s -X POST http://localhost:8080/api/compiler/analyze/full \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "sem-009",
    "dialect": "MYSQL",
    "sql": "DELETE FROM usuarios WHERE id = 1;",
    "analysisMode": "FULL",
    "connectionConfig": {
      "dialect": "MYSQL",
      "host": "localhost",
      "port": 3306,
      "database": "mi_bd",
      "username": "root",
      "password": ""
    }
  }' | python3 -m json.tool
```

### 4.10 FULL - modo LEXICAL_ONLY se sobreescribe a FULL

```bash
curl -s -X POST http://localhost:8080/api/compiler/analyze/full \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "sem-010",
    "dialect": "MYSQL",
    "sql": "SELECT 1;",
    "analysisMode": "LEXICAL_ONLY",
    "connectionConfig": {
      "dialect": "MYSQL",
      "host": "localhost",
      "port": 3306,
      "database": "mi_bd",
      "username": "root",
      "password": ""
    }
  }' | python3 -m json.tool
```

---

## 5. Prueba de conexion a BD

### 5.1 Conexion exitosa (campos individuales)

```bash
curl -s -X POST http://localhost:8080/api/compiler/connection/test \
  -H "Content-Type: application/json" \
  -d '{
    "dialect": "MYSQL",
    "host": "localhost",
    "port": 3306,
    "database": "mi_bd",
    "username": "root",
    "password": ""
  }' | python3 -m json.tool
```

### 5.2 Conexion con URL JDBC directa

```bash
curl -s -X POST http://localhost:8080/api/compiler/connection/test \
  -H "Content-Type: application/json" \
  -d '{
    "dialect": "POSTGRESQL",
    "jdbcUrl": "jdbc:postgresql://localhost:5432/mi_bd",
    "username": "postgres",
    "password": "mi_password",
    "useDirectJdbcUrl": true
  }' | python3 -m json.tool
```

### 5.3 Conexion fallida (BD no existe)

```bash
curl -s -X POST http://localhost:8080/api/compiler/connection/test \
  -H "Content-Type: application/json" \
  -d '{
    "dialect": "MYSQL",
    "host": "localhost",
    "port": 3306,
    "database": "bd_que_no_existe",
    "username": "root",
    "password": ""
  }' | python3 -m json.tool
```

---

## 6. Escenarios mixtos: combinando analisis

### 6.1 Probar conexion ANTES del analisis completo

```bash
# Paso 1: probar conexion
curl -s -X POST http://localhost:8080/api/compiler/connection/test \
  -H "Content-Type: application/json" \
  -d '{
    "dialect": "MYSQL",
    "host": "localhost",
    "port": 3306,
    "database": "mi_bd",
    "username": "root",
    "password": ""
  }' | python3 -c "import sys,json; d=json.load(sys.stdin); print('Conexion:', d['status'])"

# Paso 2: si la conexion es exitosa, ejecutar analisis completo
curl -s -X POST http://localhost:8080/api/compiler/analyze/full \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "flow-001",
    "dialect": "MYSQL",
    "sql": "SELECT * FROM usuarios;",
    "analysisMode": "FULL",
    "connectionConfig": {
      "dialect": "MYSQL",
      "host": "localhost",
      "port": 3306,
      "database": "mi_bd",
      "username": "root",
      "password": ""
    }
  }' | python3 -m json.tool
```

### 6.2 Primero lexico-sintactico (rapido, sin BD), luego full (con BD)

```bash
# Paso 1: validacion rapida sin BD
curl -s -X POST http://localhost:8080/api/compiler/analyze/lexical-syntax \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "check-lex",
    "dialect": "MYSQL",
    "sql": "SELECT id, nombre FROM usuarios WHERE edad > 18;",
    "analysisMode": "LEXICAL_SYNTAX"
  }' | python3 -c "import sys,json; d=json.load(sys.stdin); print('Lexico/Sintaxis valido:', d['valid'])"

# Paso 2: si es valido, ejecutar con validacion semantica contra BD
curl -s -X POST http://localhost:8080/api/compiler/analyze/full \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "check-full",
    "dialect": "MYSQL",
    "sql": "SELECT id, nombre FROM usuarios WHERE edad > 18;",
    "analysisMode": "FULL",
    "connectionConfig": {
      "dialect": "MYSQL",
      "host": "localhost",
      "port": 3306,
      "database": "mi_bd",
      "username": "root",
      "password": ""
    }
  }' | python3 -m json.tool
```

### 6.3 Validar CREATE TABLE antes de ejecutarlo

```bash
# Validar que el CREATE TABLE no dara error semantico
curl -s -X POST http://localhost:8080/api/compiler/analyze/full \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "ddl-check",
    "dialect": "MYSQL",
    "sql": "CREATE TABLE nueva_tabla (id INT PRIMARY KEY, nombre VARCHAR(100) NOT NULL);",
    "analysisMode": "FULL",
    "connectionConfig": {
      "dialect": "MYSQL",
      "host": "localhost",
      "port": 3306,
      "database": "mi_bd",
      "username": "root",
      "password": ""
    }
  }' | python3 -c "import sys,json; d=json.load(sys.stdin); print('Valido:', d['valid']); print('Semantico:', d['semanticResult']); print('Errores:', len(d['semanticResult']['errors']) if d['semanticResult'] else 'N/A')"
```

### 6.4 Detectar columna ambigua en JOIN

```bash
curl -s -X POST http://localhost:8080/api/compiler/analyze/full \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "ambig-check",
    "dialect": "MYSQL",
    "sql": "SELECT id FROM usuarios u INNER JOIN pedidos p ON u.id = p.usuario_id;",
    "analysisMode": "FULL",
    "connectionConfig": {
      "dialect": "MYSQL",
      "host": "localhost",
      "port": 3306,
      "database": "mi_bd",
      "username": "root",
      "password": ""
    }
  }' | python3 -c "import sys,json; d=json.load(sys.stdin); print('Valido:', d['valid']); print('Status:', d['executionStatus']); sem = d.get('semanticResult',{}); print('Errores semanticos:', len(sem.get('errors',[])) if sem else 0)"
```

---

## 7. Probar con diferentes dialectos

### 7.1 SQL Server

```bash
curl -s -X POST http://localhost:8080/api/compiler/analyze/full \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "mssql-test",
    "dialect": "SQL_SERVER",
    "sql": "SELECT [id], [name] FROM [users] WHERE [active] = 1;",
    "analysisMode": "FULL",
    "connectionConfig": {
      "dialect": "SQL_SERVER",
      "host": "localhost",
      "port": 1433,
      "database": "mi_bd",
      "username": "sa",
      "password": "mi_password"
    }
  }' | python3 -m json.tool
```

### 7.2 PostgreSQL

```bash
curl -s -X POST http://localhost:8080/api/compiler/analyze/full \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "pg-test",
    "dialect": "POSTGRESQL",
    "sql": "SELECT id, nombre FROM usuarios WHERE edad > 18;",
    "analysisMode": "FULL",
    "connectionConfig": {
      "dialect": "POSTGRESQL",
      "host": "localhost",
      "port": 5432,
      "database": "mi_bd",
      "schema": "public",
      "username": "postgres",
      "password": "mi_password"
    }
  }' | python3 -m json.tool
```

---

## 8. Opciones de configuracion

### 8.1 Sin tokens en la respuesta

```bash
curl -s -X POST http://localhost:8080/api/compiler/analyze/lexical-syntax \
  -H "Content-Type: application/json" \
  -d '{
    "dialect": "MYSQL",
    "sql": "SELECT 1;",
    "analysisMode": "LEXICAL_SYNTAX",
    "options": {
      "returnTokenList": false
    }
  }' | python3 -c "import sys,json; d=json.load(sys.stdin); print('Tokens retornados:', len(d['lexicalResult']['tokens']))"
```

### 8.2 Incluyendo comentarios como tokens

```bash
curl -s -X POST http://localhost:8080/api/compiler/analyze/lexical-syntax \
  -H "Content-Type: application/json" \
  -d '{
    "dialect": "MYSQL",
    "sql": "SELECT id -- solo el id\nFROM usuarios;",
    "analysisMode": "LEXICAL_SYNTAX",
    "options": {
      "includeCommentsAsTokens": true,
      "returnTokenList": true
    }
  }' | python3 -c "import sys,json; d=json.load(sys.stdin); tokens=d['lexicalResult']['tokens']; types=[t['type'] for t in tokens]; print('Tipos:', types)"
```

---

## Ejecutar todas las pruebas unitarias

```bash
mvn clean test
```

```bash
# Tests especificos
mvn test -Dtest=LexerTest              # 25 tests lexicos
mvn test -Dtest=ParserTest             # 31 tests sintacticos
mvn test -Dtest=IntegrationTest        # 42 tests integracion
mvn test -Dtest=CompilerControllerTest # 15 tests controller
```
