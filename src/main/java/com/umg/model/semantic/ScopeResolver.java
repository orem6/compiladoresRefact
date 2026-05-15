package com.umg.model.semantic;

import com.umg.model.symbol.SymbolTable;

public class ScopeResolver {
    private final SymbolTable symbolTable;

    public ScopeResolver(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }

    public boolean isSymbolInScope(String symbolName) {
        return symbolTable.hasSymbol(symbolName);
    }
}
