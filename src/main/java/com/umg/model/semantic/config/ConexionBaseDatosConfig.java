package com.umg.model.semantic.config;

import com.umg.model.dialect.SqlDialect;

public class ConexionBaseDatosConfig {
    private SqlDialect dialecto;
    private String host;
    private int puerto;
    private String baseDatos;
    private String esquema;
    private String usuario;
    private String password;
    private String urlJdbc;
    private boolean usarUrlJdbcDirecta;

    public ConexionBaseDatosConfig() {}

    public SqlDialect getDialecto() { return dialecto; }
    public void setDialecto(SqlDialect dialecto) { this.dialecto = dialecto; }

    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }

    public int getPuerto() { return puerto; }
    public void setPuerto(int puerto) { this.puerto = puerto; }

    public String getBaseDatos() { return baseDatos; }
    public void setBaseDatos(String baseDatos) { this.baseDatos = baseDatos; }

    public String getEsquema() { return esquema; }
    public void setEsquema(String esquema) { this.esquema = esquema; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getUrlJdbc() { return urlJdbc; }
    public void setUrlJdbc(String urlJdbc) { this.urlJdbc = urlJdbc; }

    public boolean isUsarUrlJdbcDirecta() { return usarUrlJdbcDirecta; }
    public void setUsarUrlJdbcDirecta(boolean usarUrlJdbcDirecta) { this.usarUrlJdbcDirecta = usarUrlJdbcDirecta; }

    public String getEsquemaPorDefecto() {
        if (esquema != null && !esquema.isEmpty()) return esquema;
        if (dialecto == SqlDialect.POSTGRESQL) return "public";
        if (dialecto == SqlDialect.SQL_SERVER) return "dbo";
        if (dialecto == SqlDialect.CASSANDRA) return baseDatos;
        return baseDatos;
    }

    public boolean esValida() {
        if (dialecto == null) return false;
        if (usarUrlJdbcDirecta) return urlJdbc != null && !urlJdbc.isEmpty();
        if (dialecto == SqlDialect.MONGODB) {
            return host != null && !host.isEmpty() && puerto > 0;
        }
        if (dialecto == SqlDialect.CASSANDRA) {
            return host != null && !host.isEmpty() && puerto > 0;
        }
        return host != null && !host.isEmpty() && puerto > 0
            && baseDatos != null && !baseDatos.isEmpty()
            && usuario != null;
    }

    public String getEsquemaParaBusqueda() {
        if (esquema != null && !esquema.isEmpty()) return esquema;
        if (dialecto == SqlDialect.POSTGRESQL) return "public";
        if (dialecto == SqlDialect.SQL_SERVER) return "dbo";
        if (dialecto == SqlDialect.CASSANDRA) return baseDatos;
        return baseDatos;
    }
}
