# MongoDB y Cassandra CQL - Analisis Lexico y Sintactico

## Introduccion

Este documento describe la implementacion del analisis lexico y sintactico para dialectos NoSQL (MongoDB y Cassandra CQL) en el Compilador SQL/NoSQL de UMG.

## Arquitectura

### Paquete `com.umg.model.nosql`

```
com.umg.model.nosql
├── common/
│   ├── NoSqlTokenType.java      Enum con tipos de tokens NoSQL
│   ├── NoSqlToken.java          Clase token NoSQL (type, lexeme, line, column)
│   ├── NoSqlSyntaxError.java    Error de sintaxis NoSQL
│   └── NoSqlAnalysisResult.java Resultado del analisis (syntaxValid, statementType, etc.)
├── mongodb/
│   ├── MongoLexer.java                Tokenizador MongoDB
│   ├── MongoParser.java               Validador sintactico MongoDB
│   └── MongoLexicalSyntaxAnalyzer.java Analizador completo MongoDB
└── cassandra/
    ├── CqlLexer.java                   Tokenizador Cassandra CQL
    ├── CqlParser.java                  Validador sintactico Cassandra CQL
    └── CassandraCqlLexicalSyntaxAnalyzer.java Analizador completo Cassandra CQL
```

### Enrutamiento por dialecto

`DialectAnalysisRouter` utiliza `CompilerDialect` para rutear al servicio adecuado:

| Dialecto | Servicio |
|----------|----------|
| MYSQL, POSTGRESQL, SQL_SERVER | `LexicalSyntaxAnalysisService` |
| MONGODB, CASSANDRA_CQL | `NoSqlLexicalSyntaxAnalysisService` |

## MongoDB

### Tokenizador (`MongoLexer`)

Tokeniza expresiones MongoDB como `db.coleccion.metodo({...})`.

**Tipos de tokens soportados:**
- `KEYWORD`: `db`, `find`, `findOne`, `insertOne`, `insertMany`, `updateOne`, `updateMany`, `deleteOne`, `deleteMany`, `aggregate`
- `MONGO_OPERATOR`: `$eq`, `$ne`, `$gt`, `$gte`, `$lt`, `$lte`, `$in`, `$nin`, `$and`, `$or`, `$not`, `$nor`, `$exists`, `$regex`, `$set`, `$unset`, `$inc`, `$push`, `$pull`, `$match`, `$group`, `$project`, `$sort`, `$lookup`, `$limit`, `$skip`, `$count`, `$sum`, `$avg`, `$min`, `$max`, `$first`, `$last`
- `STRING`, `NUMBER`, `BOOLEAN`, `NULL`
- Delimitadores: `{}`, `[]`, `()`, `:`, `,`, `.`, `;`
- `COMMENT`: lineas (`//`) y bloques (`/* */`)

### Validador sintactico (`MongoParser`)

Valida la estructura de expresiones MongoDB.

**Reglas de validacion:**
1. La expresion debe iniciar con `db`
2. Sigue un `.` y el nombre de la coleccion
3. Sigue un `.` y un metodo soportado
4. Sigue `(` y al menos un argumento (objeto o arreglo)
5. Los objetos deben tener pares clave:valor con `,` entre pares
6. Los arreglos deben tener elementos separados por `,`
7. Llaves/corchetes/parentesis deben estar balanceados
8. `;` opcional al final

**Metodos soportados y argumentos minimos:**
| Metodo | Args minimos |
|--------|-------------|
| find | 1 |
| findOne | 1 |
| insertOne | 1 |
| insertMany | 1 |
| updateOne | 2 |
| updateMany | 2 |
| deleteOne | 1 |
| deleteMany | 1 |
| aggregate | 1 |

### Ejemplos validos

```javascript
db.users.find({})
db.users.findOne({ status: "active" })
db.users.insertOne({ name: "Kevin" })
db.users.insertMany([{ name: "Kevin" }, { name: "Ana" }])
db.users.updateOne({ _id: 1 }, { $set: { name: "Kevin" } })
db.users.deleteOne({ _id: 1 })
db.orders.aggregate([{ $match: { status: "completed" } }])
```

### Ejemplos invalidos

```javascript
users.find({})                // Error: falta 'db'
db.users.watch({})            // Error: metodo no soportado
db.users.find()               // Error: falta argumento
db.users.find({)              // Error: llaves desbalanceadas
db.find({})                   // Error: falta nombre de coleccion
```

## Cassandra CQL

### Tokenizador (`CqlLexer`)

Tokeniza sentencias Cassandra CQL.

**Palabras clave soportadas:**
SELECT, FROM, WHERE, INSERT, INTO, VALUES, UPDATE, SET, DELETE, CREATE, KEYSPACE, TABLE, ALTER, DROP, TRUNCATE, PRIMARY, KEY, WITH, AND, OR, IF, EXISTS, NOT, NULL, USE, USING, TTL, TIMESTAMP, ORDER, BY, ALLOW, FILTERING, LIMIT, ASC, DESC, BEGIN, BATCH, APPLY, ADD, RENAME, COMPACT, STORAGE

