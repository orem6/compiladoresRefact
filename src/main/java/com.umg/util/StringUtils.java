package com.dataquery.sqlcompiler.util;

public class StringUtils {
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static String normalizeWhitespace(String str) {
        if (str == null) return "";
        return str.trim().replaceAll("\\s+", " ");
    }

    public static boolean endsWithSemicolon(String str) {
        return str != null && str.trim().endsWith(";");
    }
}
