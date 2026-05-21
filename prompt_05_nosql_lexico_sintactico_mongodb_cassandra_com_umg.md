# PROMPT 05 PARA AGENTE PROGRAMADOR
## Parte 1 NoSQL: MongoDB + Cassandra CQL - Análisis léxico/sintáctico

> **Proyecto:** Compilador SQL/NoSQL UMG  
> **Package raíz obligatorio:** `com.umg`  
> **JDK:** 17  
> **Gestor:** Maven  
> **Arquitectura actual:** Spring Boot REST API + Swagger/OpenAPI  
> **Ruta base API:** `/api/compiler`  
> **Alcance de esta fase:** agregar análisis léxico/sintáctico para MongoDB y Cassandra CQL.  
> **Fuera de alcance:** conexión a MongoDB, conexión a Cassandra, análisis semántico NoSQL, validación real de colecciones/keyspaces/tablas/campos.  
> **Documentos previos obligatorios:**  
> - `prompt_universal_migracion_spring_com_umg.md`
> - `prompt_02_migracion_lexico_sintactico_spring_com_umg.md`
> - `prompt_03_migracion_semantico_spring_com_umg.md`
> - `prompt_04_swagger_openapi_com_umg.md`

---

# 1. Validación previa del proyecto recibido

Antes de modificar código, confirmar que el proyecto corresponde a la versión Spring/Swagger actual.

La estructura esperada es:

```text
compiladoresRefact/
├── pom.xml
├── docs/
│   ├── API_COMPILER.md
│   ├── LEXER.md
│   ├── LEXICAL_SYNTAX_API.md
│   ├── SPRING_MIGRATION.md
│   └── SWAGGER_OPENAPI.md
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/
    │   │       └── umg/
    │   │           ├── CompiladoresRefactApplication.java
    │   │           ├── api/
    │   │           │   └── compiler/
    │   │           ├── application/
    │   │           │   └── compiler/
    │   │           ├── config/
    │   │           ├── exception/
    │   │           └── model/
    │   └── resources/
    │       └── application.properties
    └── test/
        └── java/
            └── com/
                └── umg/
```

El proyecto actual ya contiene Swagger/OpenAPI, por lo que deben existir:

```text
src/main/java/com/umg/config/OpenApiConfig.java
docs/SWAGGER_OPENAPI.md
src/test/java/com/umg/api/compiler/OpenApiDocumentationTest.java
```

También debe existir la estructura REST actual:

```text
src/main/java/com/umg/api/compiler/CompilerController.java
src/main/java/com/umg/application/compiler/LexicalSyntaxAnalysisService.java
src/main/java/com/umg/api/compiler/mapper/CompilerResponseMapper.java
src/main/java/com/umg/api/compiler/dto/
```

Reglas de validación obligatorias:

```bash
grep -R "com.dataquery" src pom.xml
grep -R "com.umg.comdataquery" src pom.xml
grep -R "dataquery.sqlcompiler" src pom.xml
grep -R "javax.swing" src/main/java
grep -R "org.netbeans.lib.awtextra" src/main/java
```

El resultado esperado es que no existan referencias a:

```text
com.dataquery
com.umg.comdataquery
dataquery.sqlcompiler
javax.swing
org.netbeans.lib.awtextra
```

Si aparecen, corregir antes de implementar NoSQL.

---

# 2. Objetivo general del desarrollo NoSQL

El proyecto actualmente soporta análisis léxico/sintáctico para motores SQL:

```text
MYSQL
POSTGRESQL
SQL_SERVER
```

Se debe extender el backend para soportar dos motores NoSQL:

```text
MONGODB
CASSANDRA_CQL
```

El desarrollo completo NoSQL se dividirá en dos partes:

## Parte 1 - Este documento

Implementar únicamente análisis léxico/sintáctico para:

```text
MONGODB
CASSANDRA_CQL
```

Endpoint principal reutilizado:

```http
POST /api/compiler/analyze/lexical-syntax
```

Esta parte no debe abrir conexiones a base de datos.

Esta parte no debe validar existencia real de colecciones, campos, keyspaces, tablas ni columnas.

---

## Parte 2 - Documento futuro

Implementar conexión y análisis semántico para:

```text
MONGODB
CASSANDRA_CQL
```

Endpoints que se ampliarán después:

```http
POST /api/compiler/connection/validate
POST /api/compiler/analyze/full
```

La Parte 2 queda fuera de este prompt.

---

# 3. Reglas obligatorias de esta fase

## 3.1 No implementar conexión NoSQL

No agregar todavía dependencias de drivers:

```xml
mongodb-driver-sync
spring-boot-starter-data-mongodb
java-driver-core de Cassandra
spring-data-cassandra
```

No conectar a MongoDB.

No conectar a Cassandra.

No validar metadata real.

No validar existencia real de:

```text
bases de datos MongoDB
colecciones MongoDB
campos MongoDB
keyspaces Cassandra
tablas Cassandra
columnas Cassandra
```

---

## 3.2 No modificar semántico en esta fase

