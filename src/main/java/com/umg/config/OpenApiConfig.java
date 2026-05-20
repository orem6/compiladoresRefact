package com.umg.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI compilerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("UMG SQL Compiler API")
                        .description("""
                                API REST para analisis de sentencias SQL.

                                Soporta analisis lexico, sintactico y semantico.
                                Motores SQL soportados inicialmente:
                                - MySQL
                                - PostgreSQL
                                - SQL Server

                                Preparado arquitectonicamente para futura expansion a motores NoSQL.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Equipo Compiladores UMG"))
                        .license(new License()
                                .name("Uso academico")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Servidor local de desarrollo")
                ))
                .externalDocs(new ExternalDocumentation()
                        .description("Documentacion completa de la API")
                        .url("/docs/API_COMPILER.md"));
    }
}
