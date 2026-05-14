# SQL Compiler - DataQuery Solutions S.A.

> Migración del Compilador SQL de C++ a Java

Proyecto de migración del validador de consultas SQL desde C++ a Java con interfaz gráfica Swing, eliminando dependencias JNI y aprovechando el ecosistema Java/Spring Boot de DataQuery Solutions.

---

## Requisitos Previos

| Herramienta | Versión | Verificación |
|---|---|---|
| Java JDK | 17 o superior | `java -version` |
| Maven | 3.8 o superior | `mvn -version` |
| Git | Cualquier versión | `git --version` |

### Instalación en Windows

**Opción A - Con Winget (recomendado en Windows 11):**
```powershell
winget install Microsoft.OpenJDK.17
winget install Apache.Maven
```

**Opción B - Con Chocolatey:**
```powershell
choco install openjdk17
choco install maven
```

**Opción C - Manual:**
1. Descargar JDK desde https://adoptium.net/
2. Descargar Maven desde https://maven.apache.org/download.cgi
3. Extraer y agregar `bin` al PATH del sistema

---

## Estructura del Proyecto

```
sql-compiler/
├── pom.xml                                 # Configuración Maven
│
├── src/main/java/com/dataquery/sqlcompiler/
│   ├── Main.java                           # Punto de entrada
│   │
│   ├── model/                              # Capa MODELO
│   │   ├── compiler/
│   │   │   ├── SQLCompiler.java            # Orquestador: Lexer → Parser → Semántico
│   │   │   ├── CompilationResult.java      # Resultado de compilación
│   │   │   └── CompilationSession.java     # Sesión continua
│   │   │
│   │   ├── lexer/
│   │   │   ├── Lexer.java                  # Analizador léxico
│   │   │   ├── Token.java                  # Clase Token
│   │   │   └── TokenType.java              # Tipos de token (enum)
│   │   │
│   │   ├── parser/
│   │   │   ├── Parser.java                 # Analizador sintáctico
│   │   │   └── Grammar.java                # Reglas gramaticales SQL
│   │   │
│   │   ├── ast/
│   │   │   ├── ASTNode.java                # Clase base abstracta
│   │   │   ├── SelectStatement.java        # Nodo SELECT
│   │   │   ├── InsertStatement.java        # Nodo INSERT
│   │   │   ├── UpdateStatement.java        # Nodo UPDATE
│   │   │   ├── DeleteStatement.java        # Nodo DELETE
│   │   │   ├── CreateTableStatement.java   # Nodo CREATE TABLE
│   │   │   ├── DropTableStatement.java     # Nodo DROP TABLE
│   │   │   ├── Expression.java             # Nodo de expresión
│   │   │   ├── Column.java                 # Nodo columna
│   │   │   ├── Table.java                  # Nodo tabla
│   │   │   ├── WhereClause.java            # Nodo WHERE
│   │   │   └── JoinClause.java             # Nodo JOIN
│   │   │
│   │   ├── semantic/
│   │   │   ├── SemanticAnalyzer.java       # Análisis semántico
│   │   │   ├── TypeChecker.java            # Validación de tipos
│   │   │   └── ScopeResolver.java          # Resolución de ámbitos
│   │   │
│   │   ├── symbol/
│   │   │   ├── SymbolTable.java            # Tabla de símbolos
│   │   │   ├── Symbol.java                 # Entrada de símbolo
│   │   │   └── SymbolType.java             # Tipos de símbolo (enum)
│   │   │
│   │   ├── error/
│   │   │   ├── CompilerError.java          # Modelo de error
│   │   │   ├── ErrorCollector.java         # Acumulador de errores
│   │   │   └── ErrorType.java              # Tipos de error (enum)
│   │   │
│   │   └── validation/
│   │       ├── ValidationResult.java       # Resultado de validación
│   │       └── ValidationRule.java         # Reglas de validación (enum)
│   │
│   ├── view/                               # Capa VISTA
│   │   ├── SwingApp.java                   # JFrame principal
│   │   ├── panels/
│   │   │   ├── AppMenuBar.java             # Barra de menú
│   │   │   ├── MainPanel.java              # Panel principal (input + validar)
│   │   │   ├── ResultPanel.java            # Panel de resultados
│   │   │   ├── HistoryPanel.java           # Historial de consultas
│   │   │   ├── HelpPanel.java              # Guía de uso
│   │   │   └── ExamplesPanel.java          # Ejemplos de consultas
│   │   └── components/
│   │       ├── SqlInputArea.java           # Área de texto SQL
│   │       ├── StatusBar.java              # Barra de estado
│   │       └── ErrorListPanel.java         # Lista de errores
│   │
│   ├── controller/                         # Capa CONTROLADOR
│   │   ├── SQLCompilerController.java      # Controlador principal
│   │   ├── InputHandler.java               # Manejo de entrada
│   │   └── SessionController.java          # Gestión de sesión
│   │
│   └── util/                               # Utilidades
│       ├── StringUtils.java                # Utilidades de texto
│       └── Constants.java                  # Constantes del proyecto
│
├── src/main/resources/
│   └── examples.sql                        # Ejemplos de consultas
│
└── src/test/java/com/dataquery/sqlcompiler/
    ├── lexer/
    │   └── LexerTest.java                  # Tests del analizador léxico
    ├── parser/
    │   └── ParserTest.java                 # Tests del analizador sintáctico
    ├── semantic/
    │   └── SemanticAnalyzerTest.java       # Tests del análisis semántico
    └── integration/
        └── IntegrationTest.java            # Tests de integración
```