No modificar la lógica de:

```text
com.umg.model.semantic
com.umg.model.semantic.metadata
com.umg.model.semantic.validator
```

salvo que sea estrictamente necesario para evitar errores de compilación por cambios de enums o DTOs.

El análisis semántico NoSQL se implementará en otro prompt.

Para peticiones NoSQL con `analysisMode = FULL`, responder error controlado indicando que el análisis semántico NoSQL queda pendiente para la siguiente fase.

---

## 3.3 Mantener compatibilidad SQL

No romper los motores existentes:

```text
MYSQL
POSTGRESQL
SQL_SERVER
```

Los tests existentes de SQL deben seguir pasando.

El endpoint actual:

```http
POST /api/compiler/analyze/lexical-syntax
```

debe seguir funcionando para SQL como hasta ahora.

---

## 3.4 Package raíz

Todo debe mantenerse bajo:

```java
package com.umg;
```

No crear packages fuera de `com.umg`.

---

## 3.5 Swagger debe actualizarse

El proyecto ya tiene Swagger/OpenAPI.

Actualizar documentación Swagger para reflejar los nuevos dialectos:

```text
MONGODB
CASSANDRA_CQL
```

y agregar ejemplos de request léxico/sintáctico para ambos.

---

# 4. Diseño arquitectónico requerido

Actualmente existen clases SQL bajo:

```text
com.umg.model.lexer
com.umg.model.parser
com.umg.model.ast
com.umg.model.dialect
com.umg.application.compiler
```

Para no contaminar la lógica SQL, crear una separación clara para NoSQL.

Estructura recomendada:

```text
src/main/java/com/umg/
├── model/
│   ├── dialect/
│   │   ├── CompilerDialect.java
│   │   ├── SqlDialect.java
│   │   └── NoSqlDialect.java
│   └── nosql/
│       ├── common/
│       │   ├── NoSqlAnalysisResult.java
│       │   ├── NoSqlSyntaxError.java
│       │   ├── NoSqlToken.java
│       │   ├── NoSqlTokenType.java
│       │   └── NoSqlLexicalSyntaxAnalyzer.java
│       ├── mongodb/
│       │   ├── MongoLexicalSyntaxAnalyzer.java
│       │   ├── MongoLexer.java
│       │   ├── MongoParser.java
│       │   ├── MongoTokenRegistry.java
│       │   └── MongoSyntaxRules.java
│       └── cassandra/
│           ├── CassandraCqlLexicalSyntaxAnalyzer.java
│           ├── CqlLexer.java
│           ├── CqlParser.java
│           ├── CqlKeywordRegistry.java
│           └── CqlSyntaxRules.java
├── application/
│   └── compiler/
│       ├── DialectAnalysisRouter.java
│       ├── LexicalSyntaxAnalysisService.java
│       └── NoSqlLexicalSyntaxAnalysisService.java
```

Si el agente detecta que una estructura equivalente ya existe, debe reutilizarla y no duplicarla.

---

# 5. Manejo de dialectos

## 5.1 Problema actual

El proyecto actual tiene:

```text
com.umg.model.dialect.SqlDialect
```

Este enum probablemente contiene:

```text
MYSQL
POSTGRESQL
SQL_SERVER
```

Pero ahora se necesitan motores NoSQL.

No es correcto que `MONGODB` y `CASSANDRA_CQL` dependan directamente de un enum llamado `SqlDialect`, porque MongoDB no es SQL.

---

## 5.2 Solución requerida

Crear un enum general para la API:

```text
src/main/java/com/umg/model/dialect/CompilerDialect.java
```

Valores:

```java
public enum CompilerDialect {
    MYSQL,
    POSTGRESQL,
    SQL_SERVER,
    MONGODB,
    CASSANDRA_CQL
}
```

Mantener `SqlDialect` para la lógica SQL existente.

Crear, si es útil:

```text
src/main/java/com/umg/model/dialect/NoSqlDialect.java
```

Valores:

```java
public enum NoSqlDialect {
    MONGODB,
    CASSANDRA_CQL
}
```

---

## 5.3 Mapeo entre CompilerDialect y SqlDialect

Crear métodos de utilidad en una clase nueva:

```text
src/main/java/com/umg/model/dialect/DialectMapper.java
```

Responsabilidades:

```java
boolean isSql(CompilerDialect dialect);
boolean isNoSql(CompilerDialect dialect);
SqlDialect toSqlDialect(CompilerDialect dialect);
NoSqlDialect toNoSqlDialect(CompilerDialect dialect);
```

Reglas:

```text
MYSQL        -> SqlDialect.MYSQL
POSTGRESQL   -> SqlDialect.POSTGRESQL
SQL_SERVER   -> SqlDialect.SQL_SERVER
MONGODB      -> NoSqlDialect.MONGODB
CASSANDRA_CQL -> NoSqlDialect.CASSANDRA_CQL
```

Si se intenta convertir `MONGODB` a `SqlDialect`, lanzar excepción controlada.

