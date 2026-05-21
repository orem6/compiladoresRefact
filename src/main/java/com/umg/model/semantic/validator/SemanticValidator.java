package com.umg.model.semantic.validator;

import com.umg.model.dialect.SqlDialect;
import com.umg.model.semantic.extractor.ReferenciaColumna;
import com.umg.model.semantic.extractor.ReferenciaTabla;
import com.umg.model.semantic.extractor.ReferenciasSql;
import com.umg.model.semantic.metadata.DatabaseMetadataService;
import com.umg.model.semantic.result.ErrorSemantico;
import com.umg.model.semantic.result.ResultadoSemantico;
import com.umg.model.semantic.util.IdentifierNormalizer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SemanticValidator {

    public ResultadoSemantico validar(ReferenciasSql refs, DatabaseMetadataService metadata,
                                      SqlDialect dialecto, boolean ifExists) {
        ResultadoSemantico resultado = new ResultadoSemantico();
        resultado.setValido(true);
        resultado.setDialecto(dialecto);
        resultado.setMensaje("Analisis semantico completado sin errores");

        if (!metadata.probarConexion()) {
            resultado.setValido(false);
            resultado.setMensaje("No fue posible conectar con la base de datos");
            resultado.addErrorSemantico(new ErrorSemantico(
                "SEM_CONNECTION_ERROR",
                "No fue posible conectar con la base de datos. Verifica la configuracion.",
                null, null, 0, 0));
            return resultado;
        }

        String tipo = refs.getTipoSentencia();
        List<ErrorSemantico> errores = new ArrayList<>();

        switch (tipo) {
            case "SELECT" -> validarSelect(refs, metadata, dialecto, errores);
            case "INSERT" -> validarInsert(refs, metadata, dialecto, errores);
            case "UPDATE" -> validarUpdate(refs, metadata, dialecto, errores);
            case "DELETE" -> validarDelete(refs, metadata, dialecto, errores);
            case "CREATE" -> validarCreate(refs, metadata, dialecto, errores, ifExists);
            case "DROP" -> validarDrop(refs, metadata, dialecto, errores, ifExists);
            case "ALTER" -> validarAlter(refs, metadata, dialecto, errores);
            case "TRUNCATE" -> validarTruncate(refs, metadata, dialecto, errores);
        }

        if (!errores.isEmpty()) {
            resultado.setValido(false);
            resultado.setMensaje("Se encontraron errores semanticos");
            resultado.setErroresSemanticos(errores);
        }

        return resultado;
    }

    private void validarSelect(ReferenciasSql refs, DatabaseMetadataService metadata,
                               SqlDialect dialecto, List<ErrorSemantico> errores) {
        IdentifierNormalizer normalizer = new IdentifierNormalizer();
        Set<String> tablasValidas = new HashSet<>();

        for (ReferenciaTabla rt : refs.getTablas()) {
            String nombre = normalizer.normalizar(rt.getNombre(), dialecto);
            String esquema = rt.getEsquema() != null ? normalizer.normalizar(rt.getEsquema(), dialecto) : null;

            if (refs.tieneAlias(nombre)) continue;

            if (!metadata.existeTabla(esquema, nombre)) {
                errores.add(new ErrorSemantico(
                    "SEM_TABLE_NOT_FOUND",
                    "La tabla '" + nombre + "' no existe"
                        + (esquema != null ? " en el esquema '" + esquema + "'" : ""),
                    nombre, "TABLE", rt.getLinea(), rt.getColumna()));
            } else {
                tablasValidas.add(nombre.toUpperCase());
                if (rt.getAlias() != null) {
                    tablasValidas.add(rt.getAlias().toUpperCase());
                }
            }
        }

        for (ReferenciaColumna rc : refs.getColumnas()) {
            String colName = normalizer.normalizar(rc.getNombre(), dialecto);
            String tablaOAlias = rc.getTablaOAlias() != null
                ? normalizer.normalizar(rc.getTablaOAlias(), dialecto) : null;

            if (tablaOAlias != null) {
                ReferenciaTabla tablaRef = refs.getTablaPorAlias(tablaOAlias);
                if (tablaRef == null) {
                    for (ReferenciaTabla rt : refs.getTablas()) {
                        String nombreNormalizado = normalizer.normalizar(rt.getNombre(), dialecto);
                        if (nombreNormalizado.equalsIgnoreCase(tablaOAlias)) {
                            tablaRef = rt;
                            break;
                        }
                    }
                }
                if (tablaRef == null) {
                    errores.add(new ErrorSemantico(
                        "SEM_ALIAS_NOT_FOUND",
                        "El alias o tabla '" + tablaOAlias + "' no esta declarado",
                        tablaOAlias, "ALIAS", rc.getLinea(), rc.getColumna()));
                    continue;
                }
                String tablaNombre = normalizer.normalizar(tablaRef.getNombre(), dialecto);
                String esquema = tablaRef.getEsquema() != null
                    ? normalizer.normalizar(tablaRef.getEsquema(), dialecto) : null;

                if (!metadata.existeColumna(esquema, tablaNombre, colName)) {
                    errores.add(new ErrorSemantico(
                        "SEM_COLUMN_NOT_FOUND",
                        "La columna '" + colName + "' no existe en la tabla '" + tablaNombre + "'",
                        colName, "COLUMN", rc.getLinea(), rc.getColumna()));
                }
            } else {
                List<String> tablasEncontradas = new ArrayList<>();
                for (ReferenciaTabla rt : refs.getTablas()) {
                    String tNombre = normalizer.normalizar(rt.getNombre(), dialecto);
                    String tEsquema = rt.getEsquema() != null
                        ? normalizer.normalizar(rt.getEsquema(), dialecto) : null;
                    if (metadata.existeColumna(tEsquema, tNombre, colName)) {
                        tablasEncontradas.add(tNombre);
                    }
                    if (rt.getAlias() != null) {
                        if (metadata.existeColumna(tEsquema, tNombre, colName)) {
                            if (!tablasEncontradas.contains(tNombre)) {
                                tablasEncontradas.add(tNombre);
                            }
                        }
                    }
                }
                if (tablasEncontradas.isEmpty()) {
                    errores.add(new ErrorSemantico(
                        "SEM_COLUMN_NOT_FOUND",
                        "La columna '" + colName + "' no existe en ninguna tabla de la consulta",
                        colName, "COLUMN", rc.getLinea(), rc.getColumna()));
                } else if (tablasEncontradas.size() > 1) {
                    errores.add(new ErrorSemantico(
                        "SEM_AMBIGUOUS_COLUMN",
                        "La columna '" + colName + "' es ambigua, existe en: "
                            + String.join(", ", tablasEncontradas),
                        colName, "COLUMN", rc.getLinea(), rc.getColumna()));
                }
            }
        }
    }

    private void validarInsert(ReferenciasSql refs, DatabaseMetadataService metadata,
                               SqlDialect dialecto, List<ErrorSemantico> errores) {
        IdentifierNormalizer normalizer = new IdentifierNormalizer();
        for (ReferenciaTabla rt : refs.getTablas()) {
            String nombre = normalizer.normalizar(rt.getNombre(), dialecto);
            String esquema = rt.getEsquema() != null
                ? normalizer.normalizar(rt.getEsquema(), dialecto) : null;

            if (!metadata.existeTabla(esquema, nombre)) {
                errores.add(new ErrorSemantico(
                    "SEM_TABLE_NOT_FOUND",
                    "La tabla '" + nombre + "' no existe"
                        + (esquema != null ? " en el esquema '" + esquema + "'" : ""),
                    nombre, "TABLE", rt.getLinea(), rt.getColumna()));
            } else {
                for (ReferenciaColumna rc : refs.getColumnas()) {
                    String colName = normalizer.normalizar(rc.getNombre(), dialecto);
                    if (!metadata.existeColumna(esquema, nombre, colName)) {
                        errores.add(new ErrorSemantico(
                            "SEM_COLUMN_NOT_FOUND",
                            "La columna '" + colName + "' no existe en la tabla '" + nombre + "'",
                            colName, "COLUMN", rc.getLinea(), rc.getColumna()));
                    }
                }
            }
        }
    }

    private void validarUpdate(ReferenciasSql refs, DatabaseMetadataService metadata,
                               SqlDialect dialecto, List<ErrorSemantico> errores) {
        IdentifierNormalizer normalizer = new IdentifierNormalizer();
        for (ReferenciaTabla rt : refs.getTablas()) {
            String nombre = normalizer.normalizar(rt.getNombre(), dialecto);
            String esquema = rt.getEsquema() != null
                ? normalizer.normalizar(rt.getEsquema(), dialecto) : null;

            if (!metadata.existeTabla(esquema, nombre)) {
                errores.add(new ErrorSemantico(
                    "SEM_TABLE_NOT_FOUND",
                    "La tabla '" + nombre + "' no existe"
                        + (esquema != null ? " en el esquema '" + esquema + "'" : ""),
                    nombre, "TABLE", rt.getLinea(), rt.getColumna()));
            } else {
                for (ReferenciaColumna rc : refs.getColumnas()) {
                    String colName = normalizer.normalizar(rc.getNombre(), dialecto);
                    if (!metadata.existeColumna(esquema, nombre, colName)) {
                        errores.add(new ErrorSemantico(
                            "SEM_COLUMN_NOT_FOUND",
                            "La columna '" + colName + "' no existe en la tabla '" + nombre + "'",
                            colName, "COLUMN", rc.getLinea(), rc.getColumna()));
                    }
                }
            }
        }
    }

    private void validarDelete(ReferenciasSql refs, DatabaseMetadataService metadata,
                               SqlDialect dialecto, List<ErrorSemantico> errores) {
        IdentifierNormalizer normalizer = new IdentifierNormalizer();
        for (ReferenciaTabla rt : refs.getTablas()) {
            String nombre = normalizer.normalizar(rt.getNombre(), dialecto);
            String esquema = rt.getEsquema() != null
                ? normalizer.normalizar(rt.getEsquema(), dialecto) : null;

            if (!metadata.existeTabla(esquema, nombre)) {
                errores.add(new ErrorSemantico(
                    "SEM_TABLE_NOT_FOUND",
                    "La tabla '" + nombre + "' no existe"
                        + (esquema != null ? " en el esquema '" + esquema + "'" : ""),
                    nombre, "TABLE", rt.getLinea(), rt.getColumna()));
            } else {
                for (ReferenciaColumna rc : refs.getColumnas()) {
                    String colName = normalizer.normalizar(rc.getNombre(), dialecto);
                    if (!metadata.existeColumna(esquema, nombre, colName)) {
                        errores.add(new ErrorSemantico(
                            "SEM_COLUMN_NOT_FOUND",
                            "La columna '" + colName + "' no existe en la tabla '" + nombre + "'",
                            colName, "COLUMN", rc.getLinea(), rc.getColumna()));
                    }
                }
            }
        }
    }

    private void validarCreate(ReferenciasSql refs, DatabaseMetadataService metadata,
                               SqlDialect dialecto, List<ErrorSemantico> errores, boolean ifExists) {
        IdentifierNormalizer normalizer = new IdentifierNormalizer();
        for (ReferenciaTabla rt : refs.getTablas()) {
            String nombre = normalizer.normalizar(rt.getNombre(), dialecto);
            String esquema = rt.getEsquema() != null
                ? normalizer.normalizar(rt.getEsquema(), dialecto) : null;

            if (metadata.existeTabla(esquema, nombre)) {
                if (ifExists) {
                    errores.add(new ErrorSemantico(
                        "SEM_TABLE_ALREADY_EXISTS",
                        "La tabla '" + nombre + "' ya existe (IF NOT EXISTS genera advertencia)",
                        nombre, "TABLE", rt.getLinea(), rt.getColumna()));
                } else {
                    errores.add(new ErrorSemantico(
                        "SEM_TABLE_ALREADY_EXISTS",
                        "La tabla '" + nombre + "' ya existe",
                        nombre, "TABLE", rt.getLinea(), rt.getColumna()));
                }
            }
        }
    }

    private void validarDrop(ReferenciasSql refs, DatabaseMetadataService metadata,
                             SqlDialect dialecto, List<ErrorSemantico> errores, boolean ifExists) {
        IdentifierNormalizer normalizer = new IdentifierNormalizer();
        for (ReferenciaTabla rt : refs.getTablas()) {
            String nombre = normalizer.normalizar(rt.getNombre(), dialecto);
            String esquema = rt.getEsquema() != null
                ? normalizer.normalizar(rt.getEsquema(), dialecto) : null;

            if (!metadata.existeTabla(esquema, nombre)) {
                if (!ifExists) {
                    errores.add(new ErrorSemantico(
                        "SEM_TABLE_NOT_FOUND",
                        "La tabla '" + nombre + "' no existe, no se puede eliminar",
                        nombre, "TABLE", rt.getLinea(), rt.getColumna()));
                }
            }
        }
    }

    private void validarAlter(ReferenciasSql refs, DatabaseMetadataService metadata,
                              SqlDialect dialecto, List<ErrorSemantico> errores) {
        IdentifierNormalizer normalizer = new IdentifierNormalizer();
        for (ReferenciaTabla rt : refs.getTablas()) {
            String nombre = normalizer.normalizar(rt.getNombre(), dialecto);
            String esquema = rt.getEsquema() != null
                ? normalizer.normalizar(rt.getEsquema(), dialecto) : null;

            if (!metadata.existeTabla(esquema, nombre)) {
                errores.add(new ErrorSemantico(
                    "SEM_TABLE_NOT_FOUND",
                    "La tabla '" + nombre + "' no existe"
                        + (esquema != null ? " en el esquema '" + esquema + "'" : ""),
                    nombre, "TABLE", rt.getLinea(), rt.getColumna()));
            }
        }
    }

    private void validarTruncate(ReferenciasSql refs, DatabaseMetadataService metadata,
                                 SqlDialect dialecto, List<ErrorSemantico> errores) {
        IdentifierNormalizer normalizer = new IdentifierNormalizer();
        for (ReferenciaTabla rt : refs.getTablas()) {
            String nombre = normalizer.normalizar(rt.getNombre(), dialecto);
            String esquema = rt.getEsquema() != null
                ? normalizer.normalizar(rt.getEsquema(), dialecto) : null;

            if (!metadata.existeTabla(esquema, nombre)) {
                errores.add(new ErrorSemantico(
                    "SEM_TABLE_NOT_FOUND",
                    "La tabla '" + nombre + "' no existe, no se puede truncar",
                    nombre, "TABLE", rt.getLinea(), rt.getColumna()));
            }
        }
    }
}