**Tipos de datos soportados:**
TEXT, VARCHAR, ASCII, INT, BIGINT, SMALLINT, TINYINT, VARINT, BOOLEAN, UUID, TIMEUUID, TIMESTAMP, DATE, TIME, FLOAT, DOUBLE, DECIMAL, BLOB, LIST, SET, MAP, COUNTER

**Tipos de tokens:**
- `KEYWORD`: palabras clave y tipos de datos
- `IDENTIFIER`: nombres de tablas, columnas, keyspaces
- `STRING`, `NUMBER`, `BOOLEAN`, `NULL`
- `OPERATOR`: `=`, `<`, `>`, `<=`, `>=`, `*`
- Delimitadores: `()`, `{}`, `[]`, `,`, `.`, `;`, `:`
- `COMMENT`: lineas (`--`, `//`) y bloques (`/* */`)

### Validador sintactico (`CqlParser`)

**Sentencias soportadas:**

| Sentencia | Tipo | Clausulas |
|-----------|------|-----------|
| SELECT | CASSANDRA_SELECT | SELECT, FROM, WHERE, ALLOW_FILTERING, ORDER_BY, LIMIT |
| INSERT | CASSANDRA_INSERT | INSERT, INTO, VALUES |
| UPDATE | CASSANDRA_UPDATE | UPDATE, USING, SET, WHERE, IF |
| DELETE | CASSANDRA_DELETE | DELETE, FROM, WHERE, IF |
| CREATE KEYSPACE | CASSANDRA_CREATE_KEYSPACE | CREATE_KEYSPACE, IF_NOT_EXISTS, WITH |
| CREATE TABLE | CASSANDRA_CREATE_TABLE | CREATE_TABLE, IF_NOT_EXISTS, PRIMARY_KEY, WITH |
| ALTER TABLE | CASSANDRA_ALTER_TABLE | ALTER_TABLE, ADD/DROP/RENAME |
| DROP TABLE | CASSANDRA_DROP_TABLE | DROP_TABLE |
| DROP KEYSPACE | CASSANDRA_DROP_KEYSPACE | DROP_KEYSPACE |
| TRUNCATE | CASSANDRA_TRUNCATE | TRUNCATE |
| USE | CASSANDRA_USE | USE |

### Ejemplos validos

```sql
SELECT * FROM users;
SELECT name, age FROM users WHERE age > 18 ALLOW FILTERING;
SELECT * FROM users ORDER BY name ASC;
SELECT * FROM users LIMIT 10;
INSERT INTO users (id, name) VALUES (1, 'Kevin');
UPDATE users SET name = 'Kevin' WHERE id = 1;
DELETE FROM users WHERE id = 1;
CREATE KEYSPACE mykeyspace WITH replication = {'class': 'SimpleStrategy'};
CREATE TABLE users (id INT PRIMARY KEY, name TEXT);
ALTER TABLE users ADD email TEXT;
DROP TABLE users;
TRUNCATE users;
USE mykeyspace;
```

## API REST

### Dialectos disponibles

`GET /api/compiler/dialects`

```json
{
  "supportedDialects": ["MYSQL", "POSTGRESQL", "SQL_SERVER", "MONGODB", "CASSANDRA_CQL"],
  "sqlDialects": ["MYSQL", "POSTGRESQL", "SQL_SERVER"],
  "noSqlDialects": ["MONGODB", "CASSANDRA_CQL"],
  "futureDialects": []
}
```

### Modo LEXICAL_SYNTAX

Para dialectos NoSQL, funciona igual que para SQL: ejecuta analisis lexico y sintactico.

### Modos FULL y SEMANTIC_ONLY

Para dialectos NoSQL, los modos FULL y SEMANTIC_ONLY retornan:
```json
{
  "valid": false,
  "executionStatus": "INVALID_REQUEST",
  "message": "El analisis FULL/SEMANTIC_ONLY para dialectos NoSQL queda pendiente para la fase semantica."
}
```

## Token DTO mapping

`NoSqlLexicalSyntaxAnalysisService` mapea `NoSqlTokenType` a strings para `TokenDto.type`:

| NoSqlTokenType | TokenDto.type (toString) |
|----------------|--------------------------|
| KEYWORD | "KEYWORD" |
| IDENTIFIER | "IDENTIFIER" |
| STRING | "STRING" |
| NUMBER | "NUMBER" |
| BOOLEAN | "BOOLEAN" |
| NULL | "NULL" |
| MONGO_OPERATOR | "MONGO_OPERATOR" |
| OPERATOR | "OPERATOR" |
| LEFT_BRACE | "LEFT_BRACE" |
| RIGHT_BRACE | "RIGHT_BRACE" |
| etc. | (nombre del enum) |

## Pruebas

- `com.umg.nosql.mongodb.MongoLexerTest` (17 tests)
- `com.umg.nosql.mongodb.MongoParserTest` (14 tests)
- `com.umg.nosql.cassandra.CqlLexerTest` (18 tests)
- `com.umg.nosql.cassandra.CqlParserTest` (18 tests)
- `com.umg.api.compiler.CompilerControllerTest` - incluye 6 tests NoSQL
- `com.umg.api.compiler.OpenApiDocumentationTest` - incluye verificacion de dialectos NoSQL en schema