Si se intenta convertir `MYSQL` a `NoSqlDialect`, lanzar excepción controlada.

---

# 6. Actualización de DTOs

Actualizar:

```text
src/main/java/com/umg/api/compiler/dto/CompilerAnalyzeRequest.java
```

El campo `dialect` debe aceptar ahora:

```text
MYSQL
POSTGRESQL
SQL_SERVER
MONGODB
CASSANDRA_CQL
```

Recomendación:

```java
private CompilerDialect dialect;
```

en lugar de:

```java
private SqlDialect dialect;
```

Si cambiar el tipo rompe muchas clases, hacer el cambio de forma ordenada y corregir todos los mapeos.

No usar `String` si se puede evitar.

Actualizar Swagger `@Schema`:

```java
@Schema(
    description = "Motor o dialecto seleccionado para el análisis.",
    example = "MONGODB",
    allowableValues = {
        "MYSQL",
        "POSTGRESQL",
        "SQL_SERVER",
        "MONGODB",
        "CASSANDRA_CQL"
    }
)
```

Actualizar:

```text
CompilerAnalyzeResponse
```

para que también pueda devolver el dialecto general.

---

# 7. Endpoint a reutilizar

Reutilizar el endpoint existente:

```http
POST /api/compiler/analyze/lexical-syntax
```

El endpoint debe soportar ahora:

```json
{
  "dialect": "MONGODB",
  "sql": "db.clientes.find({ estado: 1 })",
  "analysisMode": "LEXICAL_SYNTAX"
}
```

y:

```json
{
  "dialect": "CASSANDRA_CQL",
  "sql": "SELECT id, nombre FROM clientes WHERE estado = 1;",
  "analysisMode": "LEXICAL_SYNTAX"
}
```

Aunque el campo se llame `sql`, en esta fase puede seguir usándose para mantener compatibilidad del frontend/backend.

No renombrar `sql` todavía a `query`, porque eso rompe contratos existentes.

Agregar comentario en documentación:

```text
El campo `sql` se conserva por compatibilidad, pero para dialectos NoSQL representa la consulta o instrucción NoSQL enviada.
```

---

# 8. Router de análisis

Crear:

```text
src/main/java/com/umg/application/compiler/DialectAnalysisRouter.java
```

Responsabilidad:

```text
Decidir qué analizador ejecutar según el dialecto.
```

Flujo esperado:

```text
1. Recibir CompilerAnalyzeRequest.
2. Revisar request.dialect.
3. Si dialect es MYSQL/POSTGRESQL/SQL_SERVER:
   3.1 Delegar al flujo SQL existente.
4. Si dialect es MONGODB:
   4.1 Delegar a MongoLexicalSyntaxAnalyzer.
5. Si dialect es CASSANDRA_CQL:
   5.1 Delegar a CassandraCqlLexicalSyntaxAnalyzer.
6. Mapear resultado al formato REST existente.
```

No colocar esta lógica en el controller.

El controller debe seguir delegando a servicios.

---

# 9. Servicio NoSQL léxico/sintáctico

Crear:

```text
src/main/java/com/umg/application/compiler/NoSqlLexicalSyntaxAnalysisService.java
```

Responsabilidad:

```text
Ejecutar análisis léxico/sintáctico para dialectos NoSQL y mapearlos a la respuesta estándar del compilador.
```

Debe retornar el mismo tipo de respuesta que el endpoint actual:

```text
CompilerAnalyzeResponse
```

La respuesta debe llenar:

```text
lexicalResult
syntaxResult
errors
summary
console
```

Y debe dejar:

```text
semanticResult = null
connectionResult = null
```

---

# 10. Formato de respuesta para NoSQL

Debe mantenerse el formato estándar ya usado por SQL:

```json
{
  "requestId": "uuid",
  "dialect": "MONGODB",
  "analysisMode": "LEXICAL_SYNTAX",
  "valid": true,
  "message": "La instrucción MongoDB es válida a nivel léxico y sintáctico.",
  "executionStatus": "SUCCESS",
  "summary": {
    "tokenCount": 12,
    "lexicalErrorCount": 0,
    "syntaxErrorCount": 0,
    "semanticErrorCount": 0,
    "warningCount": 0,
    "analyzedAt": "2026-05-21T00:00:00"
  },
  "connectionResult": null,
  "lexicalResult": {
    "valid": true,
    "message": "Análisis léxico MongoDB finalizado correctamente.",
    "tokens": []
  },
  "syntaxResult": {
    "valid": true,
    "message": "La estructura de la instrucción MongoDB es correcta.",
    "statementType": "MONGODB_FIND",
    "detectedClauses": [
      "DB",
      "COLLECTION",
      "FIND",
      "FILTER"
    ],
    "errors": []
  },
  "semanticResult": null,
  "errors": [],
  "console": [
    "[INFO] Iniciando análisis NoSQL.",
    "[INFO] Dialecto seleccionado: MONGODB.",
    "[INFO] Modo de análisis: LEXICAL_SYNTAX.",
    "[INFO] Análisis léxico finalizado sin errores.",
    "[INFO] Análisis sintáctico finalizado sin errores.",
    "[SUCCESS] Instrucción válida a nivel léxico y sintáctico."
  ]
}
```

