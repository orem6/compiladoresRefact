package com.umg.model.semantic.util;

import com.umg.model.dialect.SqlDialect;

public class IdentifierNormalizer {

    public String normalizar(String raw) {
        if (raw == null || raw.isEmpty()) return raw;
        String s = raw.trim();
        if (s.isEmpty()) return raw;
        if ((s.startsWith("`") && s.endsWith("`")) ||
            (s.startsWith("\"") && s.endsWith("\"")) ||
            (s.startsWith("[") && s.endsWith("]"))) {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }

    public String normalizar(String raw, SqlDialect dialecto) {
        String s = normalizar(raw);
        if (s != null && dialecto != null) {
            if (dialecto == SqlDialect.POSTGRESQL) {
                return s.toLowerCase();
            }
        }
        return s;
    }

    public String[] separarPartes(String raw) {
        String s = normalizar(raw);
        if (s == null) return new String[0];
        return s.split("\\.");
    }

    public String extraerUltimaParte(String raw) {
        String[] partes = separarPartes(raw);
        if (partes.length == 0) return "";
        return partes[partes.length - 1];
    }

    public String extraerCalificador(String raw) {
        String[] partes = separarPartes(raw);
        if (partes.length <= 1) return null;
        return partes[0];
    }
}
