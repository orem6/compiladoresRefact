package com.umg.model.semantic.metadata;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.umg.model.dialect.SqlDialect;

import java.util.ArrayList;
import java.util.List;

public class CqlDatabaseMetadataService implements DatabaseMetadataService {

    private final CqlSession session;
    private final String keyspace;

    public CqlDatabaseMetadataService(CqlSession session, String keyspace) {
        this.session = session;
        this.keyspace = keyspace;
    }

    @Override
    public boolean probarConexion() {
        try {
            session.execute("SELECT release_version FROM system.local");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean existeTabla(String keyspaceName, String table) {
        String ks = keyspaceName != null ? keyspaceName : keyspace;
        if (ks == null || ks.isEmpty()) return false;
        ResultSet rs = session.execute(
            "SELECT table_name FROM system_schema.tables WHERE keyspace_name = ? AND table_name = ?",
            ks, table);
        return rs.one() != null;
    }

    @Override
    public boolean existeColumna(String keyspaceName, String table, String column) {
        String ks = keyspaceName != null ? keyspaceName : keyspace;
        if (ks == null || ks.isEmpty()) return false;
        ResultSet rs = session.execute(
            "SELECT column_name FROM system_schema.columns WHERE keyspace_name = ? AND table_name = ? AND column_name = ?",
            ks, table, column);
        return rs.one() != null;
    }

    @Override
    public List<String> obtenerColumnas(String keyspaceName, String table) {
        String ks = keyspaceName != null ? keyspaceName : keyspace;
        if (ks == null || ks.isEmpty()) return new ArrayList<>();
        ResultSet rs = session.execute(
            "SELECT column_name FROM system_schema.columns WHERE keyspace_name = ? AND table_name = ?",
            ks, table);
        List<String> columns = new ArrayList<>();
        for (Row row : rs) {
            columns.add(row.getString("column_name"));
        }
        return columns;
    }

    @Override
    public List<String> obtenerTablas(String keyspaceName) {
        String ks = keyspaceName != null ? keyspaceName : keyspace;
        if (ks == null || ks.isEmpty()) return new ArrayList<>();
        ResultSet rs = session.execute(
            "SELECT table_name FROM system_schema.tables WHERE keyspace_name = ?",
            ks);
        List<String> tables = new ArrayList<>();
        for (Row row : rs) {
            tables.add(row.getString("table_name"));
        }
        return tables;
    }

    @Override
    public void cerrar() {
        session.close();
    }
}
