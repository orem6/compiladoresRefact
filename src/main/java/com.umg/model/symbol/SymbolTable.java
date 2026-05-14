package com.dataquery.sqlcompiler.model.symbol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SymbolTable {
    private final Map<String, Symbol> symbols;

    public SymbolTable() {
        this.symbols = new HashMap<>();
    }

    public void addSymbol(Symbol symbol) {
        symbols.put(symbol.getName().toUpperCase(), symbol);
    }

    public Symbol getSymbol(String name) {
        return symbols.get(name.toUpperCase());
    }

    public boolean hasSymbol(String name) {
        return symbols.containsKey(name.toUpperCase());
    }

    public boolean removeSymbol(String name) {
        return symbols.remove(name.toUpperCase()) != null;
    }

    public List<Symbol> getAllSymbols() {
        return new ArrayList<>(symbols.values());
    }

    public void clear() {
        symbols.clear();
    }
}
