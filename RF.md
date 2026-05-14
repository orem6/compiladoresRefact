# 📋 Requerimientos Funcionales
## Proyecto: Refactorización de Compilador SQL (C++ → Java JNI)

---

| ID | Nombre del Requerimiento | Descripción | Entradas | Salidas | Prioridad |
|:---|:---|:---|:---|:---|:---|
| **RF-01** | Recepción de consultas SQL | El sistema deberá permitir el ingreso de consultas SQL en formato texto para su análisis y validación. | Consulta SQL | Resultado del análisis | 🔴 Alta |


| **RF-02** | Tokenización de consultas SQL | El sistema deberá dividir la consulta SQL en tokens identificables como palabras reservadas, operadores e identificadores. | Consulta SQL | Lista de tokens | 🔴 Alta |


| **RF-03** | Análisis Sintáctico (Parsing) | El sistema deberá validar que la estructura de la consulta SQL cumpla con las reglas gramaticales establecidas. | Tokens generados | Validación sintáctica | 🔴 Alta |


| **RF-04** | Validación Semántica | El sistema deberá validar reglas semánticas básicas como existencia de tablas y columnas. | Consulta SQL | Resultado semántico | 🟡 Media |


| **RF-05** | Generación de Mensajes de Error | El sistema deberá mostrar mensajes claros para errores léxicos, sintácticos o semánticos. | Errores detectados | Mensajes de error | 🔴 Alta |

| **RF-06** | Integración JNI | El sistema deberá permitir comunicación entre Java y componentes C++ mediante JNI. | Funciones nativas | Resultado desde C++ | 🟡 Media |


| **RF-07** | Portabilidad Multiplataforma | El sistema deberá ejecutarse en Windows, Linux y macOS. | Sistema operativo | Ejecución compatible | 🟡 Media |


| **RF-08** | Gestión Automática de Memoria | El sistema deberá utilizar la gestión automática de memoria de Java para evitar fugas de memoria. | Procesamiento interno | Liberación automática de memoria | 🔴 Alta |


| **RF-09** | Modularidad del Sistema | El sistema deberá estar organizado por módulos independientes para facilitar mantenimiento y escalabilidad. | Componentes del sistema | Arquitectura modular | 🔴 Alta |


| **RF-10** | Ejecución desde Consola | El sistema deberá permitir ejecución desde terminal o consola. | Comando de ejecución | Inicio del compilador | 🟢 Baja |


| **RF-11** | Mantenimiento del Comportamiento Original | El sistema refactorizado deberá mantener el comportamiento funcional del compilador original en C++. | Consultas SQL | Resultados equivalentes | 🔴 Alta |


| **RF-12** | Registro de Resultados | El sistema deberá mostrar los resultados del análisis realizado en consola. | Resultado del análisis | Mensajes en consola | 🟡 Media |

---

## 🔴 Prioridad Alta
Requerimientos críticos para el funcionamiento principal del compilador.

## 🟡 Prioridad Media
Requerimientos importantes para compatibilidad y optimización.

## 🟢 Prioridad Baja
Requerimientos complementarios del sistema.




