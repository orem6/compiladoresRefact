package com.umg.model.symbol;

public class Symbol {
    private final String name;
    private final SymbolType type;
    private String dataType;

    public Symbol(String name, SymbolType type) {
        this.name = name;
        this.type = type;
    }

    public Symbol(String name, SymbolType type, String dataType) {
        this.name = name;
        this.type = type;
        this.dataType = dataType;
    }

    public String getName() { return name; }
    public SymbolType getType() { return type; }
    public String getDataType() { return dataType; }
    public void setDataType(String dataType) { this.dataType = dataType; }

    @Override
    public String toString() {
        return "Symbol{name='" + name + "', type=" + type + ", dataType='" + dataType + "'}";
    }
}
