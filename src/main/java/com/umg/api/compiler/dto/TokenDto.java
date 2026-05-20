package com.umg.api.compiler.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Token generado por el analizador lexico.")
public class TokenDto {

    @Schema(description = "Tipo del token.", example = "KEYWORD")
    private String type;

    @Schema(description = "Valor lexico del token.", example = "SELECT")
    private String lexeme;

    @Schema(description = "Linea donde aparece el token.", example = "1")
    private int line;

    @Schema(description = "Columna donde aparece el token.", example = "1")
    private int column;

    @Schema(description = "Dialecto asociado al token.", example = "MYSQL")
    private String dialect;

    public TokenDto() {
    }

    public TokenDto(String type, String lexeme, int line, int column, String dialect) {
        this.type = type;
        this.lexeme = lexeme;
        this.line = line;
        this.column = column;
        this.dialect = dialect;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLexeme() {
        return lexeme;
    }

    public void setLexeme(String lexeme) {
        this.lexeme = lexeme;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public String getDialect() {
        return dialect;
    }

    public void setDialect(String dialect) {
        this.dialect = dialect;
    }
}
