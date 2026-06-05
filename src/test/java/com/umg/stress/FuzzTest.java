package com.umg.stress;

import com.umg.model.error.ErrorCollector;
import com.umg.model.lexer.*;
import com.umg.model.parser.Parser;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de estres/fuzzing para asegurar que el compilador no truena
 * con entradas extremas o malformadas.
 */
class FuzzTest {

    private void assertNoCrash(String sql) {
        try {
            ErrorCollector ec = new ErrorCollector();
            Lexer lexer = new Lexer(ec);
            List<Token> tokens = lexer.tokenize(sql);

            ErrorCollector ec2 = new ErrorCollector();
            Parser parser = new Parser(ec2);
            parser.parse(tokens);
        } catch (Exception e) {
            System.err.println("CRASH con input: [" + sql + "]");
            System.err.println("  Excepcion: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
            fail("El compilador crasheo con input: " + e.getMessage());
        }
    }

    // =========================================================
    // EMPTY/WHITESPACE INPUTS
    // =========================================================

    @Test
    void testEmptyString() { assertNoCrash(""); }

    @Test
    void testWhitespaceOnly() { assertNoCrash("   "); }

    @Test
    void testNewlinesOnly() { assertNoCrash("\n\n\n"); }

    @Test
    void testTabsAndSpaces() { assertNoCrash("\t\t  \t"); }

    @Test
    void testMixedWhitespace() { assertNoCrash(" \n \t \r \n "); }

    @Test
    void testNullInput() {
        try {
            ErrorCollector ec = new ErrorCollector();
            Lexer lexer = new Lexer(ec);
            List<Token> tokens = lexer.tokenize(null);
            // Should not crash with null
        } catch (Exception e) {
            fail("Lexer crasheo con null input: " + e.getMessage());
        }
    }

    // =========================================================
    // SINGLE CHARACTER / MINIMAL INPUTS
    // =========================================================

    @Test
    void testSingleSemicolon() { assertNoCrash(";"); }

    @Test
    void testSingleComma() { assertNoCrash(","); }

    @Test
    void testSingleDot() { assertNoCrash("."); }

    @Test
    void testSingleAsterisk() { assertNoCrash("*"); }

    @Test
    void testSingleParenthesis() { assertNoCrash("("); }

    @Test
    void testSingleClosingParen() { assertNoCrash(")"); }

    @Test
    void testSingleQuote() { assertNoCrash("'"); }

    @Test
    void testSingleBacktick() { assertNoCrash("`"); }

    @Test
    void testSingleBracket() { assertNoCrash("["); }

    @Test
    void testSingleDoubleQuote() { assertNoCrash("\""); }

    // =========================================================
    // SPECIAL CHARACTERS
    // =========================================================

    @Test
    void testAtSymbol() { assertNoCrash("@"); }

    @Test
    void testHashSymbol() { assertNoCrash("#"); }

    @Test
    void testDollarSign() { assertNoCrash("$"); }

    @Test
    void testPercentSign() { assertNoCrash("%"); }

    @Test
    void testCaretSymbol() { assertNoCrash("^"); }

    @Test
    void testAmpersand() { assertNoCrash("&"); }

    @Test
    void testTilde() { assertNoCrash("~"); }

    @Test
    void testMultipleSpecialChars() { assertNoCrash("@#$%^&~"); }

    // =========================================================
    // UNTERMINATED STRINGS AND COMMENTS
    // =========================================================

    @Test
    void testUnterminatedString() { assertNoCrash("SELECT 'hello"); }

    @Test
    void testUnterminatedStringWithNewline() { assertNoCrash("SELECT 'hello\nFROM t"); }

    @Test
    void testUnterminatedBlockComment() { assertNoCrash("SELECT /* hello"); }

    @Test
    void testUnterminatedBacktick() { assertNoCrash("SELECT `hello"); }

    @Test
    void testUnterminatedBracket() { assertNoCrash("SELECT [hello"); }

    @Test
    void testUnterminatedDoubleQuote() { assertNoCrash("SELECT \"hello"); }

    @Test
    void testNestedUnterminated() { assertNoCrash("SELECT '/*' FROM t; -- /*"); }

    // =========================================================
    // EXTREMELY LONG INPUTS
    // =========================================================

    @Test
    void testVeryLongIdentifier() {
        StringBuilder sb = new StringBuilder("SELECT ");
        sb.append("a".repeat(5000));
        sb.append(" FROM t;");
        assertNoCrash(sb.toString());
    }

    @Test
    void testVeryLongStringLiteral() {
        StringBuilder sb = new StringBuilder("SELECT '");
        sb.append("x".repeat(5000));
        sb.append("' FROM t;");
        assertNoCrash(sb.toString());
    }

    @Test
    void testDeeplyNestedParentheses() {
        StringBuilder sb = new StringBuilder("SELECT * FROM t WHERE ((((((");
        sb.append("a".repeat(500));
        sb.append(")))");
        sb.append(")".repeat(500));
        sb.append(";");
        assertNoCrash(sb.toString());
    }

    @Test
    void testManyAndConditions() {
        StringBuilder sb = new StringBuilder("SELECT * FROM t WHERE 1=1");
        for (int i = 0; i < 500; i++) {
            sb.append(" AND x=y");
        }
        sb.append(";");
        assertNoCrash(sb.toString());
    }

    @Test
    void testVeryLongBlockComment() {
        StringBuilder sb = new StringBuilder("SELECT /* ");
        sb.append("a".repeat(10000));
        sb.append(" */ * FROM t;");
        assertNoCrash(sb.toString());
    }

    @Test
    void testLongInput() {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");
        for (int i = 0; i < 1000; i++) {
            sb.append("col").append(i).append(", ");
        }
        sb.append("col1000 FROM large_table;");
        assertNoCrash(sb.toString());
    }

    // =========================================================
    // UNICODE / NON-ASCII INPUTS
    // =========================================================

    @Test
    void testUnicodeGreek() { assertNoCrash("SELECT α, β FROM t;"); }

    @Test
    void testUnicodeChinese() { assertNoCrash("SELECT 名字 FROM 用户;"); }

    @Test
    void testUnicodeJapanese() { assertNoCrash("SELECT 名前 FROM テーブル;"); }

    @Test
    void testUnicodeAccentedChars() { assertNoCrash("SELECT niños, día FROM t;"); }

    @Test
    void testUnicodeEmoji() { assertNoCrash("SELECT 😀 FROM t;"); }

    // =========================================================
    // INCOMPLETE SQL CONSTRUCTS
    // =========================================================

    @Test
    void testSelectOnly() { assertNoCrash("SELECT"); }

    @Test
    void testSelectFromOnly() { assertNoCrash("SELECT FROM"); }

    @Test
    void testSelectFromWhere() { assertNoCrash("SELECT FROM WHERE"); }

    @Test
    void testWhereOnly() { assertNoCrash("WHERE"); }

    @Test
    void testFromOnly() { assertNoCrash("FROM"); }

    @Test
    void testJoinWithoutOn() { assertNoCrash("SELECT * FROM t1 JOIN t2;"); }

    @Test
    void testOnWithoutCondition() { assertNoCrash("SELECT * FROM t1 JOIN t2 ON;"); }

    @Test
    void testGroupByWithoutBy() { assertNoCrash("SELECT * FROM t GROUP;"); }

    @Test
    void testOrderByWithoutBy() { assertNoCrash("SELECT * FROM t ORDER;"); }

    @Test
    void testLimitWithoutNumber() { assertNoCrash("SELECT * FROM t LIMIT;"); }

    @Test
    void testValuesWithoutParens() { assertNoCrash("INSERT INTO t VALUES;"); }

    @Test
    void testSetWithoutAssignments() { assertNoCrash("UPDATE t SET;"); }

    // =========================================================
    // MALFORMED NUMBERS
    // =========================================================

    @Test
    void testExponentOnly() { assertNoCrash("SELECT 1e;"); }

    @Test
    void testExponentWithSignOnly() { assertNoCrash("SELECT 1e+;"); }

    @Test
    void testMultipleDots() { assertNoCrash("SELECT 1.2.3 FROM t;"); }

    @Test
    void testTrailingDot() { assertNoCrash("SELECT 1. FROM t;"); }

    @Test
    void testLeadingDot() { assertNoCrash("SELECT .1 FROM t;"); }

    // =========================================================
    // MALFORMED OPERATORS
    // =========================================================

    @Test
    void testColonEquals() { assertNoCrash("SELECT := 'test';"); }

    @Test
    void testDoubleColon() { assertNoCrash("SELECT 1::text;"); }

    @Test
    void testPipeConcat() { assertNoCrash("SELECT 'a' || 'b';"); }

    @Test
    void testOrOperator() { assertNoCrash("SELECT 1 || 0 FROM t;"); }

    // =========================================================
    // MIXED COMMENTS AND CODE
    // =========================================================

    @Test
    void testCommentBetweenKeywords() { assertNoCrash("SELECT/*comment*/*FROM t;"); }

    @Test
    void testLineCommentBetweenColumns() { assertNoCrash("SELECT id -- comment\n, nombre FROM t;"); }

    @Test
    void testMultipleLineComments() { assertNoCrash("SELECT *\n-- line 1\n-- line 2\nFROM t;"); }

    @Test
    void testNestedCommentAttempt() { assertNoCrash("SELECT /* outer /* inner */ */ FROM t;"); }

    // =========================================================
    // MULTIPLE STATEMENTS
    // =========================================================

    @Test
    void testTwoSelects() { assertNoCrash("SELECT * FROM t1; SELECT * FROM t2;"); }

    @Test
    void testMultipleSemicolons() { assertNoCrash("SELECT 1;;;"); }

    @Test
    void testSemicolonInMiddle() { assertNoCrash("SELECT *; FROM t;"); }

    // =========================================================
    // VERY LARGE NUMBERS
    // =========================================================

    @Test
    void testVeryLargeInteger() { assertNoCrash("SELECT 999999999999999999999999999999 FROM t;"); }

    @Test
    void testVeryLargeDecimal() { assertNoCrash("SELECT 0.0000000000000000000000001 FROM t;"); }

    @Test
    void testNegativeNumber() { assertNoCrash("SELECT -1 FROM t;"); }

    // =========================================================
    // MONGODB / NOSQL IN SQL PARSER (should not crash)
    // =========================================================

    @Test
    void testMongoDbSyntax() { assertNoCrash("db.collection.find({name: 'test'});"); }

    @Test
    void testMongoDbAggregation() { assertNoCrash("db.collection.aggregate([{$match: {status: 'A'}}]);"); }

    @Test
    void testCassandraCqlSyntax() { assertNoCrash("SELECT * FROM users WHERE id = 1 ALLOW FILTERING;"); }

    // =========================================================
    // STRING ESCAPE VARIANTS
    // =========================================================

    @Test
    void testStringWithEscapedQuote() { assertNoCrash("SELECT 'O''Brien' FROM t;"); }

    @Test
    void testStringWithNewline() { assertNoCrash("SELECT 'hello\nworld' FROM t;"); }

    @Test
    void testStringWithTabs() { assertNoCrash("SELECT 'hello\tworld' FROM t;"); }

    @Test
    void testStringWithBackslash() { assertNoCrash("SELECT 'hello\\world' FROM t;"); }

    // =========================================================
    // KEYWORDS AS IDENTIFIERS
    // =========================================================

    @Test
    void testKeywordAsTableName() { assertNoCrash("SELECT * FROM \"order\";"); }

    @Test
    void testKeywordAsColumnName() { assertNoCrash("SELECT \"select\", \"from\" FROM t;"); }

    // =========================================================
    // BINARY/HEX LITERALS
    // =========================================================

    @Test
    void testHexLiteral() { assertNoCrash("SELECT x'FF' FROM t;"); }

    @Test
    void testBinaryLiteral() { assertNoCrash("SELECT b'1010' FROM t;"); }

    // =========================================================
    // INVALID IDENTIFIERS
    // =========================================================

    @Test
    void testNumberAsIdentifier() { assertNoCrash("SELECT 123 FROM 456;"); }

    @Test
    void testAllOperatorsAsIdentifiers() { assertNoCrash("SELECT =, <, >, <=, >=, <>, !=, +, -, *, /, % FROM t;"); }

    // =========================================================
    // STRESS WITH RECURSIVE CTE (not supported but should not crash)
    // =========================================================

    @Test
    void testRecursiveCte() { assertNoCrash("WITH RECURSIVE cte AS (SELECT 1 UNION ALL SELECT n+1 FROM cte WHERE n < 10) SELECT * FROM cte;"); }
}
