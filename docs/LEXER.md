# Modulo de Analisis Lexico y Sintactico Basico

## 1. Objetivo del modulo

Implementar un modulo completo de analisis lexico y validacion sintactica basica para
sentencias SQL, soportando los dialectos MySQL, PostgreSQL y SQL Server.

El modulo recibe una cadena SQL ingresada desde la interfaz y devuelve un objeto de
resultado con informacion sobre validez, tokens encontrados, errores lexicos/sintacticos
y dialecto detectado.

## 2. Como usar AnalizadorSql

La clase principal es `AnalizadorSql`, ubicada en `com.umg.model.lexer`:

```java
AnalizadorSql analizador = new AnalizadorSql();
ResultadoLexer resultado = analizador.analizar("SELECT * FROM clientes;");
```

Con dialecto preferido:

```java
ResultadoLexer resultado = analizador.analizar(
    "SELECT DATE_FORMAT(fecha, '%Y-%m-%d') FROM ventas;",
    SqlDialect.MYSQL
);
```

## 3. Ejemplo de entrada

```sql
SELECT id, nombre FROM clientes WHERE estado = 1;
```

## 4. Ejemplo de salida

```
ResultadoLexer {
  valido: true,
  mensaje: "Sentencia SQL valida (lexica y sintacticamente)",
  tokens: [
    PALABRA_RESERVADA      | SELECT           | línea 1 | columna 1  | COMMON
    IDENTIFICADOR          | id               | línea 1 | columna 8  | COMMON
    COMA                   | ,                | línea 1 | columna 10 | COMMON
    IDENTIFICADOR          | nombre           | línea 1 | columna 12 | COMMON
    PALABRA_RESERVADA      | FROM             | línea 1 | columna 19 | COMMON
    IDENTIFICADOR          | clientes         | línea 1 | columna 24 | COMMON
    PALABRA_RESERVADA      | WHERE            | línea 1 | columna 34 | COMMON
    IDENTIFICADOR          | estado           | línea 1 | columna 40 | COMMON
    OPERADOR_COMPARACION   | =                | línea 1 | columna 47 | COMMON
    NUMERO_ENTERO          | 1                | línea 1 | columna 49 | COMMON
    PUNTO_Y_COMA           | ;                | línea 1 | columna 50 | COMMON
    EOF                    |                  | línea 1 | columna 51 | COMMON
  ],
  errores: []
}
```

## 5. Estructura de ResultadoLexer

| Campo                  | Tipo                  | Descripcion                                   |
|------------------------|-----------------------|-----------------------------------------------|
| valido                 | boolean               | Indica si la sentencia es lexica y sintacticamente valida |
| mensaje                | String                | Mensaje descriptivo del resultado             |
| tokens                 | List\<Token\>          | Lista de tokens generados por el lexer        |
| errores                | List\<ErrorLexico\>    | Lista de errores encontrados                  |
| dialectoDetectado      | SqlDialect            | Dialecto detectado automaticamente            |
| dialectosCompatibles   | List\<SqlDialect\>     | Lista de dialectos compatibles con la consulta |
| sintaxisBasicaValida   | boolean               | Indica si la sintaxis basica es valida        |

## 6. Estructura de Token

| Campo    | Tipo       | Descripcion                               |
|----------|------------|-------------------------------------------|
| tipo     | TokenType  | Tipo del token (ver seccion 6.1)          |
| lexema   | String     | Valor textual del token                   |
| linea    | int        | Linea donde aparece el token              |
| columna  | int        | Columna donde aparece el token            |
| dialecto | SqlDialect | Dialecto al que pertenece (o COMMON)      |

### 6.1 Tipos de Token (TokenType)

- PALABRA_RESERVADA
- IDENTIFICADOR
- IDENTIFICADOR_DELIMITADO
- FUNCION
- TIPO_DATO
- OPERADOR
- OPERADOR_COMPARACION
- OPERADOR_LOGICO
- NUMERO_ENTERO
- NUMERO_DECIMAL
- CADENA
- COMENTARIO_LINEA
- COMENTARIO_BLOQUE
- PARENTESIS_IZQUIERDO
- PARENTESIS_DERECHO
- COMA
- PUNTO
- PUNTO_Y_COMA
- ASTERISCO
- PARAMETRO
- PLACEHOLDER
- EOF
- DESCONOCIDO

## 7. Estructura de errores

### ErrorLexico

| Campo   | Tipo   | Descripcion                     |
|---------|--------|---------------------------------|
| codigo  | String | Codigo del error (E001-E011)    |
| mensaje | String | Descripcion del error           |
| linea   | int    | Linea donde ocurrio el error    |
| columna | int    | Columna donde ocurrio el error  |
| lexema  | String | Lexema que causo el error       |

### Codigos de error

