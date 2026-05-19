package com.umg.model.semantic.extractor;

public class ReferenciaTabla {
    private String nombre;
    private String esquema;
    private String alias;
    private int linea;
    private int columna;

    public ReferenciaTabla() {}

    public ReferenciaTabla(String nombre, String esquema, String alias, int linea, int columna) {
        this.nombre = nombre;
        this.esquema = esquema;
        this.alias = alias;
        this.linea = linea;
        this.columna = columna;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEsquema() { return esquema; }
    public void setEsquema(String esquema) { this.esquema = esquema; }

    public String getAlias() { return alias; }
    public void setAlias(String alias) { this.alias = alias; }

    public int getLinea() { return linea; }
    public void setLinea(int linea) { this.linea = linea; }

    public int getColumna() { return columna; }
    public void setColumna(int columna) { this.columna = columna; }

    @Override
    public String toString() {
        String s = nombre;
        if (esquema != null) s = esquema + "." + s;
        if (alias != null) s += " AS " + alias;
        return s;
    }
}
