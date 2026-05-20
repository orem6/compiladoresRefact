# Migración a Spring Boot REST API

## Cómo arrancar el proyecto

```bash
mvn clean spring-boot:run
```

O compilar y ejecutar:

```bash
mvn clean package
java -jar target/sql-compiler-1.0.0.jar
```

La aplicación arranca en el puerto `8080`.

## Endpoints implementados en esta fase

| Método | URL | Descripción |
|--------|-----|-------------|
| GET | `/api/compiler/health` | Health check del servicio |
| GET | `/api/compiler/dialects` | Dialectos SQL soportados |
| POST | `/api/compiler/analyze/lexical-syntax` | Análisis léxico y sintáctico |

## Endpoints pendientes para fase semántica

| Método | URL | Descripción |
|--------|-----|-------------|
| POST | `/api/compiler/connection/test` | Prueba de conexión JDBC |
| POST | `/api/compiler/analyze/full` | Análisis completo (léxico + sintáctico + semántico) |

## Ejemplo de request

```bash
curl -X POST http://localhost:8080/api/compiler/analyze/lexical-syntax \
  -H "Content-Type: application/json" \
  -d '{
    "dialect": "MYSQL",
    "sql": "SELECT id, nombre FROM clientes WHERE estado = 1;",
    "analysisMode": "LEXICAL_SYNTAX",
    "options": {
      "includeCommentsAsTokens": true,
      "stopOnLexicalError": true,
      "stopOnSyntaxError": true,
      "returnTokenList": true,
      "returnConsoleOutput": true
    }
  }'
```

## Pruebas

```bash
mvn clean test
```

## Notas importantes

- No se usa datasource fijo. La conexión a BD será dinámica en la fase semántica.
- Swing fue eliminado como capa de ejecución. La lógica del compilador ahora se expone via REST.
- Package raíz: `com.umg`.
- JDK 17, Maven, Spring Boot 3.2.5.
