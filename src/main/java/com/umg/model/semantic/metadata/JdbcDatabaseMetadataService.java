package com.umg.model.semantic.metadata;

import com.umg.model.dialect.SqlDialect;
import com.umg.model.semantic.config.ConexionBaseDatosConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JdbcDatabaseMetadataService implements DatabaseMetadataService {
    private final Connection conexion;
    private final SqlDialect dialecto;
    private final DatabaseMetaData metaData;

    public JdbcDatabaseMetadataService(Connection conexion, SqlDialect dialecto) throws SQLException {
        this.conexion = conexion;
        this.dialecto = dialecto;
        this.metaData = conexion.getMetaData();
    }

    @Override
    public boolean probarConexion() {
        try {
            return conexion != null && !conexion.isClosed() && conexion.isValid(5);
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public boolean existeTabla(String esquema, String tabla) {
        if (tabla == null || tabla.isEmpty()) return false;
        try (ResultSet rs = metaData.getTables(
                null, esquema != null ? esquema : obtenerEsquemaPorDefecto(),
                tabla, new String[]{"TABLE", "VIEW"})) {
            return rs.next();
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public boolean existeColumna(String esquema, String tabla, String columna) {
        if (tabla == null || tabla.isEmpty() || columna == null || columna.isEmpty()) return false;
        try (ResultSet rs = metaData.getColumns(
                null, esquema != null ? esquema : obtenerEsquemaPorDefecto(),
                tabla, columna)) {
            return rs.next();
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public List<String> obtenerColumnas(String esquema, String tabla) {
        List<String> columnas = new ArrayList<>();
        try (ResultSet rs = metaData.getColumns(
                null, esquema != null ? esquema : obtenerEsquemaPorDefecto(),
                tabla, null)) {
            while (rs.next()) {
                columnas.add(rs.getString("COLUMN_NAME"));
            }
        } catch (SQLException e) {
            return Collections.emptyList();
        }
        return columnas;
    }

    @Override
    public List<String> obtenerTablas(String esquema) {
        List<String> tablas = new ArrayList<>();
        try (ResultSet rs = metaData.getTables(
                null, esquema != null ? esquema : obtenerEsquemaPorDefecto(),
                null, new String[]{"TABLE", "VIEW"})) {
            while (rs.next()) {
                tablas.add(rs.getString("TABLE_NAME"));
            }
        } catch (SQLException e) {
            return Collections.emptyList();
        }
        return tablas;
    }

    @Override
    public void cerrar() {
        try {
            if (conexion != null && !conexion.isClosed()) {
                conexion.close();
            }
        } catch (SQLException e) {
            // ignorar
        }
    }

    private String obtenerEsquemaPorDefecto() {
        if (dialecto == SqlDialect.POSTGRESQL) return "public";
        if (dialecto == SqlDialect.SQL_SERVER) return "dbo";
        return null;
    }
}
