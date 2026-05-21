package com.umg.model.semantic.validator;

import com.umg.model.dialect.SqlDialect;
import com.umg.model.semantic.extractor.ReferenciasSql;
import com.umg.model.semantic.extractor.ReferenciaTabla;
import com.umg.model.semantic.extractor.ReferenciaColumna;
import com.umg.model.semantic.metadata.DatabaseMetadataService;
import com.umg.model.semantic.result.ErrorSemantico;
import com.umg.model.semantic.result.ResultadoSemantico;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SemanticValidatorCql {

    public ResultadoSemantico validar(ReferenciasSql referencias, DatabaseMetadataService metadata,
                                       SqlDialect dialect, boolean ifExists) {
        ResultadoSemantico resultado = new ResultadoSemantico();
        List<ErrorSemantico> errores = new ArrayList<>();
        List<String> advertencias = new ArrayList<>();

        String tipoSentencia = referencias.getTipoSentencia();

        if (tipoSentencia == null) {
            resultado.setValido(true);
            resultado.setMensaje("Tipo de sentencia no determinado. Sin validacion.");
            return resultado;
        }

        switch (tipoSentencia.toUpperCase()) {
            case "SELECT" -> validarSelect(referencias, metadata, errores);
            case "INSERT" -> validarInsert(referencias, metadata, errores);
            case "UPDATE" -> validarUpdate(referencias, metadata, errores);
            case "DELETE" -> validarDelete(referencias, metadata, errores);
            case "CREATE TABLE" -> validarCreateTable(referencias, metadata, ifExists, errores, advertencias);
            case "CREATE KEYSPACE" -> validarCreateKeyspace(referencias, metadata, ifExists, errores, advertencias);
            case "DROP TABLE" -> validarDropTable(referencias, metadata, ifExists, errores);
            case "DROP KEYSPACE" -> validarDropKeyspace(referencias, metadata, ifExists, errores);
            case "ALTER TABLE" -> validarAlterTable(referencias, metadata, errores);
            case "TRUNCATE" -> validarTruncate(referencias, metadata, errores);
            default -> advertencias.add("Tipo de sentencia CQL '" + tipoSentencia + "' no tiene validacion especifica.");
        }

        resultado.setErroresSemanticos(errores);
        resultado.setAdvertencias(advertencias);
        resultado.setValido(errores.isEmpty());
        resultado.setMensaje(errores.isEmpty() ? "Validacion CQL completada sin errores." : "Se encontraron errores semanticos en la sentencia CQL.");

        return resultado;
    }

    private void validarSelect(ReferenciasSql referencias, DatabaseMetadataService metadata, List<ErrorSemantico> errores) {
        Set<String> tablasEntidad = new HashSet<>();
        for (ReferenciaTabla tabla : referencias.getTablas()) {
            String entidad = tabla.getNombreCompleto();
            if (tablasEntidad.add(entidad)) {
                if (!metadata.existeTabla(tabla.getEsquema(), tabla.getNombre())) {
                    errores.add(new ErrorSemantico(
                        "SEM_TABLE_NOT_FOUND", "La tabla '" + entidad + "' no existe en Cassandra",
                        entidad, null, tabla.getLinea(), tabla.getColumna()));
                }
            }
        }

        for (ReferenciaColumna col : referencias.getColumnas()) {
            if (!col.esComodin() && !col.esExpresion()) {
                for (ReferenciaTabla tabla : referencias.getTablas()) {
                    if (tabla.getNombre().equalsIgnoreCase(col.getTabla()) || col.getTabla() == null) {
                        String esquema = col.getTabla() != null ? tabla.getEsquema() : null;
                        if (!metadata.existeColumna(esquema, tabla.getNombre(), col.getNombre())) {
                            errores.add(new ErrorSemantico(
                                "SEM_COLUMN_NOT_FOUND", "La columna '" + col.getNombreCompleto() + "' no existe en la tabla '" + tabla.getNombreCompleto() + "'",
                                col.getNombreCompleto(), null, col.getLinea(), col.getColumna()));
                        }
                        break;
                    }
                }
            }
        }
    }

    private void validarInsert(ReferenciasSql referencias, DatabaseMetadataService metadata, List<ErrorSemantico> errores) {
        for (ReferenciaTabla tabla : referencias.getTablas()) {
            if (!metadata.existeTabla(tabla.getEsquema(), tabla.getNombre())) {
                errores.add(new ErrorSemantico(
                    "SEM_TABLE_NOT_FOUND", "La tabla '" + tabla.getNombreCompleto() + "' no existe en Cassandra",
                    tabla.getNombreCompleto(), null, tabla.getLinea(), tabla.getColumna()));
            }
        }

        for (ReferenciaColumna col : referencias.getColumnas()) {
            for (ReferenciaTabla tabla : referencias.getTablas()) {
                if (tabla.getNombre().equalsIgnoreCase(col.getTabla()) || col.getTabla() == null) {
                    if (!metadata.existeColumna(tabla.getEsquema(), tabla.getNombre(), col.getNombre())) {
                        errores.add(new ErrorSemantico(
                            "SEM_COLUMN_NOT_FOUND", "La columna '" + col.getNombreCompleto() + "' no existe en la tabla '" + tabla.getNombreCompleto() + "'",
                            col.getNombreCompleto(), null, col.getLinea(), col.getColumna()));
                    }
                    break;
                }
            }
        }
    }

    private void validarUpdate(ReferenciasSql referencias, DatabaseMetadataService metadata, List<ErrorSemantico> errores) {
        validarInsert(referencias, metadata, errores);
    }

    private void validarDelete(ReferenciasSql referencias, DatabaseMetadataService metadata, List<ErrorSemantico> errores) {
        for (ReferenciaTabla tabla : referencias.getTablas()) {
            if (!metadata.existeTabla(tabla.getEsquema(), tabla.getNombre())) {
                errores.add(new ErrorSemantico(
                    "SEM_TABLE_NOT_FOUND", "La tabla '" + tabla.getNombreCompleto() + "' no existe en Cassandra",
                    tabla.getNombreCompleto(), null, tabla.getLinea(), tabla.getColumna()));
            }
        }
    }

    private void validarCreateTable(ReferenciasSql referencias, DatabaseMetadataService metadata,
                                     boolean ifExists, List<ErrorSemantico> errores, List<String> advertencias) {
        for (ReferenciaTabla tabla : referencias.getTablas()) {
            boolean existe = metadata.existeTabla(tabla.getEsquema(), tabla.getNombre());
            if (existe && !ifExists) {
                errores.add(new ErrorSemantico(
                    "SEM_TABLE_ALREADY_EXISTS", "La tabla '" + tabla.getNombreCompleto() + "' ya existe en Cassandra",
                    tabla.getNombreCompleto(), null, tabla.getLinea(), tabla.getColumna()));
            } else if (existe) {
                advertencias.add("La tabla '" + tabla.getNombreCompleto() + "' ya existe. IF NOT EXISTS evita el error.");
            }
        }
    }

    private void validarCreateKeyspace(ReferenciasSql referencias, DatabaseMetadataService metadata,
                                        boolean ifExists, List<ErrorSemantico> errores, List<String> advertencias) {
    }

    private void validarDropTable(ReferenciasSql referencias, DatabaseMetadataService metadata,
                                   boolean ifExists, List<ErrorSemantico> errores) {
        for (ReferenciaTabla tabla : referencias.getTablas()) {
            boolean existe = metadata.existeTabla(tabla.getEsquema(), tabla.getNombre());
            if (!existe && !ifExists) {
                errores.add(new ErrorSemantico(
                    "SEM_TABLE_NOT_FOUND", "La tabla '" + tabla.getNombreCompleto() + "' no existe en Cassandra",
                    tabla.getNombreCompleto(), null, tabla.getLinea(), tabla.getColumna()));
            }
        }
    }

    private void validarDropKeyspace(ReferenciasSql referencias, DatabaseMetadataService metadata,
                                      boolean ifExists, List<ErrorSemantico> errores) {
    }

    private void validarAlterTable(ReferenciasSql referencias, DatabaseMetadataService metadata, List<ErrorSemantico> errores) {
        for (ReferenciaTabla tabla : referencias.getTablas()) {
            if (!metadata.existeTabla(tabla.getEsquema(), tabla.getNombre())) {
                errores.add(new ErrorSemantico(
                    "SEM_TABLE_NOT_FOUND", "La tabla '" + tabla.getNombreCompleto() + "' no existe en Cassandra",
                    tabla.getNombreCompleto(), null, tabla.getLinea(), tabla.getColumna()));
            }
        }
    }

    private void validarTruncate(ReferenciasSql referencias, DatabaseMetadataService metadata, List<ErrorSemantico> errores) {
        validarDelete(referencias, metadata, errores);
    }
}
