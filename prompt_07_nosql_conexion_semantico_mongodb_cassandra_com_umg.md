# PROMPT 07 PARA AGENTE PROGRAMADOR
## Conexión dinámica + análisis semántico NoSQL para MongoDB y Cassandra CQL

> **Proyecto:** Compilador SQL/NoSQL UMG  
> **Rama base revisada:** `main`  
> **Package raíz obligatorio:** `com.umg`  
> **JDK objetivo:** 17  
> **Gestor:** Maven  
> **Arquitectura actual:** Spring Boot REST API + Swagger/OpenAPI  
> **Ruta base API:** `/api/compiler`  
> **Motores NoSQL objetivo:** `MONGODB` y `CASSANDRA_CQL`  
> **Objetivo:** Implementar y corregir la conexión dinámica y el análisis semántico completo para MongoDB y Cassandra CQL, integrado al flujo existente de `/connection/validate` y `/analyze/full`.

---

# 1. Estado actual del proyecto revisado

El proyecto recibido corresponde a la versión Spring/Swagger correcta y mantiene el estándar:

```java
package com.umg;
```

La estructura actual contiene:

```text
src/main/java/com/umg/
├── CompiladoresRefactApplication.java
├── api/
│   └── compiler/
│       ├── CompilerController.java
│       ├── dto/
│       └── mapper/
├── application/
│   └── compiler/
│       ├── CompilerConsoleBuilder.java
│       ├── ConnectionValidationService.java
│       ├── DialectAnalysisRouter.java
│       ├── LexicalSyntaxAnalysisService.java
│       └── NoSqlLexicalSyntaxAnalysisService.java
├── config/
│   ├── OpenApiConfig.java
│   └── WebConfig.java
├── exception/
└── model/
    ├── dialect/
    ├── lexer/
    ├── parser/
    ├── mongo/
    ├── nosql/
    └── semantic/
```

También existen estas piezas importantes:

```text
com.umg.model.dialect.CompilerDialect
com.umg.model.dialect.SqlDialect
com.umg.model.dialect.NoSqlDialect
com.umg.model.dialect.DialectMapper

com.umg.model.mongo.*
com.umg.model.nosql.mongodb.*
com.umg.model.nosql.cassandra.*

com.umg.model.semantic.AnalizadorMongo
com.umg.model.semantic.AnalizadorCql
com.umg.model.semantic.metadata.MongoConnectionFactory
com.umg.model.semantic.metadata.MongoDatabaseMetadataService
com.umg.model.semantic.metadata.CqlConnectionFactory
com.umg.model.semantic.metadata.CqlDatabaseMetadataService
com.umg.model.semantic.validator.SemanticValidatorMongo
com.umg.model.semantic.validator.SemanticValidatorCql
```

El proyecto ya tiene Swagger:

```text
src/main/java/com/umg/config/OpenApiConfig.java
docs/SWAGGER_OPENAPI.md
```

---

# 2. Problemas detectados que deben corregirse

## 2.1 Inconsistencia de dialectos

Actualmente el request principal usa:

```java
CompilerDialect
```

pero `ConnectionConfigDto` todavía usa:

```java
SqlDialect
```

Esto genera inconsistencias para NoSQL, porque:

```text
CompilerDialect.CASSANDRA_CQL
```

no coincide directamente con:

```text
SqlDialect.CASSANDRA
```

Corrección requerida:

```text
El API público debe usar CompilerDialect.
La lógica interna puede convertir a SqlDialect únicamente donde sea necesario.
```

Valores oficiales de API:

```text
MYSQL
POSTGRESQL
SQL_SERVER
MONGODB
CASSANDRA_CQL
```

No documentar `CASSANDRA` como valor oficial externo.

---

## 2.2 Endpoint dialects desactualizado

Actualmente `/api/compiler/dialects` todavía puede devolver algo parecido a:

```json
{
  "supportedDialects": ["MYSQL", "POSTGRESQL", "SQL_SERVER"],
  "futureDialects": ["MONGODB", "CASSANDRA"]
}
```

Debe corregirse a:

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

---

## 2.3 `/analyze/full` no debe cambiar analysisMode automáticamente

