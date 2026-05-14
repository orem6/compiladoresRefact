# Descripción General de mi Parte en el Proyecto

## Proyecto: Migración de Compilador SQL de C a Java

El proyecto consiste en refactorizar y migrar un compilador base desarrollado en lenguaje C/C++ hacia una solución en Java, utilizando una arquitectura MVC y una interfaz gráfica construida con Swing. El compilador tiene como objetivo analizar sentencias SQL y validar su estructura, tomando como base principal el comportamiento de MySQL, pero dejando abierta la posibilidad de adaptarse a otros motores como PostgreSQL y SQL Server.

Dentro del equipo de backend, el trabajo se divide en responsabilidades específicas. Gerson se encargará principalmente de la conexión con la base de datos y de las validaciones semánticas relacionadas con la existencia de objetos, mientras que mi responsabilidad estará enfocada en el desarrollo y refactorización del analizador léxico.

---

## Mi Responsabilidad Principal

Mi parte del proyecto consiste en trabajar el **analizador léxico** del compilador SQL.

El analizador léxico es la primera etapa del proceso de compilación. Su función principal es recibir una sentencia SQL escrita por el usuario y dividirla en unidades mínimas reconocibles llamadas **tokens**. Estos tokens representan palabras reservadas, identificadores, operadores, números, cadenas de texto, signos de puntuación y otros elementos que forman parte del lenguaje SQL.

En términos simples, mi trabajo será convertir una consulta SQL como:

```sql
SELECT nombre, edad FROM usuarios WHERE id = 10;
```

En una secuencia de tokens como:

```text
SELECT      -> Palabra reservada
nombre      -> Identificador
,           -> Coma
edad        -> Identificador
FROM        -> Palabra reservada
usuarios    -> Identificador
WHERE       -> Palabra reservada
id          -> Identificador
=           -> Operador
10          -> Número
;           -> Fin de sentencia
```

Esta salida será utilizada posteriormente por otros módulos del compilador, como el analizador sintáctico y el analizador semántico.

---

## Alcance de mi Trabajo

Mi trabajo estará limitado exclusivamente al análisis léxico. Esto significa que debo concentrarme en reconocer correctamente los componentes individuales de una sentencia SQL, sin validar todavía si la sentencia está bien estructurada a nivel gramatical o si los objetos existen dentro de una base de datos real.

El alcance incluye:

- Leer una sentencia SQL ingresada por el usuario.
- Recorrer la sentencia carácter por carácter.
- Identificar palabras reservadas del lenguaje SQL.
- Reconocer identificadores como nombres de tablas, columnas, alias o esquemas.
- Detectar números enteros y decimales.
- Reconocer cadenas de texto encerradas entre comillas.
- Identificar operadores aritméticos, lógicos y relacionales.
- Reconocer símbolos como comas, paréntesis, punto, punto y coma y asterisco.
- Generar una lista ordenada de tokens.
- Reportar errores léxicos cuando exista un carácter o secuencia no reconocida.
- Preparar la salida para que pueda ser consumida por los demás módulos del compilador.

---

## Lo que no corresponde a mi parte

Para mantener una correcta separación de responsabilidades, mi parte no debe encargarse de funcionalidades que pertenecen a otros módulos del compilador.

No corresponde a mi trabajo:

- Validar si una consulta SQL está gramaticalmente bien escrita.
- Construir árboles sintácticos.
- Validar si una tabla existe en la base de datos.
- Validar si una columna existe dentro de una tabla.
- Ejecutar consultas SQL reales.
- Establecer conexión con MySQL, PostgreSQL o SQL Server.
- Diseñar pantallas en Swing.
- Modificar el flujo visual del sistema.
- Implementar lógica del analizador semántico.

Mi módulo debe entregar tokens claros y correctos para que los demás componentes puedan trabajar sobre ellos.

---

## Relación con el Proyecto Original en C/C++

El proyecto base desarrollado en C/C++ contiene una implementación inicial del análisis léxico. Mi tarea consiste en tomar esa lógica como referencia y convertirla en una versión más organizada, mantenible y portable en Java.

En C/C++, el análisis léxico suele manejarse mediante estructuras, arreglos de caracteres, punteros y funciones procedurales. En Java, esta lógica debe trasladarse a clases, objetos, enumeraciones y listas, aprovechando las ventajas del paradigma orientado a objetos.

