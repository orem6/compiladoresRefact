# PROMPT UNIVERSAL PARA AGENTE PROGRAMADOR  
## Migración del compilador SQL de Swing/MVC a Spring Boot REST API

> **Proyecto:** Compilador SQL UMG  
> **Package raíz obligatorio:** `com.umg`  
> **JDK:** 17  
> **Gestor:** Maven  
> **Arquitectura destino:** Spring Boot REST API  
> **Ruta base de API:** `/api/compiler`  
> **Estado actual:** Proyecto Java Swing/MVC con lógica ya implementada para análisis léxico, sintáctico y semántico.  
> **Objetivo de este documento:** Definir las reglas generales, arquitectura destino, dependencias, paquetes, endpoints base, formato JSON y criterios obligatorios para migrar el proyecto de Swing a Spring Boot sin romper la lógica existente.

---

# 1. Contexto del proyecto

El proyecto actual es un compilador/validador de sentencias SQL desarrollado en Java con Maven y JDK 17. Actualmente funciona con una vista Swing y una arquitectura tipo MVC.

El proyecto ya contiene lógica implementada para:

- Analizador léxico.
- Analizador sintáctico.
- Analizador semántico.
- Validación de tokens.
- Validación básica de estructura SQL.
- Validación semántica contra metadatos JDBC.
- Soporte inicial para motores SQL.

La migración debe convertir esta lógica en un servicio backend REST con Spring Boot.

La nueva arquitectura debe eliminar la dependencia de Swing y dejar toda la lógica disponible mediante endpoints HTTP que reciban y respondan JSON.

---

# 2. Reglas obligatorias del proyecto

## 2.1 Package raíz

El package raíz obligatorio es:

```java
com.umg
```

No debe existir ninguna referencia nueva ni residual a:

```java
com.dataquery.sqlcompiler
com.umg.comdataquery.sqlcompiler
dataquery.sqlcompiler
```

Antes de finalizar, buscar en todo el proyecto:

```bash
grep -R "com.dataquery" src pom.xml
grep -R "com.umg.comdataquery" src pom.xml
grep -R "dataquery.sqlcompiler" src pom.xml
```

Si aparece cualquiera de esas referencias, corregirlas.

---

## 2.2 JDK y Maven

El proyecto debe compilar con Java 17.

El `pom.xml` debe tener configuración explícita para Java 17:

```xml
<properties>
    <java.version>17</java.version>
    <maven.compiler.source>17</maven.compiler.source>
    <maven.compiler.target>17</maven.compiler.target>
</properties>
```

No usar características superiores a Java 17.

---

## 2.3 Eliminación de Swing

Se debe eliminar la parte Swing como capa de ejecución principal.

Eliminar o dejar fuera del flujo de compilación las clases de vista Swing, principalmente:

```text
src/main/java/com/umg/view/Visa_Compilador.java
src/main/java/com/umg/view/Visa_Compilador.form
```

También eliminar dependencias innecesarias relacionadas con Swing/NetBeans UI Designer, por ejemplo:

```java
javax.swing.*
org.netbeans.lib.awtextra.*
```

La aplicación final debe arrancar como Spring Boot, no como JFrame.

---

## 2.4 Aprovechamiento de la lógica existente

Aunque Swing debe eliminarse, **la lógica del `ViewController` debe revisarse y aprovecharse**.

El proyecto actual contiene:

```text
src/main/java/com/umg/controller/ViewController.java
```

Este archivo concentra parte importante del flujo actual de validación y presentación de resultados.

El agente debe revisar este controlador antes de implementar los servicios REST.

La lógica del `ViewController` debe migrarse a servicios de aplicación Spring, no copiarse ciegamente en el controller REST.

El controller REST no debe contener lógica pesada. Debe delegar en servicios.

Regla:

```text
ViewController actual = referencia de flujo y armado de respuesta.
Spring REST Controller nuevo = solo entrada/salida HTTP.
Servicios Spring = lógica real del compilador.
```

---

# 3. Estructura actual detectada

El proyecto correcto usa esta estructura base:

```text
compiladoresRefact/
├── pom.xml
├── docs/
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/
│   │           └── umg/
│   │               ├── controller/
│   │               │   └── ViewController.java
│   │               ├── model/
│   │               │   ├── ast/
│   │               │   ├── dialect/
│   │               │   ├── error/
│   │               │   ├── lexer/
│   │               │   ├── parser/
│   │               │   ├── semantic/
│   │               │   └── ViewModel.java
│   │               └── view/
│   │                   ├── Visa_Compilador.java
│   │                   └── Visa_Compilador.form
│   └── test/
│       └── java/
│           └── com/
│               └── umg/
```

Clases actuales que deben revisarse y reutilizarse:

```text
com.umg.model.lexer.AnalizadorSql
com.umg.model.lexer.ResultadoLexer
com.umg.model.lexer.Lexer
com.umg.model.lexer.Token
com.umg.model.lexer.TokenType
com.umg.model.parser.Parser
com.umg.model.dialect.SqlDialect
com.umg.model.dialect.KeywordRegistry
com.umg.model.dialect.FunctionRegistry
com.umg.model.error.CompilerError
com.umg.model.error.ErrorCollector
com.umg.model.semantic.AnalizadorSemanticoSql
com.umg.model.semantic.config.ConexionBaseDatosConfig
com.umg.model.semantic.metadata.JdbcConnectionFactory
com.umg.model.semantic.metadata.JdbcDatabaseMetadataService
com.umg.model.semantic.validator.SemanticValidator
com.umg.model.semantic.result.ResultadoSemantico
com.umg.model.semantic.result.ErrorSemantico
```

Antes de modificar cualquier clase, el agente debe abrirlas y confirmar sus métodos, constructores y responsabilidades reales.

No inventar firmas. Adaptarse a lo que ya exista.

---

# 4. Arquitectura destino en Spring Boot

La arquitectura destino debe separar claramente:

1. Capa REST.
2. DTOs de entrada/salida.
3. Servicios de aplicación.
4. Lógica de dominio ya existente.
5. Infraestructura para conexión dinámica a base de datos.
6. Manejo global de errores.

Estructura recomendada:

```text
src/main/java/com/umg/
├── CompiladoresRefactApplication.java
├── api/
│   └── compiler/
│       ├── CompilerController.java
│       ├── dto/
│       │   ├── CompilerAnalyzeRequest.java
│       │   ├── CompilerAnalyzeResponse.java
│       │   ├── ConnectionRequest.java
│       │   ├── CompilerOptionsRequest.java
│       │   ├── LexicalResultDto.java
│       │   ├── SyntaxResultDto.java
│       │   ├── SemanticResultDto.java
│       │   ├── CompilerErrorDto.java
│       │   ├── CompilerSummaryDto.java
│       │   └── ConnectionResultDto.java
│       └── mapper/
│           └── CompilerResponseMapper.java
├── application/
│   └── compiler/
│       ├── CompilerFacadeService.java
│       ├── LexicalSyntaxAnalysisService.java
│       ├── SemanticAnalysisService.java
│       └── ConnectionTestService.java
├── config/
│   ├── WebConfig.java
│   └── JacksonConfig.java
├── exception/
│   ├── GlobalExceptionHandler.java
│   ├── InvalidCompilerRequestException.java
│   └── UnsupportedDialectException.java
└── model/
    ├── ast/
    ├── dialect/
    ├── error/
    ├── lexer/
    ├── parser/
    └── semantic/
```

Notas:

- El package `model` puede conservarse si ya contiene la lógica funcional.
- La nueva API debe envolver la lógica existente, no reescribirla desde cero.
- El controller REST debe estar en `com.umg.api.compiler`.
- Los DTOs deben estar en `com.umg.api.compiler.dto`.
- Los servicios Spring deben estar en `com.umg.application.compiler`.
- Las excepciones REST deben estar en `com.umg.exception`.

---

# 5. Clase principal Spring Boot

Crear la clase principal:

```text
src/main/java/com/umg/CompiladoresRefactApplication.java
```

Contenido base esperado:

```java
package com.umg;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class CompiladoresRefactApplication {

    public static void main(String[] args) {
        SpringApplication.run(CompiladoresRefactApplication.class, args);
    }
}
```

Se debe excluir `DataSourceAutoConfiguration` porque el proyecto no tendrá una conexión fija al iniciar.

