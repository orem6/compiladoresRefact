package com.umg.model.semantic.validator;

import com.umg.model.semantic.metadata.DatabaseMetadataService;
import com.umg.model.semantic.result.ErrorSemantico;
import com.umg.model.semantic.result.ResultadoSemantico;

import java.util.ArrayList;
import java.util.List;

public class SemanticValidatorMongo {

    public ResultadoSemantico validar(List<String> stages, DatabaseMetadataService metadata, String database) {
        ResultadoSemantico resultado = new ResultadoSemantico();
        List<ErrorSemantico> errores = new ArrayList<>();
        List<String> advertencias = new ArrayList<>();

        if (!metadata.probarConexion()) {
            errores.add(new ErrorSemantico(
                "SEM_CONNECTION_ERROR", "No se pudo conectar a MongoDB",
                null, null, 0, 0));
            resultado.setErroresSemanticos(errores);
            resultado.setValido(false);
            resultado.setMensaje("Error de conexion a MongoDB");
            return resultado;
        }

        if (stages == null || stages.isEmpty()) {
            advertencias.add("Pipeline sin stages definidos.");
            resultado.setErroresSemanticos(errores);
            resultado.setAdvertencias(advertencias);
            resultado.setValido(true);
            resultado.setMensaje("Validacion MongoDB completada sin colecciones referenciadas.");
            return resultado;
        }

        for (String stage : stages) {
            switch (stage) {
                case "$lookup" -> advertencias.add("$lookup requiere validacion manual de coleccion externa.");
                case "$out" -> advertencias.add("$out escribira resultados en una nueva coleccion.");
                case "$merge" -> advertencias.add("$merge escribira resultados en una coleccion existente o nueva.");
                case "$geoNear" -> advertencias.add("$geoNear requiere un indice geografico.");
                case "$indexStats" -> advertencias.add("$indexStats devuelve estadisticas de indices.");
            }
        }

        List<String> collections = metadata.obtenerTablas(database);
        if (collections.isEmpty()) {
            advertencias.add("La base de datos '" + database + "' no contiene colecciones.");
        }

        resultado.setErroresSemanticos(errores);
        resultado.setAdvertencias(advertencias);
        resultado.setValido(errores.isEmpty());
        resultado.setMensaje(errores.isEmpty() ? "Validacion MongoDB completada." : "Se encontraron errores en el pipeline MongoDB.");

        return resultado;
    }
}
