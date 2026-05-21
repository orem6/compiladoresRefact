package com.umg.api.compiler;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;

@SpringBootTest
@AutoConfigureMockMvc
class OpenApiDocumentationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testOpenApiJsonReturns200() throws Exception {
        mockMvc.perform(get("/api/compiler/openapi"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.openapi").exists())
                .andExpect(jsonPath("$.info.title").value("UMG SQL Compiler API"))
                .andExpect(jsonPath("$.paths").exists());
    }

    @Test
    void testSwaggerUiRedirects() throws Exception {
        mockMvc.perform(get("/api/compiler/docs"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/api/compiler/swagger-ui/**"));
    }

    @Test
    void testOpenApiContainsHealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/compiler/openapi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths.['/api/compiler/health']").exists());
    }

    @Test
    void testOpenApiContainsDialectsEndpoint() throws Exception {
        mockMvc.perform(get("/api/compiler/openapi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths.['/api/compiler/dialects']").exists());
    }

    @Test
    void testOpenApiContainsLexicalSyntaxEndpoint() throws Exception {
        mockMvc.perform(get("/api/compiler/openapi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths.['/api/compiler/analyze/lexical-syntax']").exists());
    }

    @Test
    void testOpenApiContainsFullEndpoint() throws Exception {
        mockMvc.perform(get("/api/compiler/openapi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths.['/api/compiler/analyze/full']").exists());
    }

    @Test
    void testOpenApiContainsConnectionTestEndpoint() throws Exception {
        mockMvc.perform(get("/api/compiler/openapi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths.['/api/compiler/connection/test']").exists());
    }

    @Test
    void testPasswordFieldIsWriteOnly() throws Exception {
        String json = mockMvc.perform(get("/api/compiler/openapi"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Object writeOnly = JsonPath.read(json,
            "$.components.schemas.ConnectionConfigDto.properties.password.writeOnly");
        assertNotNull(writeOnly, "password debe tener writeOnly en su schema");
        assertTrue((Boolean) writeOnly, "password.writeOnly debe ser true");
    }

    @Test
    void testOpenApiHasServerUrl() throws Exception {
        mockMvc.perform(get("/api/compiler/openapi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.servers[0].url").value("http://localhost:8080"));
    }

    @Test
    void testOpenApiHasTagCompilerApi() throws Exception {
        mockMvc.perform(get("/api/compiler/openapi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tags[0].name").value("Compiler API"));
    }

    @Test
    void testOpenApiIncludesNoSqlDialects() throws Exception {
        mockMvc.perform(get("/api/compiler/openapi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.schemas.CompilerAnalyzeRequest.properties.dialect.enum", hasItems("MONGODB", "CASSANDRA_CQL")));
    }
}