---

## Compilar el Proyecto

```powershell
cd sql-compiler
mvn clean compile
```

**Salida esperada:**
```
[INFO] BUILD SUCCESS
[INFO] Total time:  X.XXX s
```

---

## Ejecutar la Aplicación

```powershell
cd sql-compiler
mvn exec:java
```

Se abrirá una ventana Swing con:
- **Menú superior:** File, Edit, View, Help
- **Área de texto:** Para escribir consultas SQL
- **Botones:** Validate Query y Clear
- **Área de resultados:** Muestra si la consulta es válida o inválida
- **Barra de estado:** Indicador en la parte inferior

### Ejecutar como JAR

```powershell
# Generar el JAR
mvn package

# Ejecutar
java -jar target/sql-compiler-1.0.0.jar
```

---

## Ejecutar los Tests

```powershell
cd sql-compiler
mvn test
```

**Salida esperada:**
```
[INFO] Tests run: X, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

---

## Probar la Aplicación

### Consulta válida
Escribe en el área de texto:
```sql
SELECT * FROM users WHERE age > 18;
```
Resultado esperado: **CONSULTA VALIDA** (texto verde)

### Consulta inválida
Escribe:
```sql
SELCT * FROM users;
```
Resultado esperado: **CONSULTA INVALIDA** con lista de errores (texto rojo)

### Consultas soportadas
```sql
SELECT * FROM users;
INSERT INTO users (name, age) VALUES ('John', 25);
UPDATE users SET age = 26 WHERE name = 'John';
DELETE FROM users WHERE age < 18;
CREATE TABLE users (id INT, name VARCHAR);
DROP TABLE users;
```

---

## Mapeo de Migración C++ → Java

| C++ (Original) | Java (Migración) |
|---|---|
| `include/Token.h` | `model/lexer/Token.java` |
| `include/Lexer.h` + `src/Lexer.cpp` | `model/lexer/Lexer.java` |
| `include/Parser.h` + `src/Parser.cpp` | `model/parser/Parser.java` |
| `include/AST.h` | `model/ast/*` |
| `include/SemanticAnalyzer.h` + `src/SemanticAnalyzer.cpp` | `model/semantic/SemanticAnalyzer.java` |
| `include/SymbolTable.h` + `src/SymbolTable.cpp` | `model/symbol/SymbolTable.java` |
| `src/main.cpp` | `Main.java` + `view/` + `controller/` |
| `tests/lexer_test.cpp` | `test/.../LexerTest.java` |
| Google Test | JUnit 5 |

---

## Requerimientos Funcionales

| RF | Descripción | Clase responsable |
|---|---|---|
| RF1 | Recepción de consultas SQL | `SqlInputArea.java` |
| RF2 | Validación de consultas SQL | `SQLCompiler.java` |
| RF3 | Análisis léxico | `Lexer.java`, `Token.java`, `TokenType.java` |
| RF4 | Análisis sintáctico | `Parser.java`, `Grammar.java` |
| RF5 | Detección y notificación de errores | `CompilerError.java`, `ErrorCollector.java`, `ErrorListPanel.java` |
| RF6 | Interfaz interactiva en Java | `SwingApp.java`, `AppMenuBar.java` |
| RF7 | Visualización de resultados | `MainPanel.java`, `ResultPanel.java` |
| RF8 | Ejecución continua | `CompilationSession.java` |
| RF9 | Guía de uso integrada | `HelpPanel.java` |
| RF10 | Ejemplos de consultas | `ExamplesPanel.java` |
| RF11 | Implementación en Java puro | Todo el proyecto (sin JNI ni C++) |
| RF12 | Consistencia funcional | Tests de migración JUnit 5 |

---

## Flujo de Trabajo para el Equipo

| Área | Paquete | Responsabilidad |
|---|---|---|
| Léxico | `model/lexer/` | Tokenización de SQL |
| Sintáctico | `model/parser/` | Gramática y parsing |
| AST | `model/ast/` | Nodos del árbol |
| Semántico | `model/semantic/` | Validación de tipos y ámbitos |
| UI Swing | `view/` | Paneles y componentes visuales |
| Controlador | `controller/` | Coordinación MVC |
| Tests | `src/test/java/` | Pruebas unitarias |

Cada miembro puede trabajar en su módulo de forma independiente.

---

## Resolución de Problemas

### Error: `mvn: command not found`
Maven no está en el PATH. Instálalo con Winget o Chocolatey (ver requisitos).

### Error: `reference to MenuBar is ambiguous`
Ya corregido. La clase se renombró a `AppMenuBar` para evitar conflicto con `java.awt.MenuBar`.

### Error: `variable might not have been initialized`
Ya corregido. Revisa el orden de inicialización de campos en el constructor.

### Error: `Unknown lifecycle phase .mainClass=...`
PowerShell interpreta `-D` como parámetro propio. Usa:
```powershell
mvn exec:java
```

---

**DataQuery Solutions S.A.** - Marzo 2026
