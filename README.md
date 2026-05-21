# SQL Compiler - UMG

API REST para análisis léxico, sintáctico y semántico de consultas SQL. Construida con Spring Boot 3.2.5 y Java 17.

> Migración del validador SQL de C++ a Java/Spring Boot, eliminando dependencias JNI y la interfaz Swing original.

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
[INFO] Tests run: 124, Failures: 0, Errors: 0, Skipped: 0
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
| GET | `/api/compiler/dialects` | Lista de dialectos SQL soportados |
| POST | `/api/compiler/analyze/lexical-syntax` | Análisis léxico + sintáctico (sin BD) |
| POST | `/api/compiler/analyze/full` | Análisis completo: léxico + sintáctico + semántico (requiere BD) |
| POST | `/api/compiler/connection/test` | Probar conexión JDBC a una BD |

### Modos de análisis (`AnalysisMode`)
- `LEXICAL_ONLY` — solo tokenización
- `LEXICAL_SYNTAX` — tokenización + gramática
- `SEMANTIC_ONLY` — solo validación semántica (requiere BD)
- `FULL` — análisis completo

---

## Dialectos SQL Soportados

| Dialecto | Identificadores | Especificidad |
|---|---|---|
| `MYSQL` | backticks `` ` `` | Funciones MySQL |
| `POSTGRESQL` | comillas dobles `"` | Funciones PostgreSQL |
| `SQL_SERVER` | corchetes `[ ]` | Funciones SQL Server |
| `COMMON` | estándar ANSI | Sintaxis SQL genérica |

---

## Análisis Semántico (con Base de Datos)

El análisis semántico valúa tablas, columnas y tipos contra una BD real.  
La conexión se configura por request — **no requiere configuración global de BD**.

Ejemplo de body para análisis completo:
```json
{
  "sql": "SELECT id, name FROM users WHERE age > 18",
  "dialect": "MYSQL",
  "mode": "FULL",
  "connectionConfig": {
    "url": "jdbc:mysql://localhost:3306/mi_bd",
    "username": "root",
    "password": "mi_password"
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
│       ├── dialect/                          # Dialectos SQL
│       ├── error/                            # Modelo de error
│       ├── lexer/                            # Analizador léxico
│       ├── parser/                           # Analizador sintáctico
│       └── semantic/                         # Analizador semántico
│
├── main/resources/
│   └── application.properties                # Configuración de la app
│
└── test/java/com/umg/
    ├── api/compiler/                         # Tests del controlador
    ├── integration/                          # Tests de integración
    ├── lexer/                                # Tests del lexer
    └── parser/                               # Tests del parser
```

---

**UMG - Universidad Mariano Gálvez** · Mayo 2026
