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

    public ResultadoSemantico analizar(String query, ConexionBaseDatosConfig config) {
        ResultadoSemantico resultado = new ResultadoSemantico();
        resultado.setDialecto(SqlDialect.MONGODB);

        String method = detectMethod(query);

        if ("aggregate".equals(method)) {
            MongoParseResult parseResult = analizarPipeline(query);
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

                String collection = detectCollection(query);
                if (collection != null && !metadata.existeTabla(config.getBaseDatos(), collection)) {
                    resultado.setValido(false);
                    resultado.setMensaje("La coleccion '" + collection + "' no existe en la base de datos.");
                    resultado.addErrorSemantico(new ErrorSemantico(
                        "SEM_COLLECTION_NOT_FOUND",
                        "La coleccion '" + collection + "' no existe en MongoDB",
                        collection, null, 0, 0));
                    return resultado;
                }

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
        } else {
            if (config == null || !config.esValida()) {
                resultado.setValido(true);
                resultado.setMensaje("Sin configuracion de base de datos. Solo se ejecuto analisis sintactico MongoDB.");
                return resultado;
            }

            MongoClient mongoClient = null;
            try {
                mongoClient = connectionFactory.crearConexion(config);
                DatabaseMetadataService metadata = new MongoDatabaseMetadataService(mongoClient, config.getBaseDatos());

                String collection = detectCollection(query);
                if (collection != null && !metadata.existeTabla(config.getBaseDatos(), collection)) {
                    resultado.setValido(false);
                    resultado.setMensaje("La coleccion '" + collection + "' no existe en la base de datos.");
                    resultado.addErrorSemantico(new ErrorSemantico(
                        "SEM_COLLECTION_NOT_FOUND",
                        "La coleccion '" + collection + "' no existe en MongoDB",
                        collection, null, 0, 0));
                    return resultado;
                }

                resultado.setValido(true);
                resultado.setMensaje("Validacion MongoDB completada.");
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
        }

        return resultado;
    }

    private String detectCollection(String query) {
        if (query == null || query.isBlank()) return null;
        String trimmed = query.trim();
        if (!trimmed.startsWith("db.")) return null;
        int firstDot = trimmed.indexOf('.');
        int secondDot = trimmed.indexOf('.', firstDot + 1);
        if (secondDot == -1) return null;
        return trimmed.substring(firstDot + 1, secondDot).trim();
    }

    private String detectMethod(String query) {
        if (query == null || query.isBlank()) return "";
        String trimmed = query.trim();
        if (!trimmed.startsWith("db.")) return "";
        int firstDot = trimmed.indexOf('.');
        int secondDot = trimmed.indexOf('.', firstDot + 1);
        if (secondDot == -1) return "";
        int paren = trimmed.indexOf('(', secondDot);
        if (paren == -1) return "";
        return trimmed.substring(secondDot + 1, paren).trim();
    }

    private MongoParseResult analizarPipeline(String query) {
        String json = query != null ? query.trim() : "";

        if (json.startsWith("db.")) {
            int parenStart = json.indexOf('(');
            int parenEnd = json.lastIndexOf(')');
            if (parenStart != -1 && parenEnd > parenStart) {
                json = json.substring(parenStart + 1, parenEnd).trim();
            }
        }

        MongoLexer lexer = new MongoLexer(json);
        List<MongoToken> tokens = lexer.tokenize();

        MongoParser parser = new MongoParser(tokens);
        return parser.parse();
    }
}
