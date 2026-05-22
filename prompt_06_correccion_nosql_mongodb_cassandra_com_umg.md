# PROMPT 06 PARA AGENTE PROGRAMADOR
## Corrección y estabilización de soporte NoSQL MongoDB + Cassandra CQL

> **Proyecto:** Compilador SQL/NoSQL UMG  
> **Rama base recibida:** `main`  
> **Package raíz obligatorio:** `com.umg`  
> **JDK objetivo:** 17  
> **Gestor:** Maven  
> **Arquitectura actual:** Spring Boot REST API + Swagger/OpenAPI  
> **Ruta base API:** `/api/compiler`  
> **Objetivo:** Corregir la implementación actual de MongoDB y Cassandra CQL para que el servicio levante, compile, tenga pruebas y quede alineado con el diseño del proyecto.  
> **Regla principal:** No entregar como finalizado si no ejecuta `mvn clean test` correctamente.

---

# 1. Contexto del problema

La implementación actual de NoSQL fue agregada sin pruebas suficientes y actualmente el servicio no levanta.

El proyecto actual sí corresponde a la versión Spring/Swagger y mantiene la estructura bajo:

```text
src/main/java/com/umg/
```

La estructura detectada contiene:

```text
com.umg.CompiladoresRefactApplication
com.umg.api.compiler
com.umg.application.compiler
com.umg.config
com.umg.exception
com.umg.model
com.umg.model.mongo
com.umg.model.parser.CqlParser
com.umg.model.semantic.AnalizadorMongo
com.umg.model.semantic.AnalizadorCql
com.umg.model.semantic.metadata.MongoConnectionFactory
com.umg.model.semantic.metadata.CqlConnectionFactory
com.umg.model.semantic.validator.SemanticValidatorMongo
com.umg.model.semantic.validator.SemanticValidatorCql
```

También existe Swagger/OpenAPI:

```text
docs/SWAGGER_OPENAPI.md
src/main/java/com/umg/config/OpenApiConfig.java
```

El problema de arranque observado es similar a:

```text
Error creating bean with name 'mongo'
defined in MongoAutoConfiguration
Failed to instantiate [com.mongodb.client.MongoClient]
NoClassDefFoundError: com/mongodb/internal/connection/StreamFactory
```

Esto ocurre porque Spring Boot detecta el driver de MongoDB en el classpath e intenta crear automáticamente un `MongoClient` al iniciar. Además, existe mezcla de versiones entre componentes del driver MongoDB.

La aplicación debe manejar conexiones dinámicas desde JSON, no conexiones automáticas al arrancar.

---

# 2. Validaciones iniciales obligatorias

Antes de modificar código, confirmar que se está trabajando sobre la versión correcta.

Ejecutar búsquedas:

```bash
grep -R "com.dataquery" src pom.xml
grep -R "com.umg.comdataquery" src pom.xml
grep -R "dataquery.sqlcompiler" src pom.xml
grep -R "javax.swing" src/main/java
grep -R "org.netbeans.lib.awtextra" src/main/java
```

No debe existir ninguna referencia a:

```text
com.dataquery
com.umg.comdataquery
dataquery.sqlcompiler
javax.swing
org.netbeans.lib.awtextra
```

Confirmar que existen:

```text
src/main/java/com/umg/CompiladoresRefactApplication.java
src/main/java/com/umg/api/compiler/CompilerController.java
src/main/java/com/umg/application/compiler/LexicalSyntaxAnalysisService.java
src/main/java/com/umg/config/OpenApiConfig.java
src/main/java/com/umg/model/mongo/
src/main/java/com/umg/model/parser/CqlParser.java
docs/SWAGGER_OPENAPI.md
```

---

# 3. Objetivo funcional de esta corrección

Corregir y estabilizar la implementación actual de NoSQL para:

```text
MONGODB
CASSANDRA_CQL
```

El sistema debe:

1. Levantar Spring Boot sin intentar conectarse automáticamente a MongoDB, Cassandra ni SQL.
2. Mantener todo bajo package `com.umg`.
3. Mantener Swagger funcionando.
4. Mantener los endpoints SQL existentes funcionando.
5. Soportar análisis léxico/sintáctico de MongoDB.
6. Soportar análisis léxico/sintáctico de Cassandra CQL.
7. Preparar la conexión/semántico NoSQL de forma dinámica, sin autoconfiguración de Spring.
8. Ejecutar tests.
9. Actualizar documentación `.md`.

---

# 4. Corrección crítica de arranque Spring Boot

## 4.1 Excluir autoconfiguraciones automáticas

Actualizar:

```text
src/main/java/com/umg/CompiladoresRefactApplication.java
```

Debe excluir explícitamente:

```java
DataSourceAutoConfiguration.class
MongoAutoConfiguration.class
MongoDataAutoConfiguration.class
CassandraAutoConfiguration.class
CassandraDataAutoConfiguration.class
```

Implementación esperada:

```java
package com.umg;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration;
import org.springframework.boot.autoconfigure.data.cassandra.CassandraDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        MongoAutoConfiguration.class,
        MongoDataAutoConfiguration.class,
        CassandraAutoConfiguration.class,
        CassandraDataAutoConfiguration.class
})
public class CompiladoresRefactApplication {

    public static void main(String[] args) {
        SpringApplication.run(CompiladoresRefactApplication.class, args);
    }
}
```

Razón:

```text
El compilador debe recibir datos de conexión desde JSON y crear conexiones dinámicas solo cuando el endpoint lo requiera.
Spring no debe crear MongoClient, CqlSession ni DataSource al iniciar.
```

---

## 4.2 No configurar datasource ni Mongo/Cassandra fijo

Verificar que `application.properties` no tenga:

```properties
spring.datasource.url=
spring.data.mongodb.uri=
spring.cassandra.contact-points=
spring.cassandra.keyspace-name=
```

El archivo puede mantener:

```properties
spring.application.name=compiladores-refact-spring
server.port=8080
springdoc.swagger-ui.path=/api/compiler/docs
springdoc.api-docs.path=/api/compiler/openapi
```

No agregar credenciales fijas.

---

# 5. Corrección de dependencias Maven

## 5.1 Problema actual

El `pom.xml` tiene drivers NoSQL y puede producir mezcla de versiones MongoDB.

Ejemplo del error observado:

```text
mongodb-driver-sync 5.1.4
mongodb-driver-core 4.11.2
bson 4.11.2
```

Esto genera:

```text
NoClassDefFoundError: com/mongodb/internal/connection/StreamFactory
```

---

## 5.2 Regla para MongoDB

Usar una sola estrategia.

### Estrategia recomendada para Spring Boot 3.2.5

Dejar que Spring Boot maneje la versión del driver MongoDB.

Cambiar:

```xml
<dependency>
    <groupId>org.mongodb</groupId>
    <artifactId>mongodb-driver-sync</artifactId>
    <version>5.1.4</version>
</dependency>
```

por:

```xml
<dependency>
    <groupId>org.mongodb</groupId>
    <artifactId>mongodb-driver-sync</artifactId>
</dependency>
```

No declarar manualmente `mongodb-driver-core` ni `bson`.

No mezclar versiones 5.x con 4.x.

---

## 5.3 Regla para Cassandra

Mantener dependencias Datastax solo si la conexión Cassandra dinámica ya compila y será usada por `/connection/validate` o fase semántica.

Dependencias aceptadas:

```xml
<dependency>
    <groupId>com.datastax.oss</groupId>
    <artifactId>java-driver-core</artifactId>
    <version>4.17.0</version>
</dependency>

<dependency>
    <groupId>com.datastax.oss</groupId>
    <artifactId>java-driver-query-builder</artifactId>
    <version>4.17.0</version>
</dependency>
```