---

# 11. MongoDB - Alcance léxico

Crear lexer para MongoDB capaz de reconocer:

## 11.1 Tokens básicos

```text
IDENTIFIER
KEYWORD
METHOD
OPERATOR
MONGO_OPERATOR
STRING
NUMBER
BOOLEAN
NULL
OBJECT_ID
DATE_LITERAL
LEFT_BRACE
RIGHT_BRACE
LEFT_BRACKET
RIGHT_BRACKET
LEFT_PAREN
RIGHT_PAREN
COLON
COMMA
DOT
SEMICOLON
COMMENT
EOF
```

## 11.2 Símbolos

```text
{ } [ ] ( ) : , . ;
```

## 11.3 Strings

Soportar:

```text
"texto"
'texto'
```

Detectar error si la cadena no cierra.

## 11.4 Números

Soportar:

```text
1
10
10.5
-5
```

Detectar número mal formado.

## 11.5 Boolean y null

Reconocer:

```text
true
false
null
```

## 11.6 Operadores MongoDB

Reconocer operadores con `$`:

```text
$eq
$ne
$gt
$gte
$lt
$lte
$in
$nin
$and
$or
$not
$nor
$exists
$regex
$set
$unset
$inc
$push
$pull
$match
$group
$project
$sort
$lookup
$limit
$skip
$count
$sum
$avg
$min
$max
$first
$last
```

Si aparece `$operador` no registrado, marcar error léxico o sintáctico según el diseño actual.

Recomendación:

```text
Si el patrón léxico `$algo` es correcto pero el operador no existe, marcarlo como error sintáctico/controlado de operador no soportado.
```

## 11.7 Comentarios

Soportar:

```javascript
// comentario de línea
/* comentario multilinea */
```

Respetar opción:

```json
"includeCommentsAsTokens": true
```

---

# 12. MongoDB - Alcance sintáctico

Implementar parser MongoDB para validar estructura básica.

Debe soportar como mínimo:

## 12.1 find

```javascript
db.clientes.find({ estado: 1 })
db.clientes.find({ nombre: "Kevin", estado: 1 })
db.clientes.find({ edad: { $gte: 18 } })
```

Statement type:

```text
MONGODB_FIND
```

## 12.2 findOne

```javascript
db.clientes.findOne({ id: 1 })
```

Statement type:

```text
MONGODB_FIND_ONE
```

## 12.3 insertOne

```javascript
db.clientes.insertOne({ nombre: "Kevin", estado: 1 })
```

Statement type:

```text
MONGODB_INSERT_ONE
```

## 12.4 insertMany

```javascript
db.clientes.insertMany([
  { nombre: "Kevin" },
  { nombre: "Gerson" }
])
```

Statement type:

```text
MONGODB_INSERT_MANY
```

## 12.5 updateOne / updateMany

```javascript
db.clientes.updateOne(
  { id: 1 },
  { $set: { nombre: "Kevin" } }
)

db.clientes.updateMany(
  { estado: 0 },
  { $set: { estado: 1 } }
)
```

Statement types:

```text
MONGODB_UPDATE_ONE
MONGODB_UPDATE_MANY
```

## 12.6 deleteOne / deleteMany

```javascript
db.clientes.deleteOne({ id: 1 })
db.clientes.deleteMany({ estado: 0 })
```

Statement types:

```text
MONGODB_DELETE_ONE
MONGODB_DELETE_MANY
```

## 12.7 aggregate

```javascript
db.pedidos.aggregate([
  { $match: { estado: "ACTIVO" } },
  { $group: { _id: "$clienteId", total: { $sum: "$monto" } } },
  { $sort: { total: -1 } }
])
```

Statement type:

```text
MONGODB_AGGREGATE
```

## 12.8 Validaciones sintácticas MongoDB mínimas

Validar:

```text
debe iniciar con db
debe tener punto después de db
debe tener nombre de colección
debe tener punto después de colección
debe tener método soportado
paréntesis balanceados
llaves balanceadas
corchetes balanceados
dos puntos válidos en objetos
comas válidas entre pares/elementos
método con cantidad mínima de argumentos esperada
operadores $ soportados
```

No validar existencia real de colección ni campos.

---

# 13. Cassandra CQL - Alcance léxico

Cassandra CQL es parecido a SQL, pero no debe mezclarse directamente con el parser SQL actual sin control.

Crear un lexer específico o adaptar cuidadosamente el lexer SQL si resulta más limpio.

Debe reconocer:

## 13.1 Palabras clave CQL mínimas

```text
SELECT
FROM
WHERE
INSERT
INTO
VALUES
UPDATE
SET
DELETE
CREATE
KEYSPACE
TABLE
ALTER
DROP
TRUNCATE
PRIMARY
KEY
WITH
AND
OR
IF
EXISTS
NOT
NULL
USE
USING
TTL
TIMESTAMP
ORDER
BY
ALLOW
FILTERING
LIMIT
ASC
DESC
BEGIN
BATCH
APPLY
```