Actualmente existe lógica similar a:

```java
if (request.getAnalysisMode() == AnalysisMode.LEXICAL_ONLY
    || request.getAnalysisMode() == AnalysisMode.LEXICAL_SYNTAX) {
    request.setAnalysisMode(AnalysisMode.FULL);
}
```

Esto debe eliminarse.

Regla oficial:

```text
POST /api/compiler/analyze/full exige analysisMode = FULL.
Si recibe otro modo, debe responder 400 Bad Request.
```

No modificar silenciosamente el request del usuario.

---

## 2.4 `/analyze/lexical-syntax` no debe cambiar FULL a LEXICAL_SYNTAX

Actualmente existe lógica similar a:

```java
if (request.getAnalysisMode() == AnalysisMode.SEMANTIC_ONLY
    || request.getAnalysisMode() == AnalysisMode.FULL) {
    request.setAnalysisMode(AnalysisMode.LEXICAL_SYNTAX);
}
```

Esto debe eliminarse.

Regla oficial:

```text
POST /api/compiler/analyze/lexical-syntax solo acepta LEXICAL_ONLY o LEXICAL_SYNTAX.
Si recibe FULL o SEMANTIC_ONLY, debe responder 400 Bad Request.
```

---

## 2.5 Conexión MongoDB desde frontend

La pantalla actual permite seleccionar `MongoDB`, pero muestra puerto `3306`.

Para MongoDB el puerto por defecto debe ser:

```text
27017
```

Para Cassandra CQL:

```text
9042
```

Para SQL:

```text
MYSQL      -> 3306
POSTGRESQL -> 5432
SQL_SERVER -> 1433
```

El backend no depende del frontend, pero debe documentar estos defaults y aceptar explícitamente el puerto enviado.

Si el frontend no envía puerto, el backend puede asignar default por dialecto.

---

## 2.6 MongoDB aggregate con pipeline puro

La pantalla actual muestra una consulta MongoDB como pipeline puro:

```javascript
[
  { "$match": { "status": "active" } },
  { "$group": { "_id": "$category", "total": { "$sum": 1 } } }
]
```

Ese formato no contiene nombre de colección.

Para análisis semántico MongoDB completo, el backend necesita saber contra qué colección validar campos.

Regla requerida:

El backend debe soportar dos formatos para MongoDB:

### Formato A: instrucción completa

```javascript
db.orders.aggregate([
  { "$match": { "status": "active" } },
  { "$group": { "_id": "$category", "total": { "$sum": 1 } } }
])
```

En este caso, la colección se infiere de:

```text
orders
```

### Formato B: pipeline puro

```javascript
[
  { "$match": { "status": "active" } },
  { "$group": { "_id": "$category", "total": { "$sum": 1 } } }
]
```

En este caso, el request debe poder enviar explícitamente:

```json
"targetCollection": "orders"
```

o dentro de `connectionConfig`:

```json
"collection": "orders"
```

Implementar uno de estos dos campos, documentarlo y usarlo.

Recomendación:

```java
CompilerAnalyzeRequest.targetCollection
```

para no mezclar colección con datos de conexión.

Si el input es pipeline puro y no viene `targetCollection`, entonces:

```text
- validar conexión y base de datos;
- validar sintaxis y stages;
- semanticResult.valid puede ser true con warning, o false según criterio;
- debe agregar warning: "No se proporcionó colección objetivo; no fue posible validar existencia de campos."
```

---

# 3. Objetivo de esta fase

Implementar el análisis semántico completo para NoSQL:

```text
MONGODB
CASSANDRA_CQL
```

integrado a:

```http
POST /api/compiler/connection/validate
POST /api/compiler/analyze/full
```

El flujo debe quedar así:

```text
/analyze/lexical-syntax:
  - Solo léxico/sintáctico.
  - No abre conexión.
  - semanticResult = null.
  - connectionResult = null.

/connection/validate:
  - Solo valida conexión.
  - No analiza query.
  - No ejecuta query del usuario.

/analyze/full:
  - Valida request.
  - Ejecuta léxico/sintáctico.
  - Si falla, no abre conexión.
  - Si pasa, valida conexión.
  - Ejecuta semántico.
  - Devuelve respuesta completa.
```

