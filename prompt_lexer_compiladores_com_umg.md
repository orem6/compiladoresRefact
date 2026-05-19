# Prompt para agente programador: Desarrollo del Lexer + Parser Sintáctico Básico para Compilador SQL Java

## Contexto general del proyecto

Estás trabajando en un proyecto académico de compiladores cuyo objetivo es migrar un compilador/validador de sentencias SQL desde una base anterior en C/C++ hacia una implementación en Java con Maven, Swing y arquitectura MVC.

El proyecto actual ya contiene una estructura Java inicial con paquetes de modelo, controlador, parser, lexer, semántica, AST, utilidades, pruebas y vistas Swing. Sin embargo, la parte que se debe trabajar en esta tarea es exclusivamente el módulo de análisis léxico y validación sintáctica básica.

La rama de trabajo corresponde al desarrollo del lexer. El estudiante encargado de esta parte es Sebas/Kevin. La parte de análisis semántico, conexión JDBC y validación real de existencia de objetos en base de datos queda para una segunda fase y NO debe implementarse en esta tarea.

## Regla crítica de paquetes

El package raíz del proyecto debe ser únicamente:

```java
com.umg
```

Todas las clases nuevas o modificadas deben vivir dentro de subpaquetes derivados de `com.umg`, por ejemplo:

```java
com.umg.model.lexer
com.umg.model.parser
com.umg.model.sql
com.umg.model.dialect
com.umg.model.error
com.umg.model.compiler
```

Está totalmente prohibido usar, crear, importar o mantener referencias a paquetes antiguos como:

```java
com.dataquery.sqlcompiler
com.umg.comdataquery.sqlcompiler
dataquery.sqlcompiler
```

Antes de hacer cambios, escanea el proyecto completo y elimina/corrige cualquier import, package, comentario técnico, documentación interna o referencia que apunte a `dataquery.sqlcompiler`, `com.dataquery` o `com.umg.comdataquery`.

Comandos sugeridos para validar:

```bash
grep -R "dataquery" -n src pom.xml README.md docs || true
grep -R "com.dataquery" -n src pom.xml README.md docs || true
grep -R "com.umg.comdataquery" -n src pom.xml README.md docs || true
grep -R "package com\.umg" -n src/main/java src/test/java
```

Al finalizar, no debe existir ninguna referencia a `dataquery.sqlcompiler` ni a variantes antiguas.

## Estructura detectada del proyecto

La estructura esperada está basada en rutas similares a estas:

```text
src/main/java/com/umg/Main.java
src/main/java/com/umg/controller/
src/main/java/com/umg/model/ast/
src/main/java/com/umg/model/compiler/
src/main/java/com/umg/model/error/
src/main/java/com/umg/model/lexer/
src/main/java/com/umg/model/parser/
src/main/java/com/umg/model/semantic/
src/main/java/com/umg/model/symbol/
src/main/java/com/umg/model/validation/
src/main/java/com/umg/util/
src/main/java/com/umg/view/
src/test/java/com/umg/
pom.xml
```

Puedes crear paquetes nuevos dentro de `com.umg`, pero debes respetar la arquitectura MVC existente.

## Restricción fuerte sobre vistas

NO modificar ningún archivo dentro de:

```text
src/main/java/com/umg/view/
```

Esto incluye:

```text
src/main/java/com/umg/view/SwingApp.java
src/main/java/com/umg/view/Visa_Compilador.java
src/main/java/com/umg/view/components/
src/main/java/com/umg/view/panels/
```

Tampoco debes modificar archivos `.form` de NetBeans/Swing.

La conexión entre la vista, el controlador y el nuevo analizador será realizada posteriormente por el estudiante. Tu tarea es dejar una clase limpia y reutilizable que pueda ser invocada desde el controlador.

## Versión de Java y Maven

El proyecto trabaja con Maven y Java 17.

Por lo tanto:

- Puedes usar características compatibles con Java 17.
- Evita dependencias innecesarias.
- Mantén el código claro, orientado a objetos, testeable y fácil de extender.
- No rompas la compilación Maven existente.

Antes de finalizar, ejecuta:

