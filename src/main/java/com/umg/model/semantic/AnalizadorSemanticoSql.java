package com.umg.model.semantic;

import com.umg.model.dialect.SqlDialect;
import com.umg.model.lexer.AnalizadorSql;
import com.umg.model.lexer.ResultadoLexer;
import com.umg.model.semantic.config.ConexionBaseDatosConfig;
import com.umg.model.semantic.extractor.ReferenciasSql;
import com.umg.model.semantic.extractor.SqlReferenceExtractor;
import com.umg.model.semantic.metadata.DatabaseMetadataService;
import com.umg.model.semantic.metadata.JdbcConnectionFactory;
import com.umg.model.semantic.metadata.JdbcDatabaseMetadataService;
import com.umg.model.semantic.result.ErrorSemantico;
import com.umg.model.semantic.result.ResultadoSemantico;
import com.umg.model.semantic.validator.SemanticValidator;

import java.sql.Connection;
import java.sql.SQLException;

public class AnalizadorSemanticoSql {

    private final AnalizadorSql analizadorLexico;
    private final SqlReferenceExtractor extractor;
    private final SemanticValidator validator;
    private final JdbcConnectionFactory connectionFactory;

    public AnalizadorSemanticoSql() {
        this.analizadorLexico = new AnalizadorSql();
        this.extractor = new SqlReferenceExtractor();
        this.validator = new SemanticValidator();
        this.connectionFactory = new JdbcConnectionFactory();
    }

    public ResultadoSemantico analizar(String sql, ConexionBaseDatosConfig config) {
        ResultadoSemantico resultado = new ResultadoSemantico();
        resultado.setDialecto(config != null ? config.getDialecto() : SqlDialect.COMMON);

        ResultadoLexer resultadoLexico = analizadorLexico.analizar(sql, config != null ? config.getDialecto() : null);
        resultado.setResultadoLexer(resultadoLexico);

        if (!resultadoLexico.isValido()) {
            resultado.setValido(false);
            resultado.setMensaje("El analisis lexico/sintactico fallo. No se ejecuto validacion semantica.");
            return resultado;
        }

        if (config == null || !config.esValida()) {
            resultado.setValido(true);
            resultado.setMensaje("Sin configuracion de base de datos. Solo se ejecuto analisis lexico/sintactico.");
            return resultado;
        }

        ReferenciasSql referencias = extractor.extraer(resultadoLexico, config.getDialecto());

        if (referencias.getTipoSentencia() == null) {
            resultado.setValido(true);
            resultado.setMensaje("No se pudo determinar el tipo de sentencia. Solo analisis lexico/sintactico.");
            return resultado;
        }

        Connection conexion = null;
        try {
            conexion = connectionFactory.crearConexion(config);
            DatabaseMetadataService metadata = new JdbcDatabaseMetadataService(conexion, config.getDialecto());

            boolean ifExists = sql.toUpperCase().contains("IF NOT EXISTS")
                || sql.toUpperCase().contains("IF EXISTS");

            ResultadoSemantico resultadoValidacion = validator.validar(
                referencias, metadata, config.getDialecto(), ifExists);

            resultado.setValido(resultadoValidacion.isValido());
            resultado.setMensaje(resultadoValidacion.getMensaje());
            resultado.setErroresSemanticos(resultadoValidacion.getErroresSemanticos());
            resultado.setAdvertencias(resultadoValidacion.getAdvertencias());

        } catch (SQLException e) {
            resultado.setValido(false);
            resultado.setMensaje("Error de conexion a la base de datos");
            resultado.addErrorSemantico(new ErrorSemantico(
                "SEM_CONNECTION_ERROR",
                "No fue posible conectar con la base de datos: " + e.getMessage(),
                null, null, 0, 0));
        } finally {
            if (conexion != null) {
                try { conexion.close(); } catch (SQLException ignored) {}
            }
        }

        return resultado;
    }

    public ResultadoSemantico analizarSoloLexico(String sql) {
        ResultadoSemantico resultado = new ResultadoSemantico();
        ResultadoLexer resultadoLexico = analizadorLexico.analizar(sql);
        resultado.setResultadoLexer(resultadoLexico);
        resultado.setValido(resultadoLexico.isValido());
        resultado.setMensaje(resultadoLexico.getMensaje());
        resultado.setDialecto(resultadoLexico.getDialectoDetectado());
        return resultado;
    }
}
