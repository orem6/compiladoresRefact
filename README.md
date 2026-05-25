# Multi-DB Compiler - UMG

API REST para análisis léxico, sintáctico y semántico de consultas **SQL, CQL (Cassandra) y MongoDB (agregación)**. Construida con Spring Boot 3.2.5 y Java 17.

> Migración del validador SQL de C++ a Java/Spring Boot, eliminando dependencias JNI y la interfaz Swing original.
> Extension a múltiples motores de base de datos (SQL, Cassandra CQL, MongoDB aggregation pipeline).

---

## Requisitos Previos

| Herramienta | Versión | Verificación |
|---|---|---|
| Java JDK | 17 o superior | `java -version` |
| Maven | 3.8 o superior | `mvn -version` |
| Git | Cualquiera | `git --version` |

### Instalación en Windows

**Opción A — Winget (Windows 11):**
```powershell
winget install Microsoft.OpenJDK.17
winget install Apache.Maven
```

**Opción B — Chocolatey:**
```powershell
choco install openjdk17
choco install maven
```

**Opción C — Manual:**
1. Descargar JDK 17+ desde https://adoptium.net/
2. Descargar Maven desde https://maven.apache.org/download.cgi
3. Extraer y agregar `bin/` al PATH del sistema

### Instalación en macOS

```bash
# Usando Homebrew (recomendado)
brew install openjdk@17 maven
```

### Instalación en Linux (Ubuntu/Debian)

```bash
sudo apt update
sudo apt install openjdk-17-jdk maven
```

---

## Compilar

```powershell
mvn clean package
```

Genera el JAR en `target/sql-compiler-1.0.0.jar`.

---

## Ejecutar

### Con Maven (desarrollo):
```powershell
mvn spring-boot:run
```

### Como JAR:
```powershell
java -jar target/sql-compiler-1.0.0.jar
```

El servidor inicia en `http://localhost:8080`.

---

## Ejecutar Tests

```powershell
mvn test
```

Salida esperada:
```
[INFO] Tests run: 343, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

---

## Acceso

| Recurso | URL |
|---|---|
| API base | `http://localhost:8080` |
| Swagger UI | `http://localhost:8080/api/compiler/docs` |
| OpenAPI JSON | `http://localhost:8080/api/compiler/openapi` |
| Health check | `http://localhost:8080/api/compiler/health` |

---