## 13.2 Tipos de datos CQL mínimos

```text
TEXT
VARCHAR
ASCII
INT
BIGINT
SMALLINT
TINYINT
VARINT
BOOLEAN
UUID
TIMEUUID
TIMESTAMP
DATE
TIME
FLOAT
DOUBLE
DECIMAL
BLOB
LIST
SET
MAP
```

## 13.3 Símbolos y operadores

```text
( ) { } [ ] , . ; : =
< > <= >=
*
```

## 13.4 Literales

```text
strings con 'texto'
números
booleanos true/false
uuid literal básico
null
```

## 13.5 Comentarios

Soportar:

```sql
-- comentario
// comentario
/* comentario multilinea */
```

---

# 14. Cassandra CQL - Alcance sintáctico

Implementar parser CQL para validar estructura básica.

Debe soportar como mínimo:

## 14.1 SELECT

```sql
SELECT id, nombre FROM clientes WHERE estado = 1;
SELECT * FROM keyspace.clientes WHERE id = 10;
SELECT id FROM clientes WHERE estado = 1 ALLOW FILTERING;
```

Statement type:

```text
CASSANDRA_SELECT
```

## 14.2 INSERT

```sql
INSERT INTO clientes (id, nombre, estado) VALUES (1, 'Kevin', 1);
```

Statement type:

```text
CASSANDRA_INSERT
```

## 14.3 UPDATE

```sql
UPDATE clientes SET nombre = 'Kevin' WHERE id = 1;
UPDATE clientes USING TTL 86400 SET estado = 1 WHERE id = 1;
```

Statement type:

```text
CASSANDRA_UPDATE
```

## 14.4 DELETE

```sql
DELETE FROM clientes WHERE id = 1;
DELETE nombre FROM clientes WHERE id = 1;
```

Statement type:

```text
CASSANDRA_DELETE
```

## 14.5 CREATE KEYSPACE

```sql
CREATE KEYSPACE IF NOT EXISTS demo
WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};
```

Statement type:

```text
CASSANDRA_CREATE_KEYSPACE
```

## 14.6 CREATE TABLE

```sql
CREATE TABLE clientes (
  id UUID PRIMARY KEY,
  nombre TEXT,
  estado INT
);
```

También soportar primary key compuesta básica:

```sql
CREATE TABLE pedidos (
  cliente_id UUID,
  pedido_id UUID,
  monto DECIMAL,
  PRIMARY KEY (cliente_id, pedido_id)
);
```

Statement type:

```text
CASSANDRA_CREATE_TABLE
```

## 14.7 ALTER TABLE

```sql
ALTER TABLE clientes ADD telefono TEXT;
```

Statement type:

```text
CASSANDRA_ALTER_TABLE
```

## 14.8 DROP TABLE / DROP KEYSPACE

```sql
DROP TABLE clientes;
DROP KEYSPACE demo;
```

Statement types:

```text
CASSANDRA_DROP_TABLE
CASSANDRA_DROP_KEYSPACE
```

## 14.9 TRUNCATE

```sql
TRUNCATE clientes;
TRUNCATE TABLE clientes;
```

Statement type:

```text
CASSANDRA_TRUNCATE
```

## 14.10 Validaciones sintácticas CQL mínimas

Validar:

```text
orden básico de cláusulas
SELECT debe tener columnas o *
SELECT debe tener FROM
INSERT debe tener INTO, columnas y VALUES
UPDATE debe tener SET
UPDATE debe tener WHERE si la gramática actual lo exige
DELETE debe tener FROM
CREATE TABLE debe tener paréntesis de columnas
CREATE KEYSPACE debe tener nombre
PRIMARY KEY debe estar bien formado
paréntesis balanceados
llaves balanceadas en opciones WITH
corchetes balanceados si aparecen colecciones
strings cerrados
sentencia puede terminar con ; opcional
```

No validar existencia real de keyspace, tabla ni columnas.

No validar todavía reglas semánticas de partition key.

---

# 15. Integración con respuesta actual

Usar los DTOs actuales:

```text
CompilerAnalyzeResponse
LexicalResultDto
SyntaxResultDto
TokenDto
CompilerErrorDto
CompilerSummaryDto
```

No crear un response paralelo exclusivo para NoSQL.

El frontend/Swagger debe recibir el mismo formato para SQL y NoSQL.

---

# 16. Mapeo de tokens NoSQL

Si se crean tokens NoSQL propios, mapearlos a `TokenDto`:

```java
private String type;
private String lexeme;
private int line;
private int column;
private String dialect;
```

Para MongoDB:

```text
dialect = MONGODB
```

Para Cassandra:

```text
dialect = CASSANDRA_CQL
```

---

# 17. Errores NoSQL

Mapear errores NoSQL a `CompilerErrorDto`.

Códigos sugeridos MongoDB:

```text
MONGO_EXPECTED_DB_PREFIX
MONGO_EXPECTED_COLLECTION
MONGO_UNSUPPORTED_METHOD
MONGO_EXPECTED_LEFT_PAREN
MONGO_EXPECTED_RIGHT_PAREN
MONGO_UNBALANCED_BRACES
MONGO_UNSUPPORTED_OPERATOR
MONGO_INVALID_OBJECT_SYNTAX
MONGO_UNCLOSED_STRING
```

Códigos sugeridos Cassandra:

```text
CQL_EXPECTED_SELECT_LIST
CQL_EXPECTED_FROM
CQL_EXPECTED_TABLE
CQL_EXPECTED_VALUES
CQL_EXPECTED_SET
CQL_EXPECTED_WHERE
CQL_EXPECTED_PRIMARY_KEY
CQL_UNBALANCED_PARENTHESES
CQL_UNCLOSED_STRING
CQL_UNSUPPORTED_STATEMENT
```

El formato debe seguir siendo:

```json
{
  "stage": "SYNTAX",
  "code": "MONGO_UNSUPPORTED_METHOD",
  "message": "Método MongoDB no soportado: save.",
  "line": 1,
  "column": 13,
  "lexeme": "save",
  "severity": "ERROR"
}
```

---

# 18. Actualización del endpoint dialects

Actualizar:

```http
GET /api/compiler/dialects
```

Debe devolver ahora:

```json
{
  "supportedDialects": [
    "MYSQL",
    "POSTGRESQL",
    "SQL_SERVER",
    "MONGODB",
    "CASSANDRA_CQL"
  ],
  "sqlDialects": [
    "MYSQL",
    "POSTGRESQL",
    "SQL_SERVER"
  ],
  "noSqlDialects": [
    "MONGODB",
    "CASSANDRA_CQL"
  ],
  "futureDialects": []
}
```

Si el response actual tiene otra forma, adaptarlo sin romper tests innecesariamente.

---

# 19. Actualización de Swagger/OpenAPI

Actualizar Swagger para que el endpoint:

```http
POST /api/compiler/analyze/lexical-syntax
```

incluya ejemplos para:

## 19.1 MongoDB find

```json
{
  "dialect": "MONGODB",
  "sql": "db.clientes.find({ estado: 1 })",
  "analysisMode": "LEXICAL_SYNTAX",
  "options": {
    "includeCommentsAsTokens": true,
    "stopOnLexicalError": true,
    "stopOnSyntaxError": true,
    "returnTokenList": true,
    "returnConsoleOutput": true
  }
}
```

## 19.2 MongoDB aggregate

```json
{
  "dialect": "MONGODB",
  "sql": "db.pedidos.aggregate([{ $match: { estado: 'ACTIVO' } }, { $group: { _id: '$clienteId', total: { $sum: '$monto' } } }])",
  "analysisMode": "LEXICAL_SYNTAX"
}
```

## 19.3 Cassandra SELECT

```json
{
  "dialect": "CASSANDRA_CQL",
  "sql": "SELECT id, nombre FROM clientes WHERE estado = 1 ALLOW FILTERING;",
  "analysisMode": "LEXICAL_SYNTAX"
}
```

## 19.4 Cassandra CREATE TABLE

```json
{
  "dialect": "CASSANDRA_CQL",
  "sql": "CREATE TABLE clientes (id UUID PRIMARY KEY, nombre TEXT, estado INT);",
  "analysisMode": "LEXICAL_SYNTAX"
}
```

Actualizar `@Schema` de dialect para incluir:

```text
MONGODB
CASSANDRA_CQL
```

---

# 20. Documentación Markdown obligatoria

Actualizar:

```text
docs/API_COMPILER.md
docs/LEXICAL_SYNTAX_API.md
docs/SWAGGER_OPENAPI.md
```

Crear:

```text
docs/NOSQL_LEXICAL_SYNTAX.md
```

Debe incluir:

1. Objetivo de soporte NoSQL.
2. Dialectos agregados.
3. Qué se implementa en esta fase.
4. Qué queda fuera de esta fase.
5. Ejemplos MongoDB.
6. Ejemplos Cassandra CQL.
7. Errores comunes.
8. Ejemplos curl.
9. Nota de que no hay conexión a BD todavía.
10. Nota de que el campo `sql` se conserva por compatibilidad aunque represente una instrucción NoSQL.
11. Nota de que el semántico NoSQL será fase posterior.

---

# 21. Ejemplos curl obligatorios

Agregar a `docs/NOSQL_LEXICAL_SYNTAX.md`.

## 21.1 MongoDB find

```bash
curl -X POST http://localhost:8080/api/compiler/analyze/lexical-syntax \
  -H "Content-Type: application/json" \
  -d '{
    "dialect": "MONGODB",
    "sql": "db.clientes.find({ estado: 1 })",
    "analysisMode": "LEXICAL_SYNTAX"
  }'
```

## 21.2 MongoDB aggregate

