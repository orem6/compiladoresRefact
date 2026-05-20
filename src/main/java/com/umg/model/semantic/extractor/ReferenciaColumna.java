package com.umg.model.semantic.extractor;

public class ReferenciaColumna {
    private String nombre;
    private String tablaOAlias;
    private int linea;
    private int columna;

    public ReferenciaColumna() {}

    public ReferenciaColumna(String nombre, String tablaOAlias, int linea, int columna) {
        this.nombre = nombre;
        this.tablaOAlias = tablaOAlias;
        this.linea = linea;
        this.columna = columna;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTablaOAlias() { return tablaOAlias; }
    public void setTablaOAlias(String tablaOAlias) { this.tablaOAlias = tablaOAlias; }

    public int getLinea() { return linea; }
    public void setLinea(int linea) { this.linea = linea; }

    public int getColumna() { return columna; }
    public void setColumna(int columna) { this.columna = columna; }

    @Override
    public String toString() {
        if (tablaOAlias != null) return tablaOAlias + "." + nombre;
        return nombre;
    }
}