---

# 4. Reglas obligatorias

## 4.1 Package raíz

Todo debe mantenerse bajo:

```java
com.umg
```

No deben existir referencias a:

```text
com.dataquery
com.umg.comdataquery
dataquery.sqlcompiler
javax.swing
org.netbeans.lib.awtextra
```

Antes de finalizar ejecutar:

```bash
grep -R "com.dataquery" src pom.xml
grep -R "com.umg.comdataquery" src pom.xml
grep -R "dataquery.sqlcompiler" src pom.xml
grep -R "javax.swing" src/main/java
grep -R "org.netbeans.lib.awtextra" src/main/java
```

---

## 4.2 No autoconfiguración automática

La app debe seguir excluyendo autoconfiguraciones:

```java
@SpringBootApplication(exclude = {
    DataSourceAutoConfiguration.class,
    MongoAutoConfiguration.class,
    MongoDataAutoConfiguration.class,
    CassandraAutoConfiguration.class,
    CassandraDataAutoConfiguration.class
})
```

No configurar conexiones fijas en `application.properties`.

No usar:

```properties
spring.datasource.url=
spring.data.mongodb.uri=
spring.cassandra.contact-points=
spring.cassandra.keyspace-name=
```

---

## 4.3 No ejecutar consultas del usuario

El compilador analiza y valida, no ejecuta operaciones de negocio.

Prohibido ejecutar directamente:

```text
SQL del usuario
CQL del usuario
Mongo query/pipeline del usuario
```

Permitido para semántico:

```text
metadata JDBC
system_schema de Cassandra
listDatabaseNames / listCollectionNames de MongoDB
consulta limitada de un documento de muestra o schema validator para inferir campos
```

MongoDB puede usar lectura de metadata o muestra controlada, pero no debe ejecutar el pipeline del usuario.

Cassandra puede consultar `system_schema`, pero no debe ejecutar el CQL del usuario.

---

## 4.4 Seguridad

No devolver password.

No loguear password.

No imprimir password en consola.

No incluir password en `toString()`.

Marcar password como write-only:

```java
@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
@Schema(accessMode = Schema.AccessMode.WRITE_ONLY)
private String password;
```

---

# 5. Cambios requeridos en DTOs

## 5.1 `ConnectionConfigDto`

Actualizar para usar `CompilerDialect` o crear un DTO externo que use `CompilerDialect`.

Recomendación:

```java
private CompilerDialect dialect;
```

en lugar de:

```java
private SqlDialect dialect;
```

Campos requeridos:

```java
private CompilerDialect dialect;
private String host;
private Integer port;
private String database;
private String schema;
private String username;
private String password;
private String jdbcUrl;
private Boolean useDirectJdbcUrl;
private String localDatacenter;
```

Notas:

- `database` significa:
  - MySQL/PostgreSQL/SQL Server: base de datos.
  - MongoDB: database.
  - Cassandra: keyspace.
- `schema` aplica principalmente a PostgreSQL/SQL Server.
- `localDatacenter` aplica a Cassandra. Si no viene, default: `datacenter1`.

Swagger debe documentar:

```text
MongoDB default port: 27017
Cassandra default port: 9042
```

---

## 5.2 `CompilerAnalyzeRequest`

Agregar si no existe:

```java
private String targetCollection;
```

Uso:

```text
Solo aplica para MongoDB cuando el input sea pipeline puro.
```

Ejemplo:

```json
{
  "dialect": "MONGODB",
  "sql": [
    { "$match": { "status": "active" } }
  ],
  "targetCollection": "orders"
}
```

Como `sql` actualmente es String, el JSON real debe enviar el pipeline como string:

```json
{
  "dialect": "MONGODB",
  "sql": "[{ \"$match\": { \"status\": \"active\" } }]",
  "targetCollection": "orders",
  "analysisMode": "FULL"
}
```

---

# 6. Servicios requeridos

## 6.1 `ConnectionValidationService`

Refactorizar para recibir `CompilerDialect`.

