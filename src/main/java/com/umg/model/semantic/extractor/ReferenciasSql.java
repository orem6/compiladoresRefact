package com.umg.model.semantic.extractor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReferenciasSql {
    private List<ReferenciaTabla> tablas;
    private List<ReferenciaColumna> columnas;
    private Map<String, ReferenciaTabla> aliases;
    private String tipoSentencia;

    public ReferenciasSql() {
        this.tablas = new ArrayList<>();
        this.columnas = new ArrayList<>();
        this.aliases = new HashMap<>();
    }

    public List<ReferenciaTabla> getTablas() { return tablas; }
    public void setTablas(List<ReferenciaTabla> tablas) { this.tablas = tablas; }
    public void addTabla(ReferenciaTabla tabla) { this.tablas.add(tabla); }

    public List<ReferenciaColumna> getColumnas() { return columnas; }
    public void setColumnas(List<ReferenciaColumna> columnas) { this.columnas = columnas; }
    public void addColumna(ReferenciaColumna columna) { this.columnas.add(columna); }

    public Map<String, ReferenciaTabla> getAliases() { return aliases; }
    public void setAliases(Map<String, ReferenciaTabla> aliases) { this.aliases = aliases; }
    public void addAlias(String nombre, ReferenciaTabla tabla) { this.aliases.put(nombre.toUpperCase(), tabla); }

    public String getTipoSentencia() { return tipoSentencia; }
    public void setTipoSentencia(String tipoSentencia) { this.tipoSentencia = tipoSentencia; }

    public boolean tieneAlias(String nombre) {
        return aliases.containsKey(nombre.toUpperCase());
    }

    public ReferenciaTabla getTablaPorAlias(String alias) {
        return aliases.get(alias.toUpperCase());
    }
}
