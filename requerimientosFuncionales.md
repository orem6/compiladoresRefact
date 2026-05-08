# 📌 Requerimientos Funcionales  
## Proyecto: Migración de Compilador SQL de C++ a Java

---

## 🎯 RF1 — Recepción de consultas SQL
El sistema debe permitir el ingreso de consultas SQL como entrada en formato de texto a través de una interfaz interactiva.

---

## 🎯 RF2 — Validación de consultas SQL
El sistema debe evaluar la consulta ingresada y determinar si es válida o inválida conforme a las reglas sintácticas definidas del lenguaje SQL soportado.

---

## 🎯 RF3 — Análisis léxico
El sistema debe descomponer la consulta SQL en tokens reconocibles (palabras clave, identificadores, operadores y símbolos) como parte del proceso de validación.

---

## 🎯 RF4 — Análisis sintáctico
El sistema debe verificar que la estructura de la consulta SQL cumpla con la gramática definida, validando el orden y la relación entre sus componentes.

---

## 🎯 RF5 — Detección y notificación de errores
El sistema debe identificar la presencia de errores en la consulta SQL e informar al usuario cuando la consulta sea inválida.

---

## 🎯 RF6 — Interfaz interactiva en Java
El sistema debe proporcionar una interfaz interactiva desarrollada en Java que permita la interacción directa con el usuario para el ingreso y validación de consultas.

---

## 🎯 RF7 — Visualización de resultados
El sistema debe mostrar al usuario el resultado de la validación de forma clara e inmediata, indicando si la consulta es válida o inválida.

---

## 🎯 RF8 — Ejecución continua
El sistema debe permitir al usuario ingresar y validar múltiples consultas en una misma sesión sin necesidad de reiniciar la aplicación.

---

## 🎯 RF9 — Guía de uso integrada
El sistema debe incluir una sección accesible dentro de la interfaz que proporcione instrucciones básicas sobre el uso de la aplicación.

---

## 🎯 RF10 — Ejemplos de consultas
El sistema debe presentar ejemplos de consultas SQL válidas e inválidas para orientar al usuario en la correcta utilización del sistema.

---

## 🎯 RF11 — Implementación en entorno Java
El sistema debe estar completamente implementado en el lenguaje Java, eliminando cualquier dependencia de código en C++ y mecanismos de integración como JNI.

---

## 🎯 RF12 — Consistencia funcional con el sistema original
El sistema debe mantener el mismo comportamiento funcional que el compilador SQL original desarrollado en C++, garantizando equivalencia en los resultados de validación.