Método recomendado:

```java
public ConnectionValidationResult validateConnection(CompilerDialect dialect, ConnectionConfigDto config)
```

Si se mantiene `Map<String,Object>`, estandarizar el contenido.

Debe soportar:

```text
MYSQL
POSTGRESQL
SQL_SERVER
MONGODB
CASSANDRA_CQL
```

Debe devolver:

```json
{
  "connected": true,
  "dialect": "MONGODB",
  "database": "orders_db",
  "schema": null,
  "host": "localhost",
  "port": 27017,
  "driver": "MongoDB Java Driver",
  "message": "Conexión validada correctamente."
}
```

No devolver password.

---

## 6.2 `NoSqlSemanticAnalysisService`

Crear:

```text
src/main/java/com/umg/application/compiler/NoSqlSemanticAnalysisService.java
```

Responsabilidad:

```text
Ejecutar análisis semántico NoSQL para MongoDB y Cassandra CQL.
```

Método recomendado:

```java
public SemanticResultDto analyze(
    CompilerAnalyzeRequest request,
    CompilerAnalyzeResponse lexicalSyntaxResponse
)
```

Debe:

```text
- validar dialecto;
- construir config de conexión;
- validar conexión;
- invocar AnalizadorMongo o AnalizadorCql;
- mapear ResultadoSemantico a SemanticResultDto;
- agregar warnings cuando aplique;
- no ejecutar query del usuario.
```

---

## 6.3 `CompilerFacadeService`

Crear o completar:

```text
src/main/java/com/umg/application/compiler/CompilerFacadeService.java
```

Debe coordinar `/analyze/full` para SQL y NoSQL.

Flujo:

```text
1. Validar analysisMode = FULL.
2. Validar SQL/instrucción no vacía.
3. Ejecutar route léxico/sintáctico.
4. Si hay error léxico/sintáctico, retornar sin conexión.
5. Validar connectionConfig.
6. Validar conexión.
7. Si conexión falla, retornar CONNECTION_ERROR.
8. Si dialecto SQL, ejecutar semántico SQL existente.
9. Si dialecto MONGODB, ejecutar semántico MongoDB.
10. Si dialecto CASSANDRA_CQL, ejecutar semántico Cassandra.
11. Mapear respuesta.
12. Agregar consola.
```

El controller debe delegar a este servicio.

---

# 7. MongoDB - conexión dinámica

## 7.1 `MongoConnectionFactory`

Revisar:

```text
src/main/java/com/umg/model/semantic/metadata/MongoConnectionFactory.java
```

Debe soportar:

### Sin autenticación

```text
mongodb://host:port
```

### Con autenticación

```text
mongodb://username:password@host:port/database?authSource=database
```

### URI directa

Si `useDirectJdbcUrl = true`, usar `jdbcUrl` aunque el nombre no sea ideal.

Recomendación: documentar que `jdbcUrl` puede actuar como URI directa para MongoDB por compatibilidad.

No loguear URI con password.

---

## 7.2 MongoDB puerto default

Si `port` no viene:

```text
27017
```

---

## 7.3 Validación de conexión MongoDB

`/connection/validate` para MongoDB debe:

```text
- crear MongoClient dinámico;
- seleccionar database;
- ejecutar comando seguro de ping o listar colecciones;
- cerrar cliente;
- devolver connected true/false.
```

Comando permitido:

```java
database.runCommand(new Document("ping", 1));
```

No ejecutar pipeline del usuario.

---

# 8. MongoDB - análisis semántico

Debe validar como mínimo:

## 8.1 Database

Validar que la conexión al database indicado funcione.

Si el usuario tiene permisos para listar databases, validar existencia.

Si no tiene permiso, aceptar conexión al database y agregar warning.

## 8.2 Collection

Para instrucciones completas:

```javascript
db.orders.find(...)
db.orders.aggregate(...)
```

extraer:

```text
orders
```

Para pipeline puro:

```javascript
[{ "$match": { "status": "active" } }]
```

usar:

```text
request.targetCollection
```

Si no hay colección:

```text
warning: No se proporcionó colección objetivo; no se validaron campos contra una colección real.
```

