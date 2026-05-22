# Migracion a Spring Boot REST API

## Como arrancar el proyecto

```bash
mvn clean spring-boot:run
```

O compilar y ejecutar:

```bash
mvn clean package
java -jar target/sql-compiler-1.0.0.jar
```

La aplicacion arranca en el puerto `8080`.

## Endpoints implementados

| Metodo | URL | Descripcion |
|--------|-----|-------------|
| GET | `/api/compiler/health` | Health check del servicio |
| GET | `/api/compiler/dialects` | Dialectos SQL soportados |
| POST | `/api/compiler/analyze/lexical-syntax` | Analisis lexico y sintactico |
| POST | `/api/compiler/analyze/full` | Analisis completo (lexico + sintactico + semantico) |
| POST | `/api/compiler/connection/test` | Prueba de conexion JDBC |

## Analisis lexico-sintactico (sin BD)

```bash
curl -X POST http://localhost:8080/api/compiler/analyze/lexical-syntax \
  -H "Content-Type: application/json" \
  -d '{
    "dialect": "MYSQL",
    "sql": "SELECT id, nombre FROM clientes WHERE estado = 1;",
    "analysisMode": "LEXICAL_SYNTAX"
  }'
```

## Analisis completo con BD

```bash
curl -X POST http://localhost:8080/api/compiler/analyze/full \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "test-001",
    "dialect": "MYSQL",
    "sql": "SELECT id, nombre FROM usuarios WHERE activo = 1;",
    "analysisMode": "FULL",
    "connectionConfig": {
      "dialect": "MYSQL",
      "host": "localhost",
      "port": 3306,
      "database": "mi_bd",
      "username": "root",
      "password": ""
    }
  }'
```

## Prueba de conexion

```bash
curl -X POST http://localhost:8080/api/compiler/connection/test \
  -H "Content-Type: application/json" \
  -d '{
    "dialect": "MYSQL",
    "host": "localhost",
    "port": 3306,
    "database": "mi_bd",
    "username": "root",
    "password": ""
  }'
```

## Pruebas

```bash
mvn clean test
```

## Swagger / OpenAPI

Se implemento documentacion interactiva Swagger/OpenAPI como fase adicional.

- Swagger UI: `http://localhost:8080/api/compiler/docs`
- OpenAPI JSON: `http://localhost:8080/api/compiler/openapi`
- Dependencia: `springdoc-openapi-starter-webmvc-ui:2.8.17`
- Clase de configuracion: `com.umg.config.OpenApiConfig`

Ver `docs/SWAGGER_OPENAPI.md` para mas detalles.

## Notas importantes

- No se usa datasource fijo. La conexion a BD es dinamica y se configura por request.
- La funcionalidad Swing fue eliminada. Toda la logica se expone via REST.
- Package raiz: `com.umg`.
- JDK 17, Maven, Spring Boot 3.2.5.
- Dialectos soportados: MySQL, PostgreSQL, SQL Server.
# Actualizacion NoSQL Semantico

- Se agrego `CompilerFacadeService` para coordinar flujo FULL.
- Se agrego `NoSqlSemanticAnalysisService` para MongoDB y Cassandra CQL.
- Se mantiene exclusion de auto-configuracion para DataSource, Mongo y Cassandra.
- `ConnectionValidationService` ahora valida conexion por `CompilerDialect` y no expone password.