Algunas equivalencias generales son:

| En C/C++ | En Java |
|---|---|
| `struct Token` | Clase `Token` |
| `enum TokenType` | Enumeración `TokenType` |
| Función `lexer()` | Clase `Lexer` con método `tokenize()` |
| Arreglos de caracteres | `String`, `charAt()` y `StringBuilder` |
| Vectores o arreglos de tokens | `List<Token>` |
| Validaciones con `if` y `switch` | Métodos auxiliares bien separados |
| Manejo manual de índices | Control interno con posición, línea y columna |

El objetivo no es copiar el código C/C++ línea por línea, sino reinterpretar su lógica y construir una versión más clara y extensible en Java.

---

## Resultado Esperado de mi Módulo

Al finalizar mi parte, el proyecto deberá contar con un módulo léxico capaz de recibir sentencias SQL y generar una lista de tokens confiable.

Por ejemplo, al recibir:

```sql
SELECT nombre FROM clientes WHERE edad >= 18;
```

El analizador léxico debe producir una salida similar a:

```text
Token{type=SELECT, lexeme='SELECT'}
Token{type=IDENTIFIER, lexeme='nombre'}
Token{type=FROM, lexeme='FROM'}
Token{type=IDENTIFIER, lexeme='clientes'}
Token{type=WHERE, lexeme='WHERE'}
Token{type=IDENTIFIER, lexeme='edad'}
Token{type=GREATER_EQUAL, lexeme='>='}
Token{type=NUMBER, lexeme='18'}
Token{type=SEMICOLON, lexeme=';'}
Token{type=EOF, lexeme=''}
```

Esta información podrá mostrarse en pantalla, enviarse al analizador sintáctico o utilizarse para depuración del proceso de compilación.

---

## Importancia de mi Parte

El analizador léxico es una pieza fundamental porque representa la entrada formal al proceso de compilación. Si los tokens se generan incorrectamente, los módulos posteriores no podrán validar correctamente la sentencia SQL.

Una mala clasificación de tokens puede provocar errores en el análisis sintáctico, errores semánticos falsos o una mala interpretación de la consulta ingresada por el usuario.

Por esa razón, mi módulo debe ser:

- Claro.
- Ordenado.
- Eficiente.
- Fácil de mantener.
- Fácil de extender.
- Independiente de la interfaz gráfica.
- Compatible con futuras reglas para distintos motores SQL.

---

## Enfoque Técnico

La implementación en Java debe seguir una estructura limpia dentro del modelo del proyecto MVC. El analizador léxico debe estar separado de las vistas y de los controladores, funcionando como una clase de backend que recibe texto y devuelve una estructura de datos procesada.

La lógica debería dividirse en componentes como:

- `TokenType`: enum con los tipos de tokens soportados.
- `Token`: clase que representa un token individual.
- `Lexer`: clase encargada de recorrer la entrada y generar tokens.
- Métodos auxiliares para leer identificadores, números, cadenas, operadores y símbolos.
- Manejo de errores léxicos con mensajes claros.

Este diseño permitirá que el módulo pueda probarse de forma independiente, sin necesidad de depender de la interfaz gráfica.

---

## Motores SQL Considerados

Aunque el proyecto toma como base principal MySQL, el analizador léxico debe diseñarse de forma flexible para poder reconocer diferencias entre motores SQL.

Por ejemplo:

- MySQL utiliza backticks para nombres de objetos: `` `tabla` ``.
- SQL Server puede utilizar corchetes: `[tabla]`.
- PostgreSQL utiliza comillas dobles para identificadores: `"tabla"`.

En esta etapa, mi responsabilidad es preparar una base léxica ordenada que pueda crecer posteriormente para soportar estas variantes.

---

## Conclusión

Mi parte dentro del proyecto consiste en desarrollar el módulo encargado de convertir sentencias SQL en tokens, tomando como referencia el compilador base en C/C++ y adaptándolo a una solución Java con arquitectura MVC.

Este trabajo será la base para que el analizador sintáctico y el analizador semántico puedan funcionar correctamente. Mi enfoque estará en construir un analizador léxico limpio, portable, eficiente y preparado para futuras extensiones hacia distintos motores SQL.

