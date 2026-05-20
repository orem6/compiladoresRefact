package com.umg.api.compiler;

import com.umg.api.compiler.dto.*;
import com.umg.application.compiler.LexicalSyntaxAnalysisService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/compiler")
public class CompilerController {

    private final LexicalSyntaxAnalysisService lexicalSyntaxAnalysisService;

    public CompilerController(LexicalSyntaxAnalysisService lexicalSyntaxAnalysisService) {
        this.lexicalSyntaxAnalysisService = lexicalSyntaxAnalysisService;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "compiler-api",
            "version", "1.0.0"
        ));
    }

    @GetMapping("/dialects")
    public ResponseEntity<Map<String, Object>> dialects() {
        return ResponseEntity.ok(Map.of(
            "supportedDialects", List.of("MYSQL", "POSTGRESQL", "SQL_SERVER"),
            "futureDialects", List.of("MONGODB")
        ));
    }

    @PostMapping("/analyze/lexical-syntax")
    public ResponseEntity<CompilerAnalyzeResponse> analyzeLexicalSyntax(
            @Valid @RequestBody CompilerAnalyzeRequest request) {
        return ResponseEntity.ok(lexicalSyntaxAnalysisService.analyze(request));
    }
}
