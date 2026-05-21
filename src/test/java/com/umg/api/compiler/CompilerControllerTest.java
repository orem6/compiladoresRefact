package com.umg.api.compiler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umg.api.compiler.dto.AnalysisMode;
import com.umg.api.compiler.dto.CompilerAnalyzeRequest;
import com.umg.model.dialect.CompilerDialect;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CompilerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/compiler/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("compiler-api"))
                .andExpect(jsonPath("$.version").value("1.0.0"));
    }

    @Test
    void testDialectsEndpoint() throws Exception {
        mockMvc.perform(get("/api/compiler/dialects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.supportedDialects", hasItems("MYSQL", "POSTGRESQL", "SQL_SERVER", "MONGODB", "CASSANDRA_CQL")))
                .andExpect(jsonPath("$.sqlDialects", hasItems("MYSQL", "POSTGRESQL", "SQL_SERVER")))
                .andExpect(jsonPath("$.noSqlDialects", hasItems("MONGODB", "CASSANDRA_CQL")))
                .andExpect(jsonPath("$.futureDialects").isEmpty());
    }

    @Test
    void testLexicalSyntaxValidSelect() throws Exception {
        CompilerAnalyzeRequest request = new CompilerAnalyzeRequest();
        request.setDialect(CompilerDialect.MYSQL);
        request.setSql("SELECT id, nombre FROM clientes WHERE estado = 1;");
        request.setAnalysisMode(AnalysisMode.LEXICAL_SYNTAX);

        mockMvc.perform(post("/api/compiler/analyze/lexical-syntax")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.executionStatus").value("SUCCESS"))
                .andExpect(jsonPath("$.lexicalResult").exists())
                .andExpect(jsonPath("$.syntaxResult").exists())
                .andExpect(jsonPath("$.semanticResult").doesNotExist())
                .andExpect(jsonPath("$.connectionResult").doesNotExist());
    }

    @Test
    void testLexicalSyntaxEmptySql() throws Exception {
        CompilerAnalyzeRequest request = new CompilerAnalyzeRequest();
        request.setDialect(CompilerDialect.MYSQL);
        request.setSql("");
        request.setAnalysisMode(AnalysisMode.LEXICAL_SYNTAX);

        mockMvc.perform(post("/api/compiler/analyze/lexical-syntax")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLexicalSyntaxModeNotAllowed() throws Exception {
        CompilerAnalyzeRequest request = new CompilerAnalyzeRequest();
        request.setDialect(CompilerDialect.MYSQL);
        request.setSql("SELECT id FROM clientes;");
        request.setAnalysisMode(AnalysisMode.FULL);

        mockMvc.perform(post("/api/compiler/analyze/lexical-syntax")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.executionStatus").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message", containsString("configuracion de base de datos")));
    }

    @Test
    void testLexicalSyntaxWithSyntaxError() throws Exception {
        CompilerAnalyzeRequest request = new CompilerAnalyzeRequest();
        request.setDialect(CompilerDialect.MYSQL);
        request.setSql("SELECT FROM WHERE;");
        request.setAnalysisMode(AnalysisMode.LEXICAL_SYNTAX);

        mockMvc.perform(post("/api/compiler/analyze/lexical-syntax")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.executionStatus").value("SYNTAX_ERROR"))
                .andExpect(jsonPath("$.syntaxResult.errors", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.semanticResult").doesNotExist());
    }

    @Test
    void testLexicalOnlyMode() throws Exception {
        CompilerAnalyzeRequest request = new CompilerAnalyzeRequest();
        request.setDialect(CompilerDialect.MYSQL);
        request.setSql("SELECT id FROM clientes;");
        request.setAnalysisMode(AnalysisMode.LEXICAL_ONLY);

        mockMvc.perform(post("/api/compiler/analyze/lexical-syntax")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lexicalResult").exists())
                .andExpect(jsonPath("$.syntaxResult").doesNotExist())
                .andExpect(jsonPath("$.semanticResult").doesNotExist())
                .andExpect(jsonPath("$.connectionResult").doesNotExist());
    }

    @Test
    void testLexicalSyntaxWithComments() throws Exception {
        CompilerAnalyzeRequest request = new CompilerAnalyzeRequest();
        request.setDialect(CompilerDialect.MYSQL);
        request.setSql("SELECT id -- solo id\nFROM clientes;");
        request.setAnalysisMode(AnalysisMode.LEXICAL_SYNTAX);

        mockMvc.perform(post("/api/compiler/analyze/lexical-syntax")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.executionStatus").value("SUCCESS"))
                .andExpect(jsonPath("$.lexicalResult.tokens", hasSize(greaterThan(0))));
    }

    @Test
    void testLexicalSyntaxResponseStructure() throws Exception {
        CompilerAnalyzeRequest request = new CompilerAnalyzeRequest();
        request.setRequestId("test-uuid-123");
        request.setDialect(CompilerDialect.POSTGRESQL);
        request.setSql("SELECT * FROM usuarios;");
        request.setAnalysisMode(AnalysisMode.LEXICAL_SYNTAX);

        mockMvc.perform(post("/api/compiler/analyze/lexical-syntax")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").value("test-uuid-123"))
                .andExpect(jsonPath("$.dialect").value("POSTGRESQL"))
                .andExpect(jsonPath("$.analysisMode").value("LEXICAL_SYNTAX"))
                .andExpect(jsonPath("$.summary.tokenCount").isNumber())
                .andExpect(jsonPath("$.summary.lexicalErrorCount").isNumber())
                .andExpect(jsonPath("$.summary.syntaxErrorCount").isNumber())
                .andExpect(jsonPath("$.summary.semanticErrorCount").value(0))
                .andExpect(jsonPath("$.summary.analyzedAt").exists())
                .andExpect(jsonPath("$.console", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.lexicalResult.tokens[0].type").exists())
                .andExpect(jsonPath("$.lexicalResult.tokens[0].lexeme").exists())
                .andExpect(jsonPath("$.lexicalResult.tokens[0].line").isNumber())
                .andExpect(jsonPath("$.lexicalResult.tokens[0].column").isNumber());
    }

    @Test
    void testDialectSqlServer() throws Exception {
        CompilerAnalyzeRequest request = new CompilerAnalyzeRequest();
        request.setDialect(CompilerDialect.SQL_SERVER);
        request.setSql("SELECT GETDATE();");
        request.setAnalysisMode(AnalysisMode.LEXICAL_SYNTAX);

        mockMvc.perform(post("/api/compiler/analyze/lexical-syntax")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dialect").value("SQL_SERVER"));
    }

    @Test
    void testFullEndpointWithConfig() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("dialect", "MYSQL");
        request.put("sql", "SELECT id FROM usuarios;");
        request.put("analysisMode", "FULL");

        Map<String, Object> dbConfig = new HashMap<>();
        dbConfig.put("dialect", "MYSQL");
        dbConfig.put("host", "localhost");
        dbConfig.put("port", 3306);
        dbConfig.put("database", "testdb");
        dbConfig.put("username", "root");
        dbConfig.put("password", "");
        request.put("connectionConfig", dbConfig);

        mockMvc.perform(post("/api/compiler/analyze/full")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analysisMode").value("FULL"))
                .andExpect(jsonPath("$.lexicalResult").exists())
                .andExpect(jsonPath("$.syntaxResult").exists())
                .andExpect(jsonPath("$.semanticResult").exists());
    }

    @Test
    void testFullEndpointWithLexicalModeOverride() throws Exception {
        CompilerAnalyzeRequest request = new CompilerAnalyzeRequest();
        request.setDialect(CompilerDialect.MYSQL);
        request.setSql("SELECT id FROM usuarios;");
        request.setAnalysisMode(AnalysisMode.LEXICAL_ONLY);

        mockMvc.perform(post("/api/compiler/analyze/full")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analysisMode").value("FULL"));
    }

    @Test
    void testConnectionTestInvalidConfig() throws Exception {
        Map<String, Object> config = new java.util.HashMap<>();
        config.put("dialect", "MYSQL");

        mockMvc.perform(post("/api/compiler/connection/test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(config)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("INVALID_CONFIG"));
    }

    @Test
    void testConnectionTestMissingDialect() throws Exception {
        Map<String, Object> config = new java.util.HashMap<>();
        config.put("host", "localhost");

        mockMvc.perform(post("/api/compiler/connection/test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(config)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("INVALID_CONFIG"));
    }

    @Test
    void testSemanticOnlyModeRequiresConfig() throws Exception {
        CompilerAnalyzeRequest request = new CompilerAnalyzeRequest();
        request.setDialect(CompilerDialect.MYSQL);
        request.setSql("SELECT id FROM usuarios;");
        request.setAnalysisMode(AnalysisMode.SEMANTIC_ONLY);

        mockMvc.perform(post("/api/compiler/analyze/lexical-syntax")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.executionStatus").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message", containsString("configuracion de base de datos")));
    }

    // ---- NoSQL endpoint tests ----

    @Test
    void testMongoDbLexicalSyntax() throws Exception {
        CompilerAnalyzeRequest request = new CompilerAnalyzeRequest();
        request.setDialect(CompilerDialect.MONGODB);
        request.setSql("db.users.find({ status: \"active\" })");
        request.setAnalysisMode(AnalysisMode.LEXICAL_SYNTAX);

        mockMvc.perform(post("/api/compiler/analyze/lexical-syntax")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.dialect").value("MONGODB"))
                .andExpect(jsonPath("$.executionStatus").value("SUCCESS"))
                .andExpect(jsonPath("$.lexicalResult").exists())
                .andExpect(jsonPath("$.syntaxResult").exists());
    }

    @Test
    void testCassandraCqlLexicalSyntax() throws Exception {
        CompilerAnalyzeRequest request = new CompilerAnalyzeRequest();
        request.setDialect(CompilerDialect.CASSANDRA_CQL);
        request.setSql("SELECT * FROM users;");
        request.setAnalysisMode(AnalysisMode.LEXICAL_SYNTAX);

        mockMvc.perform(post("/api/compiler/analyze/lexical-syntax")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.dialect").value("CASSANDRA_CQL"))
                .andExpect(jsonPath("$.executionStatus").value("SUCCESS"))
                .andExpect(jsonPath("$.lexicalResult").exists())
                .andExpect(jsonPath("$.syntaxResult").exists());
    }

    @Test
    void testMongoDbFullModeReturnsPending() throws Exception {
        CompilerAnalyzeRequest request = new CompilerAnalyzeRequest();
        request.setDialect(CompilerDialect.MONGODB);
        request.setSql("db.users.find({})");
        request.setAnalysisMode(AnalysisMode.FULL);

        mockMvc.perform(post("/api/compiler/analyze/full")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.executionStatus").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message", containsString("pendiente")));
    }

    @Test
    void testCassandraSemanticOnlyReturnsPending() throws Exception {
        CompilerAnalyzeRequest request = new CompilerAnalyzeRequest();
        request.setDialect(CompilerDialect.CASSANDRA_CQL);
        request.setSql("SELECT * FROM users;");
        request.setAnalysisMode(AnalysisMode.SEMANTIC_ONLY);

        mockMvc.perform(post("/api/compiler/analyze/lexical-syntax")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.executionStatus").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message", containsString("pendiente")));
    }

    @Test
    void testMongoDbLexicalSyntaxWithError() throws Exception {
        CompilerAnalyzeRequest request = new CompilerAnalyzeRequest();
        request.setDialect(CompilerDialect.MONGODB);
        request.setSql("db.users.find({ status: })");
        request.setAnalysisMode(AnalysisMode.LEXICAL_SYNTAX);

        mockMvc.perform(post("/api/compiler/analyze/lexical-syntax")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.executionStatus").value("SYNTAX_ERROR"));
    }

    @Test
    void testMongoDbInvalidSyntax() throws Exception {
        CompilerAnalyzeRequest request = new CompilerAnalyzeRequest();
        request.setDialect(CompilerDialect.MONGODB);
        request.setSql("users.watch({})");
        request.setAnalysisMode(AnalysisMode.LEXICAL_SYNTAX);

        mockMvc.perform(post("/api/compiler/analyze/lexical-syntax")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.executionStatus").value("SYNTAX_ERROR"));
    }
}