```bash
curl -X POST http://localhost:8080/api/compiler/analyze/lexical-syntax \
  -H "Content-Type: application/json" \
  -d '{
    "dialect": "MONGODB",
    "sql": "db.pedidos.aggregate([{ $match: { estado: \"ACTIVO\" } }, { $group: { _id: \"$clienteId\", total: { $sum: \"$monto\" } } }])",
    "analysisMode": "LEXICAL_SYNTAX"
  }'
```

## 21.3 Cassandra SELECT

```bash
curl -X POST http://localhost:8080/api/compiler/analyze/lexical-syntax \
  -H "Content-Type: application/json" \
  -d '{
    "dialect": "CASSANDRA_CQL",
    "sql": "SELECT id, nombre FROM clientes WHERE estado = 1 ALLOW FILTERING;",
    "analysisMode": "LEXICAL_SYNTAX"
  }'
```

## 21.4 Cassandra CREATE TABLE

```bash
curl -X POST http://localhost:8080/api/compiler/analyze/lexical-syntax \
  -H "Content-Type: application/json" \
  -d '{
    "dialect": "CASSANDRA_CQL",
    "sql": "CREATE TABLE clientes (id UUID PRIMARY KEY, nombre TEXT, estado INT);",
    "analysisMode": "LEXICAL_SYNTAX"
  }'
```

---

# 22. Tests obligatorios

Antes de entregar:

```bash
mvn clean test
```

No entregar si falla.

Crear tests en:

```text
src/test/java/com/umg/nosql/mongodb/
src/test/java/com/umg/nosql/cassandra/
src/test/java/com/umg/api/compiler/
```

---

# 23. Tests MongoDB requeridos

## 23.1 find válido

```javascript
db.clientes.find({ estado: 1 })
```

Debe responder:

```text
valid = true
executionStatus = SUCCESS
syntaxResult.statementType = MONGODB_FIND
semanticResult = null
connectionResult = null
```

## 23.2 aggregate válido

```javascript
db.pedidos.aggregate([{ $match: { estado: "ACTIVO" } }])
```

Debe responder:

```text
valid = true
statementType = MONGODB_AGGREGATE
```

## 23.3 método no soportado

```javascript
db.clientes.save({ nombre: "Kevin" })
```

Debe responder:

```text
valid = false
executionStatus = SYNTAX_ERROR
errors contiene MONGO_UNSUPPORTED_METHOD
```

## 23.4 llaves desbalanceadas

```javascript
db.clientes.find({ estado: 1 )
```

Debe responder:

```text
valid = false
executionStatus = SYNTAX_ERROR
```

## 23.5 string sin cerrar

```javascript
db.clientes.find({ nombre: "Kevin })
```

Debe responder:

```text
valid = false
executionStatus = LEXICAL_ERROR
```

---

# 24. Tests Cassandra requeridos

## 24.1 SELECT válido

```sql
SELECT id, nombre FROM clientes WHERE estado = 1;
```

Debe responder:

```text
valid = true
executionStatus = SUCCESS
statementType = CASSANDRA_SELECT
```

## 24.2 INSERT válido

```sql
INSERT INTO clientes (id, nombre) VALUES (1, 'Kevin');
```

Debe responder:

```text
valid = true
statementType = CASSANDRA_INSERT
```

## 24.3 CREATE TABLE válido

```sql
CREATE TABLE clientes (id UUID PRIMARY KEY, nombre TEXT);
```

Debe responder:

```text
valid = true
statementType = CASSANDRA_CREATE_TABLE
```

## 24.4 SELECT inválido

```sql
SELECT FROM WHERE;
```

Debe responder:

```text
valid = false
executionStatus = SYNTAX_ERROR
```

## 24.5 string sin cerrar

```sql
INSERT INTO clientes (nombre) VALUES ('Kevin);
```

Debe responder:

```text
valid = false
executionStatus = LEXICAL_ERROR
```

---

# 25. Tests de regresión SQL

Asegurar que sigan pasando los tests existentes de:

```text
MYSQL
POSTGRESQL
SQL_SERVER
```

Agregar un test explícito:

```json
{
  "dialect": "MYSQL",
  "sql": "SELECT id FROM clientes;",
  "analysisMode": "LEXICAL_SYNTAX"
}
```

Debe seguir funcionando como antes.

---

# 26. Tests de API

Actualizar `CompilerControllerTest`.

Casos mínimos:

## 26.1 Dialects endpoint

```http
GET /api/compiler/dialects
```

Debe incluir:

```text
MONGODB
CASSANDRA_CQL
```

## 26.2 MongoDB endpoint

```http
POST /api/compiler/analyze/lexical-syntax
```

con dialect `MONGODB`.

## 26.3 Cassandra endpoint

```http
POST /api/compiler/analyze/lexical-syntax
```

con dialect `CASSANDRA_CQL`.

## 26.4 FULL para NoSQL rechazado

Request:

```json
{
  "dialect": "MONGODB",
  "sql": "db.clientes.find({})",
  "analysisMode": "FULL"
}
```

En esta fase debe responder:

```text
HTTP 400
executionStatus = INVALID_REQUEST
message indicando que FULL para NoSQL queda pendiente para la fase semántica
```

---

# 27. Tests Swagger/OpenAPI

Actualizar:

```text
OpenApiDocumentationTest
```

Debe verificar que OpenAPI contenga:

```text
MONGODB
CASSANDRA_CQL
```

y que el endpoint:

```text
/api/compiler/analyze/lexical-syntax
```

siga documentado.

---

# 28. Manejo de analysisMode

Para esta fase, con NoSQL solo aceptar:

```text
LEXICAL_ONLY
LEXICAL_SYNTAX
```

Si llega NoSQL con:

```text
FULL
SEMANTIC_ONLY
```

responder error controlado.

Para SQL, no romper el comportamiento existente.

---

# 29. Consola para NoSQL

Actualizar `CompilerConsoleBuilder` o crear método nuevo.

Ejemplo MongoDB válido:

```text
[INFO] Iniciando análisis NoSQL.
[INFO] Dialecto seleccionado: MONGODB.
[INFO] Modo de análisis: LEXICAL_SYNTAX.
[INFO] Analizador MongoDB seleccionado.
[INFO] Análisis léxico finalizado sin errores.
[INFO] Análisis sintáctico finalizado sin errores.
[SUCCESS] Instrucción MongoDB válida a nivel léxico y sintáctico.
```

Ejemplo Cassandra válido:

```text
[INFO] Iniciando análisis NoSQL.
[INFO] Dialecto seleccionado: CASSANDRA_CQL.
[INFO] Modo de análisis: LEXICAL_SYNTAX.
[INFO] Analizador Cassandra CQL seleccionado.
[INFO] Análisis léxico finalizado sin errores.
[INFO] Análisis sintáctico finalizado sin errores.
[SUCCESS] Instrucción Cassandra CQL válida a nivel léxico y sintáctico.
```

---

# 30. Criterios de aceptación

La fase se considera completa si:

1. El proyecto mantiene package raíz `com.umg`.
2. No existe `com.dataquery`.
3. No existe Swing.
4. No se agregaron drivers MongoDB/Cassandra.
5. No se implementó conexión NoSQL.
6. No se implementó semántico NoSQL.
7. Se agregó soporte léxico/sintáctico para `MONGODB`.
8. Se agregó soporte léxico/sintáctico para `CASSANDRA_CQL`.
9. El endpoint `/api/compiler/analyze/lexical-syntax` acepta NoSQL.
10. SQL sigue funcionando.
11. `semanticResult = null` para NoSQL en esta fase.
12. `connectionResult = null` para NoSQL en esta fase.
13. `GET /api/compiler/dialects` lista MongoDB y Cassandra CQL.
14. Swagger/OpenAPI muestra los nuevos dialectos y ejemplos.
15. Existe `docs/NOSQL_LEXICAL_SYNTAX.md`.
16. Hay tests MongoDB.
17. Hay tests Cassandra.
18. Hay tests de regresión SQL.
19. Hay tests de API.
20. Hay tests OpenAPI actualizados.
21. `mvn clean test` pasa.

---

# 31. Orden recomendado de trabajo

Ejecutar en este orden:

1. Revisar estructura del proyecto.
2. Confirmar que es versión Spring/Swagger.
3. Revisar `pom.xml`.
4. Revisar `CompilerController.java`.
5. Revisar `CompilerAnalyzeRequest.java`.
6. Revisar `CompilerAnalyzeResponse.java`.
7. Revisar `SqlDialect.java`.
8. Revisar `LexicalSyntaxAnalysisService.java`.
9. Revisar `CompilerResponseMapper.java`.
10. Revisar `CompilerConsoleBuilder.java`.
11. Crear `CompilerDialect`.
12. Crear `NoSqlDialect`.
13. Crear `DialectMapper`.
14. Actualizar DTOs para aceptar dialectos NoSQL.
15. Crear paquetes `model/nosql`.
16. Implementar Mongo lexer.
17. Implementar Mongo parser.
18. Implementar Cassandra CQL lexer.
19. Implementar Cassandra CQL parser.
20. Crear servicio NoSQL.
21. Crear router de análisis por dialecto.
22. Integrar con endpoint actual.
23. Actualizar Swagger.
24. Actualizar documentación.
25. Crear tests.
26. Ejecutar `mvn clean test`.
27. Corregir fallos.
28. Entregar resumen técnico.

---

# 32. Resumen final para el agente

Implementa la primera parte del soporte NoSQL del compilador.

Debes agregar análisis léxico/sintáctico para:

```text
MONGODB
CASSANDRA_CQL
```

usando el endpoint existente:

```http
POST /api/compiler/analyze/lexical-syntax
```

No implementes conexión.

No implementes semántico.

No agregues drivers MongoDB ni Cassandra.

No rompas SQL.

No cambies el formato de respuesta.

Actualiza Swagger y documentación.

Antes de finalizar:

```bash
mvn clean test
```

Si falla compilación o tests, no entregar como completado.
