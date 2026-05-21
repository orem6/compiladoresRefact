package com.umg.model.semantic;

import com.mongodb.client.MongoClient;
import com.umg.model.dialect.SqlDialect;
import com.umg.model.mongo.*;
import com.umg.model.semantic.config.ConexionBaseDatosConfig;
import com.umg.model.semantic.metadata.DatabaseMetadataService;
import com.umg.model.semantic.metadata.MongoConnectionFactory;
import com.umg.model.semantic.metadata.MongoDatabaseMetadataService;
import com.umg.model.semantic.result.ErrorSemantico;
import com.umg.model.semantic.result.ResultadoSemantico;
import com.umg.model.semantic.validator.SemanticValidatorMongo;

import java.util.List;

public class AnalizadorMongo {

    private final MongoConnectionFactory connectionFactory;
    private final SemanticValidatorMongo validator;

    public AnalizadorMongo() {
        this.connectionFactory = new MongoConnectionFactory();
        this.validator = new SemanticValidatorMongo();
    }

    public ResultadoSemantico analizar(String pipelineJson, ConexionBaseDatosConfig config) {
        ResultadoSemantico resultado = new ResultadoSemantico();
        resultado.setDialecto(SqlDialect.MONGODB);

        MongoParseResult parseResult = analizarPipeline(pipelineJson);

        if (!parseResult.isValid()) {
            resultado.setValido(false);
            resultado.setMensaje("El pipeline MongoDB contiene errores sintacticos.");
            return resultado;
        }

        if (config == null || !config.esValida()) {
            resultado.setValido(true);
            resultado.setMensaje("Sin configuracion de base de datos. Solo se ejecuto analisis sintactico MongoDB.");
            return resultado;
        }

        MongoClient mongoClient = null;
        try {
            mongoClient = connectionFactory.crearConexion(config);
            DatabaseMetadataService metadata = new MongoDatabaseMetadataService(mongoClient, config.getBaseDatos());

            List<String> stages = parseResult.getDetectedStages();
            ResultadoSemantico validacion = validator.validar(stages, metadata, config.getBaseDatos());

            resultado.setValido(validacion.isValido());
            resultado.setMensaje(validacion.getMensaje());
            resultado.setErroresSemanticos(validacion.getErroresSemanticos());
            resultado.setAdvertencias(validacion.getAdvertencias());

        } catch (Exception e) {
            resultado.setValido(false);
            resultado.setMensaje("Error de conexion a MongoDB");
            resultado.addErrorSemantico(new ErrorSemantico(
                "SEM_CONNECTION_ERROR",
                "No fue posible conectar con MongoDB: " + e.getMessage(),
                null, null, 0, 0));
        } finally {
            if (mongoClient != null) {
                mongoClient.close();
            }
        }

        return resultado;
    }

    private MongoParseResult analizarPipeline(String pipelineJson) {
        MongoLexer lexer = new MongoLexer(pipelineJson != null ? pipelineJson : "");
        List<MongoToken> tokens = lexer.tokenize();

        MongoParser parser = new MongoParser(tokens);
        return parser.parse();
    }
}