```bash
mvn clean test
```

Si falla, corrige los errores generados por tu implementación.

---

# Objetivo principal

Implementar un módulo completo de análisis léxico y validación sintáctica básica para sentencias SQL, soportando inicialmente tres dialectos:

1. MySQL
2. PostgreSQL
3. SQL Server

El diseño debe quedar preparado para agregar en el futuro dialectos NoSQL como MongoDB u otros motores, pero en esta fase NO debes implementar MongoDB.

El analizador debe recibir un `String` con la sentencia SQL ingresada desde la interfaz y devolver un objeto de resultado con:

- si la entrada es válida o no;
- mensaje general;
- lista de tokens encontrados;
- lista de errores léxicos/sintácticos;
- dialecto SQL detectado o dialectos compatibles, si aplica.

---

# API pública requerida

Debes crear o adaptar una clase principal invocable desde controlador. El uso esperado debe ser similar a:

```java
AnalizadorSql lexer = new AnalizadorSql();
ResultadoLexer resultado = lexer.analizar(sql);
```

La clase puede vivir en un paquete como:

```java
package com.umg.model.lexer;
```

O, si consideras mejor separar responsabilidades:

```java
package com.umg.model.compiler;
```

Pero siempre bajo `com.umg`.

## Clase principal sugerida

```java
package com.umg.model.lexer;

public class AnalizadorSql {
    public ResultadoLexer analizar(String sql) {
        // análisis léxico + validación sintáctica básica
    }
}
```

También puedes incluir sobrecarga para dialecto específico:

```java
public ResultadoLexer analizar(String sql, SqlDialect dialectoPreferido)
```

Esto permitirá validar una sentencia específicamente para MySQL, PostgreSQL o SQL Server cuando el controlador lo necesite.

---

# Modelo de respuesta requerido

Implementar una clase `ResultadoLexer` con, como mínimo, los siguientes campos:

```java
private boolean valido;
private String mensaje;
private List<Token> tokens;
private List<ErrorLexico> errores;
```

También puedes agregar campos útiles como:

```java
private SqlDialect dialectoDetectado;
private List<SqlDialect> dialectosCompatibles;
private boolean sintaxisBasicaValida;
```

Debe tener constructores, getters, setters y/o métodos factory según el estilo actual del proyecto.

Ejemplo de uso esperado:

```java
String sql = "SELECT id, nombre FROM clientes WHERE estado = 1;";
AnalizadorSql analizador = new AnalizadorSql();
ResultadoLexer resultado = analizador.analizar(sql);

if (resultado.isValido()) {
    resultado.getTokens().forEach(System.out::println);
} else {
    resultado.getErrores().forEach(System.out::println);
}
```

---

# Modelo de token requerido

Cada token debe contener, como mínimo:

```java
private TokenType tipo;
private String lexema;
private int linea;
private int columna;
private SqlDialect dialecto;
```

El campo `dialecto` puede ser `null`, `COMMON`, `MYSQL`, `POSTGRESQL`, `SQL_SERVER` o equivalente cuando el token sea común o específico de un motor.

Ejemplo conceptual de salida:

```text
PALABRA_RESERVADA | SELECT   | línea 1 | columna 1  | COMMON
IDENTIFICADOR     | clientes | línea 1 | columna 15 | COMMON
OPERADOR          | =        | línea 1 | columna 31 | COMMON
CADENA            | 'Kevin'  | línea 1 | columna 33 | COMMON
```

---

# Tipos de tokens mínimos

El enum `TokenType` debe cubrir, como mínimo:

```java
PALABRA_RESERVADA,
IDENTIFICADOR,
IDENTIFICADOR_DELIMITADO,
FUNCION,
TIPO_DATO,
OPERADOR,
OPERADOR_COMPARACION,
OPERADOR_LOGICO,
NUMERO_ENTERO,
NUMERO_DECIMAL,
CADENA,
COMENTARIO_LINEA,
COMENTARIO_BLOQUE,
PARENTESIS_IZQUIERDO,
PARENTESIS_DERECHO,
COMA,
PUNTO,
PUNTO_Y_COMA,
ASTERISCO,
PARAMETRO,
PLACEHOLDER,
EOF,
DESCONOCIDO
```

