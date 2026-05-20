package com.umg.model.semantic.metadata;

import java.util.List;

public interface DatabaseMetadataService {
    boolean probarConexion();
    boolean existeTabla(String esquema, String tabla);
    boolean existeColumna(String esquema, String tabla, String columna);
    List<String> obtenerColumnas(String esquema, String tabla);
    List<String> obtenerTablas(String esquema);
    void cerrar();
}