La conexión a base de datos será dinámica y vendrá desde el JSON del request.

Esto evita el error:

```text
Failed to configure a DataSource: 'url' attribute is not specified
Failed to determine a suitable driver class
```

---

# 6. Dependencias Maven requeridas

Actualizar el `pom.xml` para convertirlo en proyecto Spring Boot.

Dependencias mínimas obligatorias:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>

<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>

<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>

<dependency>
    <groupId>com.microsoft.sqlserver</groupId>
    <artifactId>mssql-jdbc</artifactId>
    <scope>runtime</scope>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

No agregar JPA en esta etapa salvo que sea estrictamente necesario.

Evitar:

```xml
spring-boot-starter-data-jpa
```

La validación semántica debe consultar metadatos mediante JDBC, no entidades JPA.

---

# 7. Configuración base

Crear o actualizar:

```text
src/main/resources/application.properties
```

Contenido recomendado:

```properties
spring.application.name=compiladores-refact-spring
server.port=8080

spring.jackson.serialization.indent-output=true

logging.level.com.umg=INFO
logging.level.org.springframework.web=INFO
```

No agregar:

```properties
spring.datasource.url=
spring.datasource.username=
spring.datasource.password=
```

La conexión es dinámica y debe venir desde el request JSON.

---

# 8. Motores soportados

La API debe soportar inicialmente:

```text
MYSQL
POSTGRESQL
SQL_SERVER
```

Debe dejarse margen claro para soporte futuro de motores NoSQL, por ejemplo:

```text
MONGODB
```

Pero MongoDB no debe implementarse todavía.

La estructura debe permitir agregar dialectos nuevos sin romper la API.

Regla:

```text
No quemar lógica específica de MySQL directamente en controllers.
Usar enums, estrategias o servicios por dialecto.
```

---

# 9. Ruta base de API

Toda la API debe estar bajo:

```http
/api/compiler
```

Endpoints generales que deben quedar definidos como estándar del proyecto:

## 9.1 Health check

```http
GET /api/compiler/health
```

Respuesta esperada:

```json
{
  "status": "UP",
  "service": "compiler-api",
  "version": "1.0.0"
}
```

---

## 9.2 Obtener dialectos soportados

```http
GET /api/compiler/dialects
```

Respuesta esperada:

```json
{
  "supportedDialects": [
    "MYSQL",
    "POSTGRESQL",
    "SQL_SERVER"
  ],
  "futureDialects": [
    "MONGODB"
  ]
}
```

---

## 9.3 Análisis léxico/sintáctico

Este endpoint será desarrollado en la fase específica del analizador léxico/sintáctico.

```http
POST /api/compiler/analyze/lexical-syntax
```

Debe ejecutar:

```text
Lexer + Parser
```

No debe requerir conexión a base de datos.

---

## 9.4 Prueba de conexión a base de datos

Este endpoint será desarrollado en la fase semántica.

```http
POST /api/compiler/connection/test
```

Debe validar si los datos JDBC permiten conexión a la base seleccionada.

---

## 9.5 Análisis completo

Este endpoint será desarrollado después de tener semántico integrado.

```http
POST /api/compiler/analyze/full
```

Debe ejecutar:

```text
Lexer + Parser + Semantic Analyzer
```

Debe requerir conexión a base de datos.

---

# 10. JSON estándar de request

El request estándar del backend debe tener esta forma general:

```json
{
  "requestId": "opcional-uuid-del-frontend",
  "dialect": "MYSQL",
  "sql": "SELECT id, nombre FROM clientes WHERE estado = 1;",
  "analysisMode": "LEXICAL_SYNTAX",
  "connection": {
    "host": "localhost",
    "port": 3306,
    "database": "mi_base",
    "username": "root",
    "password": "123456",
    "schema": null
  },
  "options": {
    "includeCommentsAsTokens": true,
    "validateSemantic": false,
    "stopOnLexicalError": true,
    "stopOnSyntaxError": true,
    "returnTokenList": true,
    "returnConsoleOutput": true
  }
}
```

Reglas:

- `dialect` es obligatorio.
- `sql` es obligatorio para análisis.
- `analysisMode` es obligatorio.
- `connection` es opcional para análisis léxico/sintáctico.
- `connection` será obligatorio para análisis semántico o completo.
- `password` nunca debe devolverse en la respuesta.

Valores válidos de `analysisMode`:

```text
LEXICAL_ONLY
LEXICAL_SYNTAX
SEMANTIC_ONLY
FULL
```

En la primera fase específica solo se implementará:

```text
LEXICAL_ONLY
LEXICAL_SYNTAX
```

---

# 11. JSON estándar de response

La respuesta debe ser uniforme para toda la API.

Formato general:

```json
{
  "requestId": "uuid",
  "dialect": "MYSQL",
  "analysisMode": "LEXICAL_SYNTAX",
  "valid": true,
  "message": "La sentencia SQL es válida a nivel léxico y sintáctico.",
  "executionStatus": "SUCCESS",
  "summary": {
    "tokenCount": 10,
    "lexicalErrorCount": 0,
    "syntaxErrorCount": 0,
    "semanticErrorCount": 0,
    "warningCount": 0,
    "analyzedAt": "2026-05-20T23:15:00"
  },
  "connectionResult": null,
  "lexicalResult": {
    "valid": true,
    "message": "Análisis léxico finalizado correctamente.",
    "tokens": [
      {
        "type": "KEYWORD",
        "lexeme": "SELECT",
        "line": 1,
        "column": 1,
        "dialect": "COMMON_SQL"
      }
    ],
    "errors": []
  },
  "syntaxResult": {
    "valid": true,
    "message": "La estructura de la sentencia SQL es correcta.",
    "statementType": "SELECT",
    "detectedClauses": [
      "SELECT",
      "FROM",
      "WHERE"
    ],
    "errors": []
  },
  "semanticResult": null,
  "errors": [],
  "console": [
    "[INFO] Iniciando análisis SQL.",
    "[INFO] Dialecto seleccionado: MYSQL.",
    "[INFO] Análisis léxico finalizado sin errores.",
    "[INFO] Análisis sintáctico finalizado sin errores.",
    "[SUCCESS] Sentencia válida."
  ]
}
```

---

# 12. Estados de ejecución

Usar valores claros para `executionStatus`:

```text
SUCCESS
LEXICAL_ERROR
SYNTAX_ERROR
SEMANTIC_ERROR
CONNECTION_ERROR
UNSUPPORTED_DIALECT
INVALID_REQUEST
INTERNAL_ERROR
```

---

# 13. Formato estándar de errores

Todo error debe tener este formato:

```json
{
  "stage": "LEXICAL",
  "code": "UNRECOGNIZED_CHARACTER",
  "message": "Carácter no reconocido '@'.",
  "line": 1,
  "column": 15,
  "lexeme": "@",
  "severity": "ERROR"
}
```

Valores válidos de `stage`:

```text
REQUEST
CONNECTION
LEXICAL
SYNTAX
SEMANTIC
INTERNAL
```

Valores válidos de `severity`:

```text
INFO
WARNING
ERROR
```

---

# 14. Reglas de seguridad

No devolver nunca el password en ninguna respuesta JSON.

No imprimir password en consola.

No imprimir password en logs.

No guardar password en variables estáticas.

No dejar credenciales quemadas en `application.properties`.

---

# 15. Manejo de conexiones dinámicas

La API debe preparar la arquitectura para conexiones dinámicas.

La conexión se construirá a partir de:

```json
{
  "host": "localhost",
  "port": 3306,
  "database": "mi_base",
  "username": "root",
  "password": "123456",
  "schema": null
}
```

No usar un `DataSource` global fijo.

La futura clase o servicio de conexión debe soportar:

```text
MYSQL      -> jdbc:mysql://host:port/database
POSTGRESQL -> jdbc:postgresql://host:port/database
SQL_SERVER -> jdbc:sqlserver://host:port;databaseName=database;encrypt=false;trustServerCertificate=true
```

La implementación detallada de conexión será parte del Markdown específico del analizador semántico.

---

# 16. CORS

Crear configuración CORS solo si es necesario para consumir la API desde frontend web.

Si se crea, hacerlo en:

```text
src/main/java/com/umg/config/WebConfig.java
```