Pero debe mantenerse excluida la autoconfiguración Cassandra en `CompiladoresRefactApplication`.

---

## 5.4 Verificación Maven

Ejecutar:

```bash
mvn dependency:tree
```

Verificar que MongoDB no tenga mezcla incompatible de versiones.

Luego ejecutar:

```bash
mvn clean test
```

---

# 6. Estandarización de dialectos

## 6.1 Problema actual

El enum actual parece estar usando:

```text
CASSANDRA
MONGODB
```

dentro de `SqlDialect`.

Pero para claridad del API, Cassandra debe exponerse como:

```text
CASSANDRA_CQL
```

---

## 6.2 Recomendación obligatoria

Crear o corregir un enum general para la API, por ejemplo:

```text
src/main/java/com/umg/model/dialect/CompilerDialect.java
```

Con valores:

```java
public enum CompilerDialect {
    MYSQL,
    POSTGRESQL,
    SQL_SERVER,
    MONGODB,
    CASSANDRA_CQL
}
```

Mantener `SqlDialect` solo si la lógica SQL existente lo necesita internamente.

Si por costo de cambio se decide seguir usando `SqlDialect`, entonces:

1. Renombrar `CASSANDRA` a `CASSANDRA_CQL`, o
2. Aceptar `CASSANDRA` como alias temporal, pero documentar `CASSANDRA_CQL` como el valor oficial del API.

Valor oficial requerido en JSON:

```json
{
  "dialect": "CASSANDRA_CQL"
}
```

Valores oficiales:

```text
MYSQL
POSTGRESQL
SQL_SERVER
MONGODB
CASSANDRA_CQL
```

---

# 7. Corrección del endpoint de conexión

## 7.1 Problema actual

Existe endpoint:

```http
POST /api/compiler/connection/test
```

Pero el estándar definido para el proyecto es:

```http
POST /api/compiler/connection/validate
```

---

## 7.2 Cambio requerido

Implementar el endpoint oficial:

```http
POST /api/compiler/connection/validate
```

Puede mantenerse temporalmente `/connection/test` como alias si se quiere evitar romper pruebas existentes, pero debe quedar documentado como deprecated.

Recomendación:

```java
@PostMapping("/connection/validate")
public ResponseEntity<Map<String, Object>> validateConnection(...) {
    ...
}

@Deprecated
@PostMapping("/connection/test")
public ResponseEntity<Map<String, Object>> testConnection(...) {
    return validateConnection(...);
}
```

Actualizar documentación y Swagger para que el endpoint principal sea:

```http
POST /api/compiler/connection/validate
```

---

# 8. Separación correcta de responsabilidades

## 8.1 Problema actual

`CompilerController` contiene demasiada lógica de conexión directa.

El controller no debe:

```text
crear conexiones JDBC
crear conexiones MongoDB
crear sesiones Cassandra
construir objetos de conexión complejos
hacer validaciones semánticas pesadas
```

---

## 8.2 Corrección requerida

Crear o completar servicios:

```text
src/main/java/com/umg/application/compiler/ConnectionValidationService.java
src/main/java/com/umg/application/compiler/NoSqlLexicalSyntaxAnalysisService.java
src/main/java/com/umg/application/compiler/CompilerFacadeService.java
```

Responsabilidades:

### `ConnectionValidationService`

```text
Validar conexiones dinámicas SQL, MongoDB y Cassandra CQL.
Nunca guardar credenciales.
Nunca devolver password.
Cerrar recursos correctamente.
```

### `NoSqlLexicalSyntaxAnalysisService`

```text
Ejecutar MongoLexer/MongoParser y CqlParser.
No abrir conexiones.
Retornar CompilerAnalyzeResponse estándar.
```

### `CompilerFacadeService`

```text
Coordinar análisis full cuando aplique.
No ejecutar SQL del usuario.
No ejecutar query MongoDB/CQL del usuario.
Usar metadata solamente en fase semántica.
```

`CompilerController` debe delegar.

---

