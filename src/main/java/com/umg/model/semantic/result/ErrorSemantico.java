package com.umg.model.semantic.result;

public class ErrorSemantico {
    private String codigo;
    private String mensaje;
    private String entidad;
    private String tipoEntidad;
    private int linea;
    private int columna;

    public ErrorSemantico() {}

    public ErrorSemantico(String codigo, String mensaje, String entidad, String tipoEntidad, int linea, int columna) {
        this.codigo = codigo;
        this.mensaje = mensaje;
        this.entidad = entidad;
        this.tipoEntidad = tipoEntidad;
        this.linea = linea;
        this.columna = columna;
    }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public String getEntidad() { return entidad; }
    public void setEntidad(String entidad) { this.entidad = entidad; }

    public String getTipoEntidad() { return tipoEntidad; }
    public void setTipoEntidad(String tipoEntidad) { this.tipoEntidad = tipoEntidad; }

    public int getLinea() { return linea; }
    public void setLinea(int linea) { this.linea = linea; }

    public int getColumna() { return columna; }
    public void setColumna(int columna) { this.columna = columna; }

    @Override
    public String toString() {
        return "[" + codigo + "] " + mensaje + " (linea " + linea + ", col " + columna + ")";
    }
}