## Endpoints

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/compiler/health` | Estado del servidor |
| GET | `/api/compiler/dialects` | Lista de motores soportados |
| POST | `/api/compiler/analyze/lexical-syntax` | Análisis léxico + sintáctico (sin BD) |
| POST | `/api/compiler/analyze/full` | Análisis completo: léxico + sintáctico + semántico (requiere BD) |
| POST | `/api/compiler/connection/test` | Probar conexión a BD (JDBC, CQL o MongoDB) |

### Modos de análisis (`AnalysisMode`)
- `LEXICAL_ONLY` — solo tokenización
- `LEXICAL_SYNTAX` — tokenización + gramática
- `SEMANTIC_ONLY` — solo validación semántica (requiere BD)
- `FULL` — análisis completo

---

## Motores Soportados

| Motor | Dialecto | Pipeline | Análisis Semántico |
|---|---|---|---|
| MySQL | `MYSQL` | Lexer SQL + Parser SQL | JdbcDatabaseMetadataService |
| PostgreSQL | `POSTGRESQL` | Lexer SQL + Parser SQL | JdbcDatabaseMetadataService |
| SQL Server | `SQL_SERVER` | Lexer SQL + Parser SQL | JdbcDatabaseMetadataService |
| Cassandra | `CASSANDRA` | Lexer SQL + CqlParser | CqlDatabaseMetadataService |
| MongoDB | `MONGODB` | MongoLexer JSON + MongoParser | MongoDatabaseMetadataService |

---

## Análisis Semántico (con Base de Datos)

El análisis semántico valúa tablas, columnas y tipos contra una BD real.  
La conexión se configura por request — **no requiere configuración global de BD**.

### Ejemplo SQL (MySQL)
```json
{
  "dialect": "MYSQL",
  "sql": "SELECT id, name FROM users WHERE age > 18",
  "analysisMode": "FULL",
  "connectionConfig": {
    "host": "localhost",
    "port": 3306,
    "database": "mi_bd",
    "username": "root",
    "password": "********"
  }
}
```

### Ejemplo CQL (Cassandra)
```json
{
  "dialect": "CASSANDRA",
  "sql": "SELECT id, nombre FROM usuarios WHERE edad > 18 ALLOW FILTERING;",
  "analysisMode": "FULL",
  "connectionConfig": {
    "host": "localhost",
    "port": 9042,
    "database": "mi_keyspace"
  }
}
```

### Ejemplo MongoDB (Aggregation Pipeline)
```json
{
  "dialect": "MONGODB",
  "sql": "[{\"$match\": {\"status\": \"active\"}}, {\"$group\": {\"_id\": \"$category\", \"total\": {\"$sum\": 1}}}]",
  "analysisMode": "FULL",
  "connectionConfig": {
    "host": "localhost",
    "port": 27017,
    "database": "mi_db"
  }
}
```

---

## Documentación Adicional

La carpeta `docs/` contiene guías detalladas:

| Archivo | Contenido |
|---|---|
| `API_COMPILER.md` | Documentación completa de la API REST |
| `LEXER.md` | Módulo del analizador léxico |
| `LEXICAL_SYNTAX_API.md` | Escenarios curl para endpoints |
| `SWAGGER_OPENAPI.md` | Uso de Swagger / OpenAPI |
| `SPRING_MIGRATION.md` | Guía de migración a Spring Boot |

---

## Solución de Problemas

### Error: `mvn: command not found`
Maven no está instalado o no está en el PATH.  
Ver sección **Instalación en Windows**.

### Error: `Input length = 1` o problemas con `target/`
Limpiar el directorio de compilación:
```powershell
mvn clean compile
```

### Error al conectar a BD
Verificar que la URL JDBC, usuario y contraseña sean correctos.  
Usar el endpoint `POST /api/compiler/connection/test` para diagnosticar.

### Puerto 8080 en uso
Cambiar el puerto en `src/main/resources/application.properties`:
```properties
server.port=9090
```

---

## Estructura del Proyecto

```
src/
├── main/java/com/umg/
│   ├── CompiladoresRefactApplication.java   # Punto de entrada Spring Boot
│   ├── api/compiler/                         # Controlador REST + DTOs
│   ├── application/compiler/                 # Servicios de aplicación
│   ├── config/                               # Configuración (Swagger, CORS)
│   ├── exception/                            # Manejo global de errores
│   └── model/                                # Modelos de dominio
│       ├── ast/                              # Nodos del AST
│       ├── dialect/                          # Dialectos SQL/CQL
│       ├── error/                            # Modelo de error
│       ├── lexer/                            # Analizador léxico SQL
│       ├── mongo/                            # Analizador MongoDB (lexer + parser)
│       ├── parser/                           # Analizador sintáctico (SQL + CQL)
│       └── semantic/                         # Analizador semántico
│           ├── extractor/                    # Extractor de referencias
│           ├── metadata/                     # Servicios de metadatos (JDBC, CQL, MongoDB)
│           ├── result/                       # Modelos de resultado semántico
│           └── validator/                    # Validadores (SQL, CQL, MongoDB)
│
├── main/resources/
│   └── application.properties                # Configuración de la app
│
├── sql-scripts/
│   ├── 01-mysql.sql                          # Script de prueba MySQL
│   ├── 02-postgresql.sql                     # Script de prueba PostgreSQL
│   ├── 03-sqlserver.sql                      # Script de prueba SQL Server
│   ├── 04-cassandra.cql                      # Script de prueba Cassandra CQL
│   └── README.md                             # Documentación de scripts
│
└── test/java/com/umg/
    ├── api/compiler/                         # Tests del controlador (15 tests)
    ├── extractor/                            # Tests del extractor de referencias (34 tests)
    ├── integration/                          # Tests de integración (62 tests)
    ├── lexer/                                # Tests del lexer (57 tests)
    ├── parser/                               # Tests del parser (77 tests)
    └── stress/                               # Tests de fuzzing (87 tests)
```

---

**UMG - Universidad Mariano Gálvez** · Mayo 2026