Configuración base aceptable para desarrollo:

```java
package com.umg.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/compiler/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "OPTIONS")
                .allowedHeaders("*");
    }
}
```

No usar esta configuración abierta como recomendación final para producción.

---

# 17. Manejo global de excepciones

Crear:

```text
src/main/java/com/umg/exception/GlobalExceptionHandler.java
```

Debe manejar como mínimo:

- Errores de request inválido.
- Dialecto no soportado.
- Errores de conexión.
- Errores internos no controlados.

El response de error debe respetar el formato estándar del proyecto.

---

# 18. Separación por fases

La migración completa se dividirá en tres grandes bloques.

## 18.1 Markdown universal

Este documento define:

- Arquitectura general.
- Reglas de paquetes.
- Reglas Maven.
- Eliminación Swing.
- API base.
- JSON estándar.
- Endpoints generales.
- Reglas de seguridad.
- Reglas de pruebas.

No debe implementar todavía todos los detalles de negocio.

---

## 18.2 Fase específica: analizador léxico/sintáctico

Se hará en un segundo Markdown.

Objetivo:

```text
Migrar Lexer + Parser existentes a servicios Spring y exponerlos por REST.
```

Endpoint principal:

```http
POST /api/compiler/analyze/lexical-syntax
```

Debe reutilizar principalmente:

```text
com.umg.model.lexer.*
com.umg.model.parser.*
com.umg.model.ast.*
com.umg.model.dialect.*
com.umg.model.error.*
```

No debe requerir conexión a base de datos.

---

## 18.3 Fase específica: conexión BD + analizador semántico

Se hará en un tercer Markdown.

Objetivo:

```text
Migrar conexión dinámica JDBC y analizador semántico a servicios Spring.
```

Endpoints principales:

```http
POST /api/compiler/connection/test
POST /api/compiler/analyze/full
```

Debe reutilizar principalmente:

```text
com.umg.model.semantic.*
```

Debe soportar:

```text
MYSQL
POSTGRESQL
SQL_SERVER
```

Debe dejar margen para NoSQL futuro.

---

# 19. Reglas para el agente antes de modificar código

Antes de programar, el agente debe:

1. Leer el `pom.xml`.
2. Leer `ViewController.java`.
3. Leer `AnalizadorSql.java`.
4. Leer `ResultadoLexer.java`.
5. Leer `Lexer.java`.
6. Leer `Parser.java`.
7. Leer `SqlDialect.java`.
8. Leer clases actuales de `model/semantic`.
9. Confirmar métodos existentes y no inventar firmas.
10. Revisar tests actuales en `src/test/java/com/umg`.

El agente debe adaptar la implementación a lo existente.

---

# 20. Reglas de implementación

## 20.1 Controllers

Los controllers REST solo deben:

- Recibir request JSON.
- Validar datos básicos con Bean Validation.
- Invocar servicios.
- Retornar response JSON.

No deben contener lógica de análisis SQL.

---

## 20.2 Services

Los servicios deben:

- Coordinar el flujo.
- Invocar lexer/parser/semantic.
- Construir resultado uniforme.
- Generar mensajes de consola.
- Mapear errores.

---

## 20.3 DTOs

Los DTOs deben:

- Ser claros.
- No exponer entidades internas innecesariamente.
- No devolver password.
- Ser serializables por Jackson.
- Usar nombres entendibles para frontend.

Se pueden usar clases normales de Java 17.

No usar Lombok salvo que ya esté configurado en el proyecto.

---

# 21. Pruebas obligatorias

Antes de dar por terminado cualquier cambio, el agente debe ejecutar pruebas.

Comando obligatorio:

```bash
mvn clean test
```

Si las pruebas fallan, el desarrollo no está completo.

No responder que finalizó si:

- No compila.
- Hay tests fallando.
- Hay imports rotos.
- Hay referencias a Swing activas en la aplicación principal.
- Hay referencias a `com.dataquery`.
- La app Spring no arranca.

---

# 22. Pruebas mínimas que debe crear o conservar

Debe existir cobertura para:

## 22.1 Arranque Spring

Verificar que el contexto Spring cargue sin requerir datasource fijo.

Ejemplo esperado:

```text
CompiladoresRefactApplicationTests
```

Debe validar que Spring arranque sin configurar `spring.datasource.url`.

---

## 22.2 Health endpoint

Probar:

```http
GET /api/compiler/health
```

Debe responder `200 OK`.

---

## 22.3 Dialects endpoint

Probar:

```http
GET /api/compiler/dialects
```

Debe incluir:

```text
MYSQL
POSTGRESQL
SQL_SERVER
```

---

## 22.4 No Swing

Validar que la aplicación principal no dependa de:

```text
javax.swing
org.netbeans.lib.awtextra
com.umg.view
```

El package `com.umg.view` puede eliminarse completamente.

---

# 23. Documentación obligatoria

Crear o actualizar:

```text
docs/SPRING_MIGRATION.md
```

Debe incluir:

- Cómo arrancar el proyecto.
- Qué endpoints existen.
- Qué endpoints son de esta fase.
- Qué endpoints quedan para fases siguientes.
- Ejemplo de request.
- Ejemplo de response.
- Comando de pruebas.
- Nota de que no se usa datasource fijo.
- Nota de que Swing fue eliminado como capa de ejecución.

También crear o actualizar:

```text
docs/API_COMPILER.md
```

Debe documentar:

- `/api/compiler/health`
- `/api/compiler/dialects`
- `/api/compiler/analyze/lexical-syntax`
- `/api/compiler/connection/test`
- `/api/compiler/analyze/full`

Los endpoints no implementados aún deben marcarse como:

```text
Pendiente para siguiente fase
```

---

# 24. Criterios de aceptación del Markdown universal

El trabajo de esta etapa se considera correcto si:

1. El proyecto tiene clase principal Spring Boot en `com.umg`.
2. El proyecto compila con Java 17.
3. El proyecto usa Maven.
4. Se eliminaron dependencias activas de Swing como flujo principal.
5. Se respetó package raíz `com.umg`.
6. No existe `com.dataquery`.
7. Se definió estructura estándar Spring.
8. Se definió ruta base `/api/compiler`.
9. Se definió JSON estándar de request y response.
10. Se dejaron endpoints base claros.
11. La app Spring arranca sin datasource fijo.
12. Se ejecutó `mvn clean test`.
13. Las pruebas pasan.
14. Se documentó en `docs/SPRING_MIGRATION.md`.
15. Se documentó en `docs/API_COMPILER.md`.

---

# 25. Orden sugerido de trabajo

El agente debe trabajar en este orden:

1. Revisar estructura actual.
2. Revisar `pom.xml`.
3. Revisar `ViewController.java`.
4. Revisar lógica existente de lexer/parser/semantic.
5. Convertir proyecto a Spring Boot.
6. Crear clase principal `CompiladoresRefactApplication`.
7. Agregar dependencias Maven.
8. Eliminar Swing como flujo principal.
9. Crear estructura de paquetes Spring.
10. Crear endpoints base `health` y `dialects`.
11. Crear DTOs base de request/response.
12. Crear manejo global de errores.
13. Crear documentación.
14. Ejecutar `mvn clean test`.
15. Corregir errores.
16. Entregar resumen técnico de cambios.

---

# 26. Resumen final para el agente

Debes migrar el proyecto Java Maven JDK 17 desde Swing/MVC hacia Spring Boot REST API.

El package raíz obligatorio es:

```java
com.umg
```

La ruta base de API será:

```http
/api/compiler
```

Debes eliminar Swing como capa de ejecución, pero debes revisar y aprovechar la lógica del `ViewController`, porque ahí se concentra el flujo actual de validación y armado de resultados.

La lógica del compilador debe quedar en servicios Spring reutilizando las clases actuales de:

```text
com.umg.model.lexer
com.umg.model.parser
com.umg.model.ast
com.umg.model.dialect
com.umg.model.error
com.umg.model.semantic
```

No implementes todavía detalles específicos del segundo y tercer Markdown. Este primer paso define la base Spring y las reglas generales para que las siguientes fases trabajen sobre una arquitectura limpia.

Antes de completar, ejecutar:

```bash
mvn clean test
```

Si no compila o las pruebas fallan, no entregar como finalizado.
