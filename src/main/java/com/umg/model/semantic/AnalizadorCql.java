package com.umg.model.semantic;

import com.umg.model.dialect.SqlDialect;
import com.umg.model.error.ErrorCollector;
import com.umg.model.lexer.*;
import com.umg.model.parser.CqlParser;
import com.umg.model.semantic.config.ConexionBaseDatosConfig;
import com.umg.model.semantic.extractor.ReferenciasSql;
import com.umg.model.semantic.extractor.SqlReferenceExtractor;
import com.umg.model.semantic.metadata.CqlConnectionFactory;
import com.umg.model.semantic.metadata.CqlDatabaseMetadataService;
import com.umg.model.semantic.metadata.DatabaseMetadataService;
import com.umg.model.semantic.result.ErrorSemantico;
import com.umg.model.semantic.result.ResultadoSemantico;
import com.umg.model.semantic.validator.SemanticValidatorCql;

public class AnalizadorCql {

    private final SqlReferenceExtractor extractor;
    private final SemanticValidatorCql validator;
    private final CqlConnectionFactory connectionFactory;

    public AnalizadorCql() {
        this.extractor = new SqlReferenceExtractor();
        this.validator = new SemanticValidatorCql();
        this.connectionFactory = new CqlConnectionFactory();
    }

    public ResultadoSemantico analizar(String cql, ConexionBaseDatosConfig config) {
        ResultadoSemantico resultado = new ResultadoSemantico();
        resultado.setDialecto(config != null ? config.getDialecto() : SqlDialect.CASSANDRA);

        ResultadoLexer resultadoLexico = analizarCql(cql, config != null ? config.getDialecto() : SqlDialect.CASSANDRA);
        resultado.setResultadoLexer(resultadoLexico);

        if (!resultadoLexico.isValido()) {
            resultado.setValido(false);
            resultado.setMensaje("El analisis lexico/sintactico CQL fallo. No se ejecuto validacion semantica.");
            return resultado;
        }

        if (config == null || !config.esValida()) {
            resultado.setValido(true);
            resultado.setMensaje("Sin configuracion de base de datos. Solo se ejecuto analisis lexico/sintactico CQL.");
            return resultado;
        }

        ReferenciasSql referencias = extractor.extraer(resultadoLexico, config.getDialecto());

        if (referencias.getTipoSentencia() == null) {
            resultado.setValido(true);
            resultado.setMensaje("No se pudo determinar el tipo de sentencia. Solo analisis lexico/sintactico.");
            return resultado;
        }

        com.datastax.oss.driver.api.core.CqlSession session = null;
        try {
            session = connectionFactory.crearConexion(config);
            DatabaseMetadataService metadata = new CqlDatabaseMetadataService(session, config.getBaseDatos());

            boolean ifExists = cql.toUpperCase().contains("IF NOT EXISTS")
                || cql.toUpperCase().contains("IF EXISTS");

            ResultadoSemantico resultadoValidacion = validator.validar(
                referencias, metadata, config.getDialecto(), ifExists);

            resultado.setValido(resultadoValidacion.isValido());
            resultado.setMensaje(resultadoValidacion.getMensaje());
            resultado.setErroresSemanticos(resultadoValidacion.getErroresSemanticos());
            resultado.setAdvertencias(resultadoValidacion.getAdvertencias());

        } catch (Exception e) {
            resultado.setValido(false);
            resultado.setMensaje("Error de conexion a Cassandra");
            resultado.addErrorSemantico(new ErrorSemantico(
                "SEM_CONNECTION_ERROR",
                "No fue posible conectar con Cassandra: " + e.getMessage(),
                null, null, 0, 0));
        } finally {
            if (session != null) {
                session.close();
            }
        }

        return resultado;
    }

    public ResultadoSemantico analizarSoloLexico(String cql) {
        ResultadoSemantico resultado = new ResultadoSemantico();
        ResultadoLexer resultadoLexico = analizarCql(cql, SqlDialect.CASSANDRA);
        resultado.setResultadoLexer(resultadoLexico);
        resultado.setValido(resultadoLexico.isValido());
        resultado.setMensaje(resultadoLexico.getMensaje());
        resultado.setDialecto(SqlDialect.CASSANDRA);
        return resultado;
    }

    private ResultadoLexer analizarCql(String cql, SqlDialect dialecto) {
        ResultadoLexer resultado = new ResultadoLexer();

        if (cql == null || cql.trim().isEmpty()) {
            resultado.setValido(false);
            resultado.setMensaje("La consulta CQL no puede estar vacia");
            return resultado;
        }

        ErrorCollector errorCollector = new ErrorCollector();
        Lexer lexer = new Lexer(errorCollector);
        java.util.List<Token> tokens = lexer.tokenize(cql);

        resultado.setTokens(tokens);
        resultado.setDialectoDetectado(dialecto);

        java.util.List<SqlDialect> compatibles = new java.util.ArrayList<>();
        compatibles.add(SqlDialect.COMMON);
        compatibles.add(SqlDialect.CASSANDRA);
        resultado.setDialectosCompatibles(compatibles);

        java.util.List<ErrorLexico> erroresLexicos = new java.util.ArrayList<>();
        for (com.umg.model.error.CompilerError err : errorCollector.getErrors()) {
            if ("LEXICAL".equalsIgnoreCase(err.getType())) {
                erroresLexicos.add(new ErrorLexico("E001", err.getMessage(), err.getLine(), err.getColumn(), ""));
            }
        }
        resultado.setErrores(erroresLexicos);

        if (!errorCollector.hasErrors()) {
            resultado.setValido(true);
            resultado.setMensaje("Analisis lexico CQL completado sin errores");
            resultado.setSintaxisBasicaValida(true);

            ErrorCollector parserEc = new ErrorCollector();
            CqlParser parser = new CqlParser(parserEc);
            parser.parse(tokens);

            if (parserEc.hasErrors()) {
                for (com.umg.model.error.CompilerError ce : parserEc.getErrors()) {
                    resultado.addError(new ErrorLexico("E007", ce.getMessage(), ce.getLine(), ce.getColumn(), ""));
                }
                resultado.setValido(false);
                resultado.setSintaxisBasicaValida(false);
                resultado.setMensaje("Error de sintaxis en la sentencia CQL");
            } else {
                resultado.setMensaje("Sentencia CQL valida (lexica y sintacticamente)");
            }
        } else {
            resultado.setValido(false);
            resultado.setSintaxisBasicaValida(false);
            resultado.setMensaje("Error lexico en la sentencia CQL");
        }

        return resultado;
    }
}