Puedes agregar más tipos si mejora la claridad.

---

# Manejo de errores requerido

Implementar una clase `ErrorLexico` o reutilizar/adaptar el sistema de errores existente si ya hay clases como `CompilerError`, `ErrorCollector` o `ErrorType`.

Cada error debe contener:

```java
private String codigo;
private String mensaje;
private int linea;
private int columna;
private String lexema;
```

Errores mínimos que debe detectar:

1. Carácter no reconocido.
2. Cadena sin cerrar.
3. Comentario multilinea sin cerrar.
4. Identificador inválido.
5. Número mal formado.
6. Símbolo no soportado.
7. Error sintáctico básico.
8. Paréntesis sin cerrar.
9. Comillas o delimitadores propios del dialecto sin cerrar.
10. Función no soportada para el dialecto validado.
11. Palabra reservada usada incorrectamente como identificador sin delimitador.

Ejemplo de error:

```text
E001 - Carácter no reconocido '@' en línea 1, columna 15
```

---

# Dialectos SQL requeridos

Crear una estructura extensible para dialectos. Se recomienda:

```java
package com.umg.model.dialect;

public enum SqlDialect {
    COMMON,
    MYSQL,
    POSTGRESQL,
    SQL_SERVER
}
```

También puedes crear interfaces o clases como:

```java
SqlDialectDefinition
KeywordRegistry
FunctionRegistry
OperatorRegistry
DialectRegistry
```

Cada dialecto debe tener su propio catálogo de:

- palabras reservadas;
- funciones soportadas;
- tipos de datos;
- operadores especiales;
- delimitadores de identificador;
- reglas propias cuando aplique.

## Catálogos separados por motor

Crear catálogos separados, por ejemplo:

```text
com.umg.model.dialect.mysql.MySqlKeywords
com.umg.model.dialect.mysql.MySqlFunctions
com.umg.model.dialect.postgresql.PostgreSqlKeywords
com.umg.model.dialect.postgresql.PostgreSqlFunctions
com.umg.model.dialect.sqlserver.SqlServerKeywords
com.umg.model.dialect.sqlserver.SqlServerFunctions
```

O una estructura equivalente, siempre bajo `com.umg`.

La intención es que después pueda agregarse:

```text
com.umg.model.dialect.mongodb.MongoDbKeywords
com.umg.model.dialect.mongodb.MongoDbFunctions
```

Pero NO implementar MongoDB todavía.

---

# Palabras reservadas mínimas

El lexer debe reconocer palabras reservadas comunes, incluyendo como mínimo:

```sql
SELECT, FROM, WHERE, INSERT, INTO, VALUES, UPDATE, SET, DELETE,
CREATE, TABLE, ALTER, DROP, TRUNCATE,
JOIN, INNER, LEFT, RIGHT, FULL, OUTER, CROSS, ON,
GROUP, BY, ORDER, HAVING, UNION, ALL,
WITH, AS, DISTINCT, LIMIT, OFFSET, TOP,
AND, OR, NOT, IN, LIKE, BETWEEN, IS, NULL,
CASE, WHEN, THEN, ELSE, END,
PRIMARY, KEY, FOREIGN, REFERENCES,
CONSTRAINT, DEFAULT, CHECK, UNIQUE,
DATABASE, SCHEMA, INDEX, VIEW,
ASC, DESC
```

Agregar las palabras particulares de MySQL, PostgreSQL y SQL Server según corresponda.

---

# Funciones SQL requeridas

No se debe aceptar cualquier palabra seguida de paréntesis como función válida.

El lexer/parser debe validar funciones contra catálogos conocidos por dialecto.

Funciones comunes mínimas:

```sql
COUNT, SUM, AVG, MIN, MAX,
CONCAT, COALESCE, NULLIF,
UPPER, LOWER, TRIM, LTRIM, RTRIM,
SUBSTRING, LENGTH,
ROUND, ABS,
NOW, CURRENT_DATE, CURRENT_TIME, CURRENT_TIMESTAMP
```

Funciones MySQL mínimas:

```sql
DATE_FORMAT, IFNULL, IF, DATABASE, UUID, STR_TO_DATE, CHAR_LENGTH
```

Funciones PostgreSQL mínimas:

```sql
TO_CHAR, TO_DATE, DATE_TRUNC, STRING_AGG, ARRAY_AGG, GEN_RANDOM_UUID, LENGTH
```

Funciones SQL Server mínimas:

```sql
GETDATE, ISNULL, LEN, DATEADD, DATEDIFF, FORMAT, NEWID, SYSDATETIME
```

Debe reconocer funciones con argumentos, funciones sin argumentos y funciones agregadas.

Ejemplos válidos:

```sql
SELECT COUNT(*) FROM clientes;
SELECT SUM(total) FROM ventas;
SELECT AVG(salario) FROM empleados;
SELECT CONCAT(nombre, ' ', apellido) FROM clientes;
SELECT GETDATE();
SELECT NOW();
SELECT COALESCE(nombre, 'N/A') FROM clientes;
SELECT DATE_FORMAT(fecha, '%Y-%m-%d') FROM ventas;
SELECT TO_CHAR(fecha, 'YYYY-MM-DD') FROM ventas;
SELECT ISNULL(nombre, 'N/A') FROM clientes;
```

Si una función pertenece a un dialecto específico y se valida contra otro dialecto, debe reportarse error o advertencia según el diseño que implementes.

---

# Identificadores especiales por motor

El lexer debe soportar:

## MySQL

```sql
SELECT `nombre_cliente` FROM `clientes`;
```

## SQL Server

```sql
SELECT [nombre cliente] FROM [dbo].[clientes];
```

## PostgreSQL

```sql
SELECT "nombre_cliente" FROM "clientes";
```

Debe detectar errores si el delimitador no cierra:

```sql
SELECT `nombre FROM clientes;
SELECT [nombre FROM clientes;
SELECT "nombre FROM clientes;
```

---

# Comentarios SQL

Los comentarios deben reconocerse como tokens, no ignorarse por completo.

Soportar:

```sql
-- comentario de línea

/* comentario
   multilinea */
```

Token types sugeridos:

```java
COMENTARIO_LINEA
COMENTARIO_BLOQUE
```

Detectar error si el comentario multilinea no se cierra.

---

# Validación sintáctica básica requerida

Aunque el módulo se llame lexer, esta fase debe validar también el orden básico de las sentencias SQL. No se debe implementar semántica ni conexión a base de datos.

Debe validar que las sentencias tengan estructura mínima correcta.

## SELECT

Soportar, como mínimo:

```sql
SELECT columna FROM tabla;
SELECT * FROM tabla;
SELECT columna1, columna2 FROM tabla WHERE id = 1;
SELECT COUNT(*) FROM tabla;
SELECT a.id, b.nombre FROM tabla_a a INNER JOIN tabla_b b ON a.id = b.id;
SELECT departamento, COUNT(*) FROM empleados GROUP BY departamento HAVING COUNT(*) > 1 ORDER BY departamento ASC;
WITH cte AS (SELECT id FROM clientes) SELECT * FROM cte;
```

Debe detectar errores como:

```sql
SELECT FROM tabla;
SELECT nombre WHERE id = 1;
SELECT nombre tabla;
SELECT nombre FROM;
```

## INSERT

Soportar:

```sql
INSERT INTO clientes (nombre, edad) VALUES ('Kevin', 25);
INSERT INTO clientes VALUES (1, 'Kevin');
```

Detectar errores como:

```sql
INSERT clientes (nombre) VALUES ('Kevin');
INSERT INTO VALUES ('Kevin');
```

## UPDATE

Soportar:

```sql
UPDATE clientes SET nombre = 'Kevin' WHERE id = 1;
UPDATE clientes SET estado = 1;
```

Detectar errores como:

```sql
UPDATE SET nombre = 'Kevin';
UPDATE clientes nombre = 'Kevin';
```

## DELETE

Soportar:

```sql
DELETE FROM clientes WHERE id = 1;
DELETE FROM clientes;
```

Detectar errores como:

```sql
DELETE clientes WHERE id = 1;
DELETE FROM WHERE id = 1;
```

## CREATE TABLE

Soportar estructura básica:

```sql
CREATE TABLE clientes (
    id INT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    estado INT DEFAULT 1
);
```

Detectar errores básicos de paréntesis, ausencia de nombre de tabla y columnas.

## ALTER TABLE

Soportar operaciones básicas:

```sql
ALTER TABLE clientes ADD COLUMN correo VARCHAR(100);
ALTER TABLE clientes DROP COLUMN correo;
ALTER TABLE clientes ALTER COLUMN nombre VARCHAR(150);
```

## DROP TABLE

```sql
DROP TABLE clientes;
```

## TRUNCATE

```sql
TRUNCATE TABLE clientes;
```

## JOINs

Soportar:

```sql
INNER JOIN
LEFT JOIN
LEFT OUTER JOIN
RIGHT JOIN
RIGHT OUTER JOIN
FULL JOIN
FULL OUTER JOIN
CROSS JOIN
```

Debe validar presencia de tabla y condición `ON` donde aplique, excepto `CROSS JOIN` cuando no requiera condición.

## GROUP BY, ORDER BY, HAVING, UNION

Validar estructura básica:

```sql
SELECT departamento, COUNT(*) FROM empleados GROUP BY departamento;
SELECT * FROM clientes ORDER BY nombre ASC;
SELECT departamento, COUNT(*) FROM empleados GROUP BY departamento HAVING COUNT(*) > 2;
SELECT nombre FROM clientes UNION SELECT nombre FROM proveedores;
```

## WITH / CTE

Soportar estructura básica:

```sql
WITH clientes_activos AS (
    SELECT * FROM clientes WHERE estado = 1
)
SELECT * FROM clientes_activos;
```

Detectar errores básicos como CTE sin `AS`, paréntesis sin cerrar o SELECT final ausente.

---

# Semántica fuera de alcance

NO implementar todavía:

- conexión JDBC;
- validación de existencia real de tablas;
- validación de existencia real de columnas;
- permisos;
- ejecución de sentencias;
- conexión a MySQL, PostgreSQL o SQL Server;
- validación contra metadata real de base de datos.

La validación semántica vendrá en otra fase.

---

# Reutilización del código existente

El proyecto ya puede contener clases como:

```text
com.umg.model.lexer.Lexer
com.umg.model.lexer.Token
com.umg.model.lexer.TokenType
com.umg.model.parser.Parser
com.umg.model.parser.Grammar
com.umg.model.error.CompilerError
com.umg.model.error.ErrorCollector
com.umg.model.compiler.SQLCompiler
```

Debes revisarlas y decidir si conviene:

1. extenderlas;
2. refactorizarlas;
3. crear nuevas clases compatibles;
4. mantener wrappers para no romper el flujo actual.

No elimines clases existentes si son usadas por otras partes del proyecto, a menos que actualices todas sus referencias y las pruebas sigan pasando.

La API pública nueva debe quedar clara y estable para que el controlador pueda integrarla después.

---

# Pruebas requeridas

Crear o ampliar pruebas unitarias en:

```text
src/test/java/com/umg/lexer/
src/test/java/com/umg/parser/
src/test/java/com/umg/integration/
```

Las pruebas deben cubrir, como mínimo:

## Casos válidos

```sql
SELECT * FROM clientes;
SELECT id, nombre FROM clientes WHERE estado = 1;
SELECT COUNT(*) FROM clientes;
SELECT CONCAT(nombre, ' ', apellido) FROM clientes;
SELECT GETDATE();
SELECT NOW();
SELECT TO_CHAR(fecha, 'YYYY-MM-DD') FROM ventas;
INSERT INTO clientes (nombre, edad) VALUES ('Kevin', 25);
UPDATE clientes SET estado = 1 WHERE id = 10;
DELETE FROM clientes WHERE id = 10;
CREATE TABLE clientes (id INT PRIMARY KEY, nombre VARCHAR(100));
ALTER TABLE clientes ADD COLUMN correo VARCHAR(100);
DROP TABLE clientes;
TRUNCATE TABLE clientes;
SELECT a.id FROM clientes a INNER JOIN pedidos p ON a.id = p.cliente_id;
WITH cte AS (SELECT id FROM clientes) SELECT * FROM cte;
```

## Casos inválidos

```sql
SELECT FROM clientes;
SELECT nombre WHERE id = 1;
INSERT clientes (nombre) VALUES ('Kevin');
UPDATE SET nombre = 'Kevin';
DELETE clientes WHERE id = 1;
SELECT 'cadena sin cerrar FROM clientes;
SELECT /* comentario sin cerrar FROM clientes;
SELECT @ FROM clientes;
SELECT nombre FROM;
CREATE TABLE (id INT);
WITH cte SELECT * FROM clientes;
```

## Casos por dialecto

MySQL:

```sql
SELECT `nombre_cliente` FROM `clientes`;
SELECT DATE_FORMAT(fecha, '%Y-%m-%d') FROM ventas;
```

PostgreSQL:

```sql
SELECT "nombre_cliente" FROM "clientes";
SELECT TO_CHAR(fecha, 'YYYY-MM-DD') FROM ventas;
```

SQL Server:

```sql
SELECT [nombre cliente] FROM [dbo].[clientes];
SELECT ISNULL(nombre, 'N/A') FROM clientes;
SELECT GETDATE();
```

También deben existir pruebas para confirmar que una función inválida no se acepta como función real:

```sql
SELECT FUNCION_INVENTADA(nombre) FROM clientes;
```

---

# Documentación requerida

Crear o actualizar:

```text
docs/LEXER.md
```

Debe explicar:

1. objetivo del módulo;
2. cómo usar `AnalizadorSql`;
3. ejemplo de entrada;
4. ejemplo de salida;
5. estructura de `ResultadoLexer`;
6. estructura de `Token`;
7. estructura de errores;
8. dialectos soportados;
9. funciones soportadas por motor;
10. sentencias soportadas;
11. limitaciones actuales;
12. qué queda pendiente para la fase semántica;
13. cómo ejecutar pruebas.

Ejemplo mínimo de documentación:

```java
AnalizadorSql analizador = new AnalizadorSql();
ResultadoLexer resultado = analizador.analizar("SELECT * FROM clientes;");

System.out.println(resultado.isValido());
System.out.println(resultado.getMensaje());
System.out.println(resultado.getTokens());
System.out.println(resultado.getErrores());
```

---

# Criterios de aceptación

La tarea se considera terminada solamente si se cumple todo lo siguiente:

1. El proyecto compila con Maven y Java 17.
2. `mvn clean test` pasa correctamente.
3. No se modificó ningún archivo dentro de `src/main/java/com/umg/view/`.
4. No existe ninguna referencia a `dataquery.sqlcompiler`, `com.dataquery` o `com.umg.comdataquery`.
5. La API pública permite ejecutar:

```java
ResultadoLexer resultado = new AnalizadorSql().analizar(sql);
```

6. `ResultadoLexer` contiene `valido`, `mensaje`, `tokens` y `errores`.
7. Los tokens contienen tipo, lexema, línea, columna y dialecto.
8. Se reconocen palabras reservadas y funciones por dialecto.
9. Se soportan MySQL, PostgreSQL y SQL Server.
10. Se reconocen comentarios como tokens.
11. Se validan errores léxicos básicos.
12. Se valida sintaxis básica de SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, DROP, TRUNCATE, JOIN, GROUP BY, ORDER BY, HAVING, UNION y WITH/CTE.
13. No se implementa JDBC ni validación semántica real.
14. Existe `docs/LEXER.md` actualizado.
15. Hay pruebas unitarias e integración suficientes para validar el comportamiento.

---

# Nota final para el agente

Prioriza una implementación limpia, extensible y fácil de conectar desde el controlador. No hagas cambios visuales. No modifiques Swing. No ejecutes sentencias SQL. No conectes a bases de datos.

Esta fase debe entregar el núcleo del análisis léxico y sintáctico básico, dejando preparada la base para que en una segunda fase se agregue el análisis semántico con JDBC y soporte real de metadata por motor.