# 9. Revisión y corrección MongoDB léxico/sintáctico

Revisar y corregir:

```text
src/main/java/com/umg/model/mongo/MongoLexer.java
src/main/java/com/umg/model/mongo/MongoParser.java
src/main/java/com/umg/model/mongo/MongoParseError.java
src/main/java/com/umg/model/mongo/MongoParseResult.java
src/main/java/com/umg/model/mongo/MongoToken.java
src/main/java/com/umg/model/mongo/MongoTokenType.java
```

Debe soportar como mínimo:

```javascript
db.clientes.find({ estado: 1 })
db.clientes.findOne({ id: 1 })
db.clientes.insertOne({ nombre: "Kevin", estado: 1 })
db.clientes.insertMany([{ nombre: "Kevin" }, { nombre: "Gerson" }])
db.clientes.updateOne({ id: 1 }, { $set: { nombre: "Kevin" } })
db.clientes.updateMany({ estado: 0 }, { $set: { estado: 1 } })
db.clientes.deleteOne({ id: 1 })
db.clientes.deleteMany({ estado: 0 })
db.pedidos.aggregate([{ $match: { estado: "ACTIVO" } }, { $group: { _id: "$clienteId", total: { $sum: "$monto" } } }])
```

Validar:

```text
prefijo db
colección
método soportado
paréntesis balanceados
llaves balanceadas
corchetes balanceados
strings cerrados
operadores $ soportados
estructura básica de objetos JSON-like
```

No validar existencia real de colección ni campos en esta parte léxico/sintáctica.

---

# 10. Revisión y corrección Cassandra CQL léxico/sintáctico

Revisar y corregir:

```text
src/main/java/com/umg/model/parser/CqlParser.java
src/main/java/com/umg/model/semantic/AnalizadorCql.java
```

Si el parser CQL está mezclado con semántico, separar responsabilidades.

Debe soportar como mínimo:

```sql
SELECT id, nombre FROM clientes WHERE estado = 1;
SELECT * FROM keyspace.clientes WHERE id = 10;
SELECT id FROM clientes WHERE estado = 1 ALLOW FILTERING;
INSERT INTO clientes (id, nombre, estado) VALUES (1, 'Kevin', 1);
UPDATE clientes SET nombre = 'Kevin' WHERE id = 1;
UPDATE clientes USING TTL 86400 SET estado = 1 WHERE id = 1;
DELETE FROM clientes WHERE id = 1;
CREATE KEYSPACE IF NOT EXISTS demo WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};
CREATE TABLE clientes (id UUID PRIMARY KEY, nombre TEXT, estado INT);
CREATE TABLE pedidos (cliente_id UUID, pedido_id UUID, monto DECIMAL, PRIMARY KEY (cliente_id, pedido_id));
ALTER TABLE clientes ADD telefono TEXT;
DROP TABLE clientes;
DROP KEYSPACE demo;
TRUNCATE clientes;
TRUNCATE TABLE clientes;
```

Validar:

```text
orden básico de cláusulas
SELECT con lista o *
SELECT con FROM
INSERT con INTO y VALUES
UPDATE con SET
DELETE con FROM
CREATE TABLE con paréntesis
CREATE KEYSPACE con nombre
PRIMARY KEY bien formado
strings cerrados
paréntesis balanceados
llaves balanceadas
sentencia con ; opcional
```

No validar existencia real de keyspace, tabla ni columnas en el flujo léxico/sintáctico.

---

# 11. Corrección del flujo `/analyze/lexical-syntax`

Endpoint:

```http
POST /api/compiler/analyze/lexical-syntax
```

Debe aceptar:

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

Reglas:

```text
No abrir conexión.
No ejecutar semántico.
connectionResult = null.
semanticResult = null.
```

Si `analysisMode = FULL` para MongoDB o Cassandra CQL y el semántico no está completamente probado, devolver error controlado:

```text
HTTP 400
executionStatus = INVALID_REQUEST
message = "El análisis FULL para NoSQL pertenece a la fase semántica y aún no está habilitado."
```

Si el equipo decide conservar FULL NoSQL porque ya está implementado, entonces debe quedar probado con mocks y sin autoconfiguración al arranque.

---

# 12. Corrección del flujo `/analyze/full`

Endpoint:

```http
POST /api/compiler/analyze/full
```

Reglas obligatorias:

1. No cambiar automáticamente `LEXICAL_ONLY` o `LEXICAL_SYNTAX` a `FULL`.
2. Si el endpoint `/analyze/full` recibe un modo distinto de `FULL`, responder `400 Bad Request`.
3. Ejecutar léxico/sintáctico primero.
4. Si falla léxico/sintáctico, no abrir conexión.
5. Si pasa léxico/sintáctico, validar conexión.
6. Luego ejecutar semántico si corresponde.
7. No ejecutar la sentencia del usuario.
8. No devolver password.

Corregir esta práctica si existe:

```java
if (request.getAnalysisMode() == AnalysisMode.LEXICAL_ONLY
    || request.getAnalysisMode() == AnalysisMode.LEXICAL_SYNTAX) {
    request.setAnalysisMode(AnalysisMode.FULL);
}
```

Eso no debe hacerse.

---

# 13. Corrección de responses

El formato de respuesta debe seguir el estándar actual:

```json
{
  "requestId": "REQ-001",
  "dialect": "MONGODB",
  "analysisMode": "LEXICAL_SYNTAX",
  "valid": true,
  "message": "La instrucción MongoDB es válida a nivel léxico y sintáctico.",
  "executionStatus": "SUCCESS",
  "summary": {
    "tokenCount": 10,
    "lexicalErrorCount": 0,
    "syntaxErrorCount": 0,
    "semanticErrorCount": 0,
    "warningCount": 0,
    "analyzedAt": "2026-05-21T00:00:00"
  },
  "connectionResult": null,
  "lexicalResult": {
    "valid": true,
    "message": "Análisis léxico finalizado correctamente.",
    "tokens": []
  },
  "syntaxResult": {
    "valid": true,
    "message": "La estructura es correcta.",
    "statementType": "MONGODB_FIND",
    "detectedClauses": ["DB", "COLLECTION", "FIND", "FILTER"],
    "errors": []
  },
  "semanticResult": null,
  "errors": [],
  "console": []
}
```

No crear un response paralelo para MongoDB/Cassandra.

---

# 14. Seguridad

Reglas obligatorias:

```text
No devolver password.
No loguear password.
No imprimir password en consola.
No guardar credenciales en variables estáticas.
No configurar credenciales en application.properties.
No ejecutar SQL/CQL/Mongo query del usuario.
```

En DTOs de conexión, usar:

```java
@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
@Schema(accessMode = Schema.AccessMode.WRITE_ONLY)
private String password;
```

---

# 15. Swagger/OpenAPI

Actualizar Swagger para reflejar MongoDB y Cassandra CQL.

Verificar:

```text
GET /api/compiler/openapi
GET /api/compiler/docs
```

El OpenAPI debe documentar:

```http
GET /api/compiler/health
GET /api/compiler/dialects
POST /api/compiler/analyze/lexical-syntax
POST /api/compiler/analyze/full
POST /api/compiler/connection/validate
```

Si se conserva `/connection/test`, debe marcarse como deprecated.

Agregar ejemplos para:

## MongoDB

```json
{
  "dialect": "MONGODB",
  "sql": "db.clientes.find({ estado: 1 })",
  "analysisMode": "LEXICAL_SYNTAX"
}
```

## Cassandra CQL

```json
{
  "dialect": "CASSANDRA_CQL",
  "sql": "SELECT id, nombre FROM clientes WHERE estado = 1 ALLOW FILTERING;",
  "analysisMode": "LEXICAL_SYNTAX"
}
```

Actualizar allowable values de dialectos:

```text
MYSQL
POSTGRESQL
SQL_SERVER
MONGODB
CASSANDRA_CQL
```

---

# 16. Documentación Markdown obligatoria

Actualizar o crear:

```text
docs/NOSQL_LEXICAL_SYNTAX.md
docs/API_COMPILER.md
docs/SWAGGER_OPENAPI.md
docs/SPRING_MIGRATION.md
```

La documentación debe indicar:

1. Motores NoSQL soportados.
2. Estado actual de soporte MongoDB.
3. Estado actual de soporte Cassandra CQL.
4. Endpoints.
5. Ejemplos curl.
6. Que el campo `sql` se conserva por compatibilidad aunque contenga instrucciones NoSQL.
7. Que la conexión es dinámica.
8. Que no hay autoconfiguración MongoDB/Cassandra al arranque.
9. Que no se devuelve password.
10. Cómo ejecutar pruebas.

---

# 17. Tests obligatorios

Crear o corregir tests.

Comando obligatorio antes de entregar:

```bash
mvn clean test
```

No entregar si falla.

---

## 17.1 Test de arranque Spring

En:

```text
src/test/java/com/umg/CompiladoresRefactApplicationTests.java
```

Debe verificar que Spring context carga sin requerir:

```text
DataSource
MongoClient automático
CqlSession automático
```

---

## 17.2 Tests MongoDB lexer/parser

Crear o corregir:

```text
src/test/java/com/umg/nosql/mongodb/MongoLexicalSyntaxTest.java
```

Casos mínimos:

```javascript
db.clientes.find({ estado: 1 })
db.clientes.findOne({ id: 1 })
db.clientes.insertOne({ nombre: "Kevin", estado: 1 })
db.clientes.updateOne({ id: 1 }, { $set: { nombre: "Kevin" } })
db.clientes.deleteOne({ id: 1 })
db.pedidos.aggregate([{ $match: { estado: "ACTIVO" } }])
```

Casos inválidos:

```javascript
db.clientes.save({ nombre: "Kevin" })
db.clientes.find({ estado: 1 )
db.clientes.find({ nombre: "Kevin })
```

Validar:

```text
valid = true/false según corresponda
executionStatus correcto
statementType correcto
semanticResult = null para lexical-syntax
connectionResult = null para lexical-syntax
```

---

## 17.3 Tests Cassandra CQL lexer/parser

Crear o corregir:

```text
src/test/java/com/umg/nosql/cassandra/CassandraCqlLexicalSyntaxTest.java
```

Casos mínimos:

```sql
SELECT id, nombre FROM clientes WHERE estado = 1;
SELECT * FROM keyspace.clientes WHERE id = 10;
INSERT INTO clientes (id, nombre) VALUES (1, 'Kevin');
UPDATE clientes SET nombre = 'Kevin' WHERE id = 1;
DELETE FROM clientes WHERE id = 1;
CREATE TABLE clientes (id UUID PRIMARY KEY, nombre TEXT);
CREATE KEYSPACE IF NOT EXISTS demo WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};
DROP TABLE clientes;
TRUNCATE clientes;
```

Casos inválidos:

```sql
SELECT FROM WHERE;
INSERT INTO clientes (nombre) VALUES ('Kevin);
CREATE TABLE clientes id UUID PRIMARY KEY;
```

---

## 17.4 Tests API

Actualizar:

```text
src/test/java/com/umg/api/compiler/CompilerControllerTest.java
```

Casos obligatorios:

```http
GET /api/compiler/health
GET /api/compiler/dialects
POST /api/compiler/analyze/lexical-syntax con MYSQL
POST /api/compiler/analyze/lexical-syntax con MONGODB
POST /api/compiler/analyze/lexical-syntax con CASSANDRA_CQL
POST /api/compiler/analyze/full con analysisMode incorrecto
POST /api/compiler/connection/validate con request inválido
```

---

## 17.5 Tests Swagger/OpenAPI

Actualizar:

```text
src/test/java/com/umg/api/compiler/OpenApiDocumentationTest.java
```

Validar que OpenAPI contenga:

```text
/api/compiler/health
/api/compiler/dialects
/api/compiler/analyze/lexical-syntax
/api/compiler/analyze/full
/api/compiler/connection/validate
MONGODB
CASSANDRA_CQL
```

Si `/connection/test` se conserva como alias, validar que no sea la ruta principal documentada.

---

## 17.6 Tests de seguridad password

Agregar test que verifique que las respuestas no contienen:

```text
password
123456
secret
```

para endpoints de conexión.

---

# 18. Revisión de Java 17

El proyecto debe correr con Java 17.

Verificar:

```text
pom.xml -> java.version = 17
maven.compiler.source = 17
maven.compiler.target = 17
```

En la documentación, indicar que IntelliJ debe usar:

```text
Project SDK: Java 17
Run Configuration JRE: Java 17
Maven Runner JRE: Java 17
```

---

# 19. Criterios de aceptación

La corrección se considera completa si:

1. El proyecto levanta sin error de MongoAutoConfiguration.
2. El proyecto no intenta crear MongoClient automático al arrancar.
3. El proyecto no intenta crear CqlSession automático al arrancar.
4. El proyecto no intenta crear DataSource automático al arrancar.
5. No hay referencias a `com.dataquery`.
6. No hay Swing.
7. `mvn clean test` pasa.
8. `/api/compiler/health` responde.
9. `/api/compiler/docs` abre Swagger.
10. `/api/compiler/openapi` responde JSON.
11. `/api/compiler/dialects` lista `MONGODB` y `CASSANDRA_CQL`.
12. `/api/compiler/analyze/lexical-syntax` funciona con MongoDB.
13. `/api/compiler/analyze/lexical-syntax` funciona con Cassandra CQL.
14. SQL sigue funcionando.
15. `/api/compiler/connection/validate` existe.
16. `/api/compiler/connection/test` no es la ruta principal o está deprecada.
17. No se devuelve password.
18. La documentación fue actualizada.

---

# 20. Orden recomendado de trabajo

Trabajar en este orden:

1. Revisar `pom.xml`.
2. Corregir dependencias MongoDB.
3. Excluir autoconfiguraciones en `CompiladoresRefactApplication`.
4. Ejecutar prueba rápida de arranque.
5. Revisar dialectos y estandarizar `CASSANDRA_CQL`.
6. Revisar `CompilerAnalyzeRequest`.
7. Revisar `CompilerAnalyzeResponse`.
8. Revisar `CompilerController`.
9. Mover lógica de conexión fuera del controller.
10. Corregir endpoint `/connection/validate`.
11. Revisar Mongo lexer/parser.
12. Revisar Cassandra CQL parser.
13. Corregir flujo `/analyze/lexical-syntax`.
14. Corregir flujo `/analyze/full`.
15. Actualizar Swagger.
16. Actualizar documentación `.md`.
17. Crear/corregir tests.
18. Ejecutar `mvn clean test`.
19. Corregir todos los fallos.
20. Entregar resumen técnico de cambios.

---

# 21. Resumen final para el agente

Corrige la implementación actual de MongoDB y Cassandra CQL.

El servicio actualmente falla al arrancar por autoconfiguración automática de MongoDB y posible mezcla de versiones del driver.

Debes:

```text
- mantener package com.umg;
- corregir dependencias;
- excluir Mongo/Cassandra/DataSource autoconfiguration;
- estabilizar dialectos MONGODB y CASSANDRA_CQL;
- corregir endpoint /connection/validate;
- asegurar análisis léxico/sintáctico MongoDB;
- asegurar análisis léxico/sintáctico Cassandra CQL;
- no romper SQL;
- actualizar Swagger;
- actualizar documentación;
- crear pruebas;
- ejecutar mvn clean test.
```

No entregues como finalizado si el servicio no levanta o si los tests fallan.