## 8.3 Campos

Validar campos usados en:

```text
find filter
find projection
$match
$group _id
$group acumuladores
$project
$sort
$lookup localField/foreignField
update $set/$unset/$inc
delete filter
```

Como MongoDB es flexible, la validación debe ser prudente:

Estrategia aceptada:

```text
1. Intentar obtener schema validator de la colección.
2. Si no existe schema validator, inferir campos desde un documento de muestra.
3. Si no hay documentos, no marcar error; agregar warning.
```

No bloquear como error si no se puede inferir campo por falta de documentos.

Error solo cuando:

```text
- collection no existe;
- operador no soportado;
- método no soportado;
- campo contradice schema validator explícito;
- lookup referencia una colección inexistente.
```

Warning cuando:

```text
- no hay schema validator;
- colección vacía;
- campo no verificable con certeza;
- permisos insuficientes para listar metadata.
```

---

# 9. MongoDB - DTO semántico esperado

`semanticResult` debe incluir:

```json
{
  "valid": true,
  "message": "Análisis semántico MongoDB completado.",
  "validatedObjects": {
    "databases": [
      {
        "name": "orders_db",
        "exists": true,
        "message": "Base de datos accesible."
      }
    ],
    "collections": [
      {
        "name": "orders",
        "exists": true,
        "message": "Colección encontrada."
      }
    ],
    "fields": [
      {
        "collection": "orders",
        "name": "status",
        "exists": true,
        "source": "SAMPLE_DOCUMENT",
        "message": "Campo inferido desde documento de muestra."
      }
    ],
    "operators": [
      {
        "name": "$match",
        "valid": true,
        "message": "Stage soportado."
      }
    ]
  },
  "errors": [],
  "warnings": []
}
```

Si los DTO actuales no tienen `validatedObjects`, agregarlos sin romper SQL.

---

# 10. Cassandra CQL - conexión dinámica

## 10.1 `CqlConnectionFactory`

Revisar:

```text
src/main/java/com/umg/model/semantic/metadata/CqlConnectionFactory.java
```

Debe soportar:

```text
host
port
keyspace/database
username
password
localDatacenter
```

Default:

```text
port = 9042
localDatacenter = datacenter1
```

Si el usuario no envía keyspace, se puede conectar sin keyspace para validar conexión, pero el análisis semántico de tablas debe requerir keyspace.

---

## 10.2 Validación de conexión Cassandra

`/connection/validate` para Cassandra debe:

```text
- crear CqlSession dinámico;
- ejecutar SELECT release_version FROM system.local;
- cerrar sesión;
- devolver connected true/false.
```

No ejecutar CQL del usuario.

---

# 11. Cassandra CQL - análisis semántico

Debe validar como mínimo:

## 11.1 Keyspace

Validar que exista el keyspace indicado en `connectionConfig.database`.

Consultar:

```sql
system_schema.keyspaces
```

## 11.2 Tables

Validar tablas usadas en:

```text
SELECT FROM
INSERT INTO
UPDATE
DELETE FROM
ALTER TABLE
DROP TABLE
TRUNCATE
```

Consultar:

```sql
system_schema.tables
```

## 11.3 Columns

Validar columnas usadas en:

```text
SELECT
WHERE
INSERT columns
UPDATE SET
DELETE specific columns
ORDER BY
```

Consultar:

```sql
system_schema.columns
```

## 11.4 Primary key / partition key

Validar reglas básicas de Cassandra:

```text
- WHERE debería incluir partition key para SELECT/UPDATE/DELETE cuando aplique.
- Si la consulta usa columnas no clave en WHERE sin ALLOW FILTERING, generar warning o error según criterio.
- ALLOW FILTERING debe generar warning de rendimiento.
```

No ejecutar el CQL del usuario.

---

# 12. Cassandra CQL - DTO semántico esperado

Ejemplo:

```json
{
  "valid": true,
  "message": "Análisis semántico Cassandra CQL completado.",
  "validatedObjects": {
    "keyspaces": [
      {
        "name": "demo",
        "exists": true,
        "message": "Keyspace encontrado."
      }
    ],
    "tables": [
      {
        "name": "clientes",
        "exists": true,
        "keyspace": "demo",
        "message": "Tabla encontrada."
      }
    ],
    "columns": [
      {
        "table": "clientes",
        "name": "id",
        "exists": true,
        "kind": "partition_key",
        "message": "Columna encontrada."
      }
    ],
    "rules": [
      {
        "code": "ALLOW_FILTERING_WARNING",
        "severity": "WARNING",
        "message": "ALLOW FILTERING puede impactar rendimiento."
      }
    ]
  },
  "errors": [],
  "warnings": []
}
```

---

# 13. Corrección de `SemanticResultDto`

Actualmente `SemanticResultDto` solo contiene:

```text
valid
message
errors
warnings
```

Agregar soporte para objetos validados:

```java
private Map<String, Object> validatedObjects;
```

o DTOs específicos:

```text
ValidatedObjectsDto
DatabaseValidationDto
CollectionValidationDto
FieldValidationDto
KeyspaceValidationDto
TableValidationDto
ColumnValidationDto
RuleValidationDto
OperatorValidationDto
```

Recomendación para rapidez y flexibilidad:

```java
private Map<String, Object> validatedObjects;
```

pero documentarlo en Swagger.

No romper el response SQL existente.

---

# 14. Corrección de `CompilerResponseMapper`

Actualizar:

```text
src/main/java/com/umg/api/compiler/mapper/CompilerResponseMapper.java
```

Debe mapear:

```text
ResultadoSemantico -> SemanticResultDto
ErrorSemantico -> CompilerErrorDto
validatedObjects -> DTO o Map
warnings -> warnings
```

Para SQL, MongoDB y Cassandra.

Si `ResultadoSemantico` no tiene validatedObjects, agregar un campo flexible:

```java
private Map<String, Object> objetosValidados;
```

con getters/setters.

---

# 15. Endpoint `/connection/validate`

Debe aceptar:

## 15.1 MongoDB

```json
{
  "dialect": "MONGODB",
  "host": "localhost",
  "port": 27017,
  "database": "orders_db",
  "username": "",
  "password": "",
  "schema": null
}
```

También aceptar con credenciales:

```json
{
  "dialect": "MONGODB",
  "host": "localhost",
  "port": 27017,
  "database": "orders_db",
  "username": "admin",
  "password": "secret"
}
```

## 15.2 Cassandra

```json
{
  "dialect": "CASSANDRA_CQL",
  "host": "localhost",
  "port": 9042,
  "database": "demo",
  "username": "cassandra",
  "password": "cassandra",
  "localDatacenter": "datacenter1"
}
```

Respuesta estándar:

```json
{
  "valid": true,
  "message": "Conexión validada correctamente.",
  "executionStatus": "SUCCESS",
  "connectionResult": {
    "connected": true,
    "dialect": "MONGODB",
    "database": "orders_db",
    "host": "localhost",
    "port": 27017,
    "message": "Conexión establecida correctamente."
  },
  "errors": [],
  "console": []
}
```

Si falla conexión:

```text
HTTP 200
valid = false
executionStatus = CONNECTION_ERROR
```

Si request inválido:

```text
HTTP 400
executionStatus = INVALID_REQUEST
```

---

# 16. Endpoint `/analyze/full`

## 16.1 MongoDB full con instrucción completa

Request:

```json
{
  "dialect": "MONGODB",
  "sql": "db.orders.aggregate([{ \"$match\": { \"status\": \"active\" } }, { \"$group\": { \"_id\": \"$category\", \"total\": { \"$sum\": 1 } } }])",
  "analysisMode": "FULL",
  "connectionConfig": {
    "dialect": "MONGODB",
    "host": "localhost",
    "port": 27017,
    "database": "orders_db",
    "username": "",
    "password": ""
  }
}
```

## 16.2 MongoDB full con pipeline puro

Request:

```json
{
  "dialect": "MONGODB",
  "sql": "[{ \"$match\": { \"status\": \"active\" } }, { \"$group\": { \"_id\": \"$category\", \"total\": { \"$sum\": 1 } } }]",
  "targetCollection": "orders",
  "analysisMode": "FULL",
  "connectionConfig": {
    "dialect": "MONGODB",
    "host": "localhost",
    "port": 27017,
    "database": "orders_db"
  }
}
```

## 16.3 Cassandra full

Request:

```json
{
  "dialect": "CASSANDRA_CQL",
  "sql": "SELECT id, nombre FROM clientes WHERE id = 1;",
  "analysisMode": "FULL",
  "connectionConfig": {
    "dialect": "CASSANDRA_CQL",
    "host": "localhost",
    "port": 9042,
    "database": "demo",
    "username": "cassandra",
    "password": "cassandra",
    "localDatacenter": "datacenter1"
  }
}
```

---

# 17. Frontend: observaciones para contrato API

La pantalla actual funciona como base, pero debe respetar estos puntos:

1. Cuando el usuario seleccione `MongoDB`, el puerto sugerido debe cambiar a `27017`.
2. Cuando seleccione `Cassandra CQL`, el puerto sugerido debe cambiar a `9042`.
3. Cuando seleccione `MySQL`, usar `3306`.
4. Cuando seleccione `PostgreSQL`, usar `5432`.
5. Cuando seleccione `SQL Server`, usar `1433`.
6. Para MongoDB pipeline puro debe existir forma de enviar `targetCollection`.
7. El label `Base de datos` puede conservarse:
   - MongoDB: database.
   - Cassandra: keyspace.
   - SQL: database.
8. El backend debe validar aunque el frontend mande datos incompletos.

Si el frontend todavía no tiene campo `targetCollection`, documentar que para análisis semántico MongoDB completo se debe usar instrucción completa:

```javascript
db.orders.aggregate([...])
```

o agregar el campo en el request.

---

# 18. Swagger/OpenAPI

Actualizar Swagger para documentar:

```text
MONGODB
CASSANDRA_CQL
```

en:

```text
CompilerAnalyzeRequest
ConnectionConfigDto
/api/compiler/dialects
/api/compiler/connection/validate
/api/compiler/analyze/full
```

Agregar ejemplos:

```text
MongoDB connection validate
MongoDB full con db.collection.aggregate
MongoDB full con pipeline puro + targetCollection
Cassandra connection validate
Cassandra full SELECT
Cassandra full CREATE TABLE
```

Asegurar:

```text
password writeOnly = true
```

---

# 19. Documentación Markdown obligatoria

Actualizar o crear:

```text
docs/NOSQL_SEMANTIC_API.md
docs/NOSQL_LEXICAL_SYNTAX.md
docs/API_COMPILER.md
docs/SWAGGER_OPENAPI.md
docs/SPRING_MIGRATION.md
```

`docs/NOSQL_SEMANTIC_API.md` debe incluir:

1. Objetivo.
2. Motores soportados.
3. Endpoints.
4. Formato de conexión MongoDB.
5. Formato de conexión Cassandra.
6. MongoDB con instrucción completa.
7. MongoDB con pipeline puro y `targetCollection`.
8. Cassandra CQL.
9. Ejemplos curl.
10. Reglas de seguridad.
11. Qué se valida semánticamente.
12. Qué no se ejecuta.
13. Limitaciones.

---

# 20. Tests obligatorios

Antes de entregar:

```bash
mvn clean test
```

No entregar si falla.

---

## 20.1 Tests de arranque

Verificar que Spring levanta sin crear automáticamente:

```text
DataSource
MongoClient
CqlSession
```

---

## 20.2 Tests `/connection/validate`

Crear o corregir tests para:

```text
MYSQL request inválido
MONGODB request válido usando mock/stub
MONGODB request inválido
CASSANDRA_CQL request válido usando mock/stub
CASSANDRA_CQL request inválido
password no aparece en response
```

No depender de bases reales en tests automáticos.

---

## 20.3 Tests `/analyze/full` MongoDB

Casos:

```javascript
db.orders.find({ status: "active" })
db.orders.aggregate([{ $match: { status: "active" } }])
[{ "$match": { "status": "active" } }]
```

Validar:

```text
LEXICAL/SYNTAX primero
connection después
semanticResult != null
connectionResult != null
no password en response
```

Para pipeline puro sin `targetCollection`, validar warning.

---

## 20.4 Tests `/analyze/full` Cassandra

Casos:

```sql
SELECT id, nombre FROM clientes WHERE id = 1;
INSERT INTO clientes (id, nombre) VALUES (1, 'Kevin');
UPDATE clientes SET nombre = 'Kevin' WHERE id = 1;
DELETE FROM clientes WHERE id = 1;
```

Con metadata mock:

```text
keyspace existe
tabla existe
columnas existen
partition key conocida
```

Validar:

```text
semanticResult.valid = true
connectionResult.connected = true
```

Casos inválidos:

```text
tabla inexistente
columna inexistente
keyspace inexistente
WHERE sin partition key -> warning/error según criterio
```

---

## 20.5 Tests de no ejecución

Agregar tests o validaciones de diseño para asegurar que no se llama:

```text
session.execute(cqlDelUsuario)
collection.aggregate(pipelineDelUsuario)
collection.find(filtroDelUsuario) con query completa del usuario
```

MongoDB puede leer un documento de muestra para inferencia, pero no debe ejecutar el pipeline del usuario.

---

## 20.6 Tests Swagger

OpenAPI debe contener:

```text
MONGODB
CASSANDRA_CQL
targetCollection
/api/compiler/connection/validate
/api/compiler/analyze/full
```

---

# 21. Criterios de aceptación

La fase se considera completa si:

1. El proyecto compila.
2. Spring Boot levanta.
3. `mvn clean test` pasa.
4. `com.umg` se mantiene como package raíz.
5. No hay Swing.
6. No hay `com.dataquery`.
7. `/api/compiler/dialects` lista SQL y NoSQL correctamente.
8. `/api/compiler/connection/validate` funciona para MongoDB.
9. `/api/compiler/connection/validate` funciona para Cassandra CQL.
10. `/api/compiler/analyze/full` funciona para MongoDB.
11. `/api/compiler/analyze/full` funciona para Cassandra CQL.
12. `/analyze/full` rechaza analysisMode distinto de `FULL`.
13. `/analyze/lexical-syntax` rechaza `FULL` y `SEMANTIC_ONLY`.
14. No se ejecutan queries del usuario.
15. No se devuelve password.
16. Swagger está actualizado.
17. Documentación `.md` está actualizada.
18. Hay tests de conexión.
19. Hay tests semánticos.
20. Hay tests de seguridad.

---

# 22. Orden recomendado de trabajo

1. Revisar `pom.xml`.
2. Confirmar exclusiones de autoconfiguración.
3. Revisar `CompilerDialect`, `SqlDialect`, `DialectMapper`.
4. Migrar `ConnectionConfigDto` a `CompilerDialect` o mapear formalmente.
5. Corregir `/dialects`.
6. Corregir validación de `analysisMode` en endpoints.
7. Crear/ajustar `CompilerFacadeService`.
8. Crear/ajustar `NoSqlSemanticAnalysisService`.
9. Corregir `ConnectionValidationService`.
10. Corregir `MongoConnectionFactory`.
11. Corregir `CqlConnectionFactory`.
12. Corregir metadata MongoDB.
13. Corregir metadata Cassandra.
14. Agregar `targetCollection`.
15. Actualizar DTOs semánticos.
16. Actualizar mapper.
17. Actualizar Swagger.
18. Actualizar documentación.
19. Crear tests.
20. Ejecutar `mvn clean test`.
21. Corregir fallos.
22. Entregar resumen técnico.

---

# 23. Resumen final para el agente

Implementa la conexión dinámica y el análisis semántico completo para:

```text
MONGODB
CASSANDRA_CQL
```

usando los endpoints:

```http
POST /api/compiler/connection/validate
POST /api/compiler/analyze/full
```

El backend debe seguir soportando SQL existente.

No ejecutes consultas del usuario.

No devuelvas password.

Actualiza Swagger y documentación.

Haz pruebas múltiples y corrige incidencias hasta que:

```bash
mvn clean test
```

pase correctamente.