| Codigo | Descripcion                                             |
|--------|---------------------------------------------------------|
| E001   | Caracter no reconocido                                  |
| E002   | Cadena sin cerrar                                       |
| E003   | Comentario multilinea sin cerrar                        |
| E004   | Identificador invalido                                  |
| E005   | Numero mal formado                                      |
| E006   | Simbolo no soportado                                    |
| E007   | Error sintactico basico                                 |
| E008   | Parentesis sin cerrar                                   |
| E009   | Comillas o delimitadores propios del dialecto sin cerrar |
| E010   | Funcion no soportada para el dialecto validado           |
| E011   | Palabra reservada usada incorrectamente como identificador |

## 8. Dialectos soportados

- **COMMON**: SQL estandar ANSI
- **MYSQL**: Soporte para backticks `` ` ``, funciones especificas (DATE_FORMAT, IFNULL, UUID, etc.)
- **POSTGRESQL**: Soporte para comillas dobles `"`, funciones especificas (TO_CHAR, DATE_TRUNC, STRING_AGG, etc.)
- **SQL_SERVER**: Soporte para corchetes `[]`, funciones especificas (GETDATE, ISNULL, DATEADD, etc.)

## 9. Funciones soportadas por motor

### Comunes (todos los dialectos)
COUNT, SUM, AVG, MIN, MAX, CONCAT, COALESCE, NULLIF, UPPER, LOWER,
TRIM, LTRIM, RTRIM, SUBSTRING, LENGTH, ROUND, ABS, NOW, CURRENT_DATE,
CURRENT_TIME, CURRENT_TIMESTAMP, CAST, CONVERT, ISNULL, LEN

### MySQL
DATE_FORMAT, IFNULL, IF, DATABASE, UUID, STR_TO_DATE, CHAR_LENGTH,
GROUP_CONCAT, UNIX_TIMESTAMP, DATE_ADD, DATE_SUB, MD5, SHA1, SHA2,
JSON_EXTRACT, JSON_UNQUOTE, AES_ENCRYPT, AES_DECRYPT, RAND, POW, etc.

### PostgreSQL
TO_CHAR, TO_DATE, DATE_TRUNC, STRING_AGG, ARRAY_AGG, GEN_RANDOM_UUID,
TO_TIMESTAMP, SPLIT_PART, REGEXP_REPLACE, UNNEST, GENERATE_SERIES,
JSON_BUILD_OBJECT, JSON_AGG, PG_SLEEP, etc.

### SQL Server
GETDATE, ISNULL, LEN, DATEADD, DATEDIFF, FORMAT, NEWID, SYSDATETIME,
CHARINDEX, PATINDEX, SOUNDEX, IIF, CHOOSE, HASHBYTES, JSON_VALUE,
JSON_QUERY, RANK, DENSE_RANK, ROW_NUMBER, NTILE, LEAD, LAG, etc.

## 10. Sentencias soportadas

- SELECT (con WHERE, JOIN, GROUP BY, HAVING, ORDER BY, LIMIT, OFFSET, UNION)
- INSERT INTO ... VALUES
- UPDATE ... SET ... WHERE
- DELETE FROM ... WHERE
- CREATE TABLE (con columnas, tipos, constraints, PRIMARY KEY, FOREIGN KEY, etc.)
- ALTER TABLE (ADD/DROP/ALTER COLUMN, MODIFY, RENAME)
- DROP TABLE, DROP DATABASE, DROP INDEX, DROP VIEW
- TRUNCATE TABLE
- WITH (CTE - Common Table Expressions)

## 11. Limitaciones actuales

- No se soportan todos los tipos de JOIN exoticos (NATURAL JOIN, LATERAL, etc.)
- No se valida coincidencia de tipos en expresiones
- No se soportan funciones ventana complejas (OVER con particiones avanzadas)
- No se soportan procedimientos almacenados completos
- MongoDB y otros motores NoSQL no estan implementados

## 12. Analisis semantico (implementado)

La validacion semantica contra base de datos real esta disponible via:

- **POST /api/compiler/analyze/full** — analisis lexico + sintactico + semantico
- **POST /api/compiler/connection/test** — prueba de conexion JDBC

Requiere enviar `connectionConfig` con credenciales de base de datos.
Ver `API_COMPILER.md` para documentacion detallada de los endpoints.

## 13. Como ejecutar pruebas

```bash
# Ejecutar todas las pruebas
mvn clean test

# Ejecutar solo pruebas del lexer
mvn test -Dtest="com.umg.lexer.LexerTest"

# Ejecutar solo pruebas del parser
mvn test -Dtest="com.umg.parser.ParserTest"

# Ejecutar solo pruebas de integracion
mvn test -Dtest="com.umg.integration.IntegrationTest"
```
