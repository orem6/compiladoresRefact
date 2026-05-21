# Swagger / OpenAPI

## ¿Que es Swagger/OpenAPI?

Swagger UI es una interfaz grafica interactiva que permite explorar y probar los endpoints de la API REST del compilador SQL directamente desde el navegador, sin necesidad de herramientas externas como Postman o curl.

La especificacion OpenAPI se genera automaticamente a partir de las anotaciones en el codigo Java.

## Dependencia agregada

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.8.17</version>
</dependency>
```

## Rutas

| Recurso | URL |
|---------|-----|
| Swagger UI | `http://localhost:8080/api/compiler/docs` |
| OpenAPI JSON | `http://localhost:8080/api/compiler/openapi` |
| OpenAPI YAML | `http://localhost:8080/api/compiler/openapi.yaml` |

## Endpoints documentados

| Metodo | Endpoint | Descripcion |
|--------|----------|-------------|
| GET | `/api/compiler/health` | Verificar estado del servicio |
| GET | `/api/compiler/dialects` | Listar dialectos soportados |
| POST | `/api/compiler/analyze/lexical-syntax` | Analisis lexico y sintactico |
| POST | `/api/compiler/analyze/full` | Analisis completo (lexico + sintactico + semantico) |
| POST | `/api/compiler/connection/test` | Prueba de conexion JDBC |

## Como probar desde el navegador

1. Iniciar el servidor:
   ```bash
   mvn clean spring-boot:run
   ```
2. Abrir en el navegador: `http://localhost:8080/api/compiler/docs`
3. Explorar los endpoints disponibles, sus request bodies y responses.
4. Hacer clic en "Try it out" para probar cada endpoint directamente.

## Como probar desde curl

```bash
# Obtener el JSON OpenAPI
curl -s http://localhost:8080/api/compiler/openapi | python3 -m json.tool
```

```bash
# Obtener el YAML OpenAPI
curl -s http://localhost:8080/api/compiler/openapi.yaml
```

## Seguridad: campo password

El campo `password` en `ConnectionConfigDto` esta marcado como `writeOnly: true`.

Esto significa que:
- Swagger UI permite ingresar una contrasena en el formulario de request.
- La contrasena **nunca** aparece en las respuestas de la API.
- El campo esta documentado como sensible en el schema OpenAPI.

## Dialectos NoSQL en OpenAPI

A partir de la implementacion Prompt 05, la especificacion OpenAPI incluye:

- **CompilerDialect**: enum con valores `MYSQL`, `POSTGRESQL`, `SQL_SERVER`, `MONGODB`, `CASSANDRA_CQL`
- **Ejemplos en request body**:
  - `analyze/lexical-syntax`: 5 ejemplos (SELECT MySQL, MongoDB find, MongoDB aggregate, Cassandra SELECT, Cassandra CREATE TABLE)
  - `analyze/full`: Analisis completo MySQL (NoSQL devuelve "pendiente" para FULL/SEMANTIC_ONLY)
- **Dialects response**: incluye `sqlDialects` y `noSqlDialects` como arreglos separados

El schema `ConnectionConfigDto.dialect` usa `SqlDialect` (solo SQL), mientras que `CompilerAnalyzeRequest.dialect` y `CompilerAnalyzeResponse.dialect` usan `CompilerDialect` (SQL + NoSQL).

## Nota importante

La documentacion Swagger/OpenAPI es solo documentacion. No modifica ni afecta la logica del compilador (lexer, parser, semantico). Ninguna anotacion Swagger cambia el comportamiento de los endpoints.
