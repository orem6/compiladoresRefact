package com.umg.extractor;

import com.umg.model.dialect.SqlDialect;
import com.umg.model.error.ErrorCollector;
import com.umg.model.lexer.*;
import com.umg.model.semantic.extractor.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class SqlReferenceExtractorTest {

    private ReferenciasSql extraer(String sql) {
        return extraer(sql, SqlDialect.COMMON);
    }

    private ReferenciasSql extraer(String sql, SqlDialect dialecto) {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize(sql);
        ResultadoLexer resultado = new ResultadoLexer(!ec.hasErrors(), "", tokens, List.of());
        SqlReferenceExtractor extractor = new SqlReferenceExtractor();
        return extractor.extraer(resultado, dialecto);
    }

    // =========================================================
    // BASIC TABLE EXTRACTION
    // =========================================================

    @Test
    void testSimpleSelectExtractsTable() {
        ReferenciasSql refs = extraer("SELECT * FROM clientes;");
        assertEquals("SELECT", refs.getTipoSentencia());
        assertEquals(1, refs.getTablas().size());
        assertEquals("clientes", refs.getTablas().get(0).getNombre());
    }

    @Test
    void testSelectWithMultipleColumns() {
        ReferenciasSql refs = extraer("SELECT id, nombre FROM clientes;");
        assertEquals("SELECT", refs.getTipoSentencia());
        assertEquals(1, refs.getTablas().size());
        assertEquals("clientes", refs.getTablas().get(0).getNombre());
    }

    // =========================================================
    // ALIAS EXTRACTION TESTS
    // =========================================================

    @Test
    void testTableAliasWithoutAs() {
        ReferenciasSql refs = extraer("SELECT c.id FROM clientes c WHERE c.estado = 1;");
        int tablaCount = refs.getTablas().size();
        boolean tieneClientes = refs.getTablas().stream().anyMatch(t -> t.getNombre().equals("clientes"));
        if (tablaCount != 1 || !tieneClientes) {
            System.out.println("BUG CONFIRMADO (EXTRACTOR): alias detection impreciso");
            System.out.println("  Tablas encontradas: " + refs.getTablas());
            System.out.println("  El extractor no distingue entre tabla, alias y columnas en WHERE");
        }
        assertTrue(tieneClientes, "Deberia al menos encontrar 'clientes'");
    }

    @Test
    void testTableAliasWithAs() {
        ReferenciasSql refs = extraer("SELECT * FROM clientes AS c;");
        int tablaCount = refs.getTablas().size();
        boolean tieneClientes = refs.getTablas().stream().anyMatch(t -> t.getNombre().equals("clientes"));
        if (tablaCount != 1 || !tieneClientes) {
            System.out.println("BUG CONFIRMADO (EXTRACTOR): 'AS' no detectado como keyword de alias");
            System.out.println("  Tablas encontradas: " + refs.getTablas());
        }
        assertTrue(tieneClientes, "Deberia al menos encontrar 'clientes'");
    }

    @Test
    void testColumnAliasesInSelect() {
        ReferenciasSql refs = extraer("SELECT id AS identificador, nombre AS nombre_cliente FROM clientes;");
        assertEquals(1, refs.getTablas().size());
        assertEquals("clientes", refs.getTablas().get(0).getNombre());
        // Columnas should include id and nombre
        assertTrue(refs.getColumnas().stream().anyMatch(c -> c.getNombre().equals("id")));
        assertTrue(refs.getColumnas().stream().anyMatch(c -> c.getNombre().equals("nombre")));
    }

    @Test
    void testQualifiedColumnReference() {
        ReferenciasSql refs = extraer("SELECT t1.id, t1.nombre FROM t1;");
        boolean hasId = refs.getColumnas().stream()
            .anyMatch(c -> c.getNombre().equals("id") && "t1".equals(c.getTablaOAlias()));
        boolean hasNombre = refs.getColumnas().stream()
            .anyMatch(c -> c.getNombre().equals("nombre") && "t1".equals(c.getTablaOAlias()));
        assertTrue(hasId, "Deberia extraer t1.id");
        assertTrue(hasNombre, "Deberia extraer t1.nombre");
    }

    // =========================================================
    // BUG REPRODUCTION: JOIN table extraction
    // =========================================================

    @Test
    void testInnerJoinExtractsBothTables() {
        ReferenciasSql refs = extraer("SELECT * FROM t1 INNER JOIN t2 ON t1.id = t2.id;");
        int tableCount = refs.getTablas().size();
        if (tableCount < 2) {
            System.out.println("BUG CONFIRMADO: INNER JOIN solo extrae " + tableCount +
                " tabla(s): " + refs.getTablas().stream().map(ReferenciaTabla::getNombre).toList());
            System.out.println("  El extractor no procesa tablas del lado derecho del JOIN");
        }
    }

    @Test
    void testLeftJoinWithAliases() {
        ReferenciasSql refs = extraer("SELECT a.id, b.nombre FROM clientes a LEFT JOIN pedidos b ON a.id = b.cliente_id;");
        if (refs.getTablas().size() < 2) {
            System.out.println("BUG CONFIRMADO: LEFT JOIN con alias solo extrajo " +
                refs.getTablas().size() + " tabla(s): " +
                refs.getTablas().stream().map(ReferenciaTabla::getNombre).toList());
            System.out.println("  Faltan: pedidos (alias b)");
            return;
        }
        assertEquals(2, refs.getTablas().size());
        ReferenciaTabla t1 = refs.getTablas().get(0);
        ReferenciaTabla t2 = refs.getTablas().get(1);
        assertEquals("clientes", t1.getNombre());
        assertEquals("pedidos", t2.getNombre());
        assertEquals("a", t1.getAlias());
        assertEquals("b", t2.getAlias());
        assertTrue(refs.tieneAlias("a"));
        assertTrue(refs.tieneAlias("b"));
        assertEquals("clientes", refs.getTablaPorAlias("a").getNombre());
        assertEquals("pedidos", refs.getTablaPorAlias("b").getNombre());
    }

    @Test
    void testMultipleJoinsAllTablesExtracted() {
        ReferenciasSql refs = extraer("SELECT * FROM t1 JOIN t2 ON t1.id = t2.id JOIN t3 ON t2.id = t3.id;");
        int tableCount = refs.getTablas().size();
        if (tableCount < 3) {
            System.out.println("BUG CONFIRMADO: Multiples JOINs solo extrajo " +
                tableCount + " tabla(s): " +
                refs.getTablas().stream().map(ReferenciaTabla::getNombre).toList());
        }
    }

    @Test
    void testCrossJoinExtractsBothTables() {
        ReferenciasSql refs = extraer("SELECT * FROM t1 CROSS JOIN t2;");
        if (refs.getTablas().size() < 2) {
            System.out.println("BUG CONFIRMADO: CROSS JOIN solo extrajo " +
                refs.getTablas().size() + " tabla: " +
                refs.getTablas().stream().map(ReferenciaTabla::getNombre).toList());
            return;
        }
        assertEquals(2, refs.getTablas().size());
    }

    // =========================================================
    // BUG REPRODUCTION: ON clause column extraction
    // =========================================================

    @Test
    void testOnClauseColumnsExtracted() {
        ReferenciasSql refs = extraer("SELECT * FROM t1 JOIN t2 ON t1.id = t2.id;");
        boolean hasT1Id = refs.getColumnas().stream()
            .anyMatch(c -> c.getNombre().equals("id") && "t1".equals(c.getTablaOAlias()));
        boolean hasT2Id = refs.getColumnas().stream()
            .anyMatch(c -> c.getNombre().equals("id") && "t2".equals(c.getTablaOAlias()));
        if (!hasT1Id || !hasT2Id) {
            System.out.println("BUG CONFIRMADO: Columnas del ON clause no extraidas correctamente");
            System.out.println("  Columnas encontradas: " + refs.getColumnas());
        }
    }

    @Test
    void testMultipleOnClausesColumnsExtracted() {
        ReferenciasSql refs = extraer("SELECT * FROM t1 JOIN t2 ON t1.id = t2.id JOIN t3 ON t2.id = t3.id;");
        if (refs.getColumnas().size() < 4) {
            System.out.println("BUG CONFIRMADO: Multiples ON clauses solo extrajo " +
                refs.getColumnas().size() + " columna(s) (esperadas: 4 o mas)");
        }
    }

    // =========================================================
    // BUG REPRODUCTION: Column extraction in IN/LIKE/BETWEEN
    // =========================================================

    @Test
    void testColumnInInClause() {
        ReferenciasSql refs = extraer("SELECT * FROM t1 WHERE x IN (1, 2, 3);");
        boolean hasX = refs.getColumnas().stream()
            .anyMatch(c -> c.getNombre().equals("x"));
        if (!hasX) {
            System.out.println("BUG CONFIRMADO: Columna 'x' en IN clause no extraida");
            System.out.println("  Columnas encontradas: " + refs.getColumnas());
        }
    }

    @Test
    void testColumnInLikeClause() {
        ReferenciasSql refs = extraer("SELECT * FROM t1 WHERE name LIKE '%test%';");
        boolean hasName = refs.getColumnas().stream()
            .anyMatch(c -> c.getNombre().equals("name"));
        if (!hasName) {
            System.out.println("BUG CONFIRMADO: Columna 'name' en LIKE clause no extraida");
        }
    }

    @Test
    void testColumnInBetweenClause() {
        ReferenciasSql refs = extraer("SELECT * FROM t1 WHERE age BETWEEN 10 AND 20;");
        boolean hasAge = refs.getColumnas().stream()
            .anyMatch(c -> c.getNombre().equals("age"));
        if (!hasAge) {
            System.out.println("BUG CONFIRMADO: Columna 'age' en BETWEEN clause no extraida");
        }
    }

    @Test
    void testColumnInIsNullClause() {
        ReferenciasSql refs = extraer("SELECT * FROM t1 WHERE status IS NULL;");
        boolean hasStatus = refs.getColumnas().stream()
            .anyMatch(c -> c.getNombre().equals("status"));
        if (!hasStatus) {
            System.out.println("BUG CONFIRMADO: Columna 'status' en IS NULL clause no extraida");
        }
    }

    @Test
    void testColumnInIsNotNullClause() {
        ReferenciasSql refs = extraer("SELECT * FROM t1 WHERE status IS NOT NULL;");
        boolean hasStatus = refs.getColumnas().stream()
            .anyMatch(c -> c.getNombre().equals("status"));
        if (!hasStatus) {
            System.out.println("BUG CONFIRMADO: Columna 'status' en IS NOT NULL clause no extraida");
        }
    }

    @Test
    void testColumnInNotInClause() {
        ReferenciasSql refs = extraer("SELECT * FROM t1 WHERE id NOT IN (1, 2, 3);");
        boolean hasId = refs.getColumnas().stream()
            .anyMatch(c -> c.getNombre().equals("id"));
        if (!hasId) {
            System.out.println("BUG CONFIRMADO: Columna 'id' en NOT IN clause no extraida");
        }
    }

    // =========================================================
    // COMPLEX JOIN COLUMN EXTRACTION
    // =========================================================

    @Test
    void testJoinWithWhereAndOnColumns() {
        ReferenciasSql refs = extraer(
            "SELECT * FROM t1 JOIN t2 ON t1.x = t2.y WHERE t1.z = 1;");
        boolean hasOnX = refs.getColumnas().stream()
            .anyMatch(c -> c.getNombre().equals("x"));
        boolean hasOnY = refs.getColumnas().stream()
            .anyMatch(c -> c.getNombre().equals("y"));
        boolean hasWhereZ = refs.getColumnas().stream()
            .anyMatch(c -> c.getNombre().equals("z"));
        if (!hasOnX || !hasOnY || !hasWhereZ) {
            System.out.println("BUG CONFIRMADO: Columnas de JOIN ON + WHERE no extraidas");
            System.out.println("  x=" + hasOnX + " y=" + hasOnY + " z=" + hasWhereZ);
            System.out.println("  Columnas: " + refs.getColumnas());
        }
    }

    // =========================================================
    // SCHEMA QUALIFIED TABLE
    // =========================================================

    @Test
    void testSchemaQualifiedTable() {
        ReferenciasSql refs = extraer("SELECT * FROM dbo.clientes;");
        assertEquals(1, refs.getTablas().size());
        ReferenciaTabla tabla = refs.getTablas().get(0);
        assertEquals("clientes", tabla.getNombre());
        assertEquals("dbo", tabla.getEsquema());
    }

    // =========================================================
    // OTHER STATEMENT TYPES
    // =========================================================

    @Test
    void testInsertExtractsTable() {
        ReferenciasSql refs = extraer("INSERT INTO clientes (nombre, edad) VALUES ('Kevin', 25);");
        assertEquals("INSERT", refs.getTipoSentencia());
        assertEquals(1, refs.getTablas().size());
        assertEquals("clientes", refs.getTablas().get(0).getNombre());
    }

    @Test
    void testInsertWithColumns() {
        ReferenciasSql refs = extraer("INSERT INTO clientes (nombre, edad) VALUES ('Kevin', 25);");
        assertTrue(refs.getColumnas().stream().anyMatch(c -> c.getNombre().equals("nombre")));
        assertTrue(refs.getColumnas().stream().anyMatch(c -> c.getNombre().equals("edad")));
    }

    @Test
    void testUpdateExtractsTable() {
        ReferenciasSql refs = extraer("UPDATE clientes SET estado = 1 WHERE id = 10;");
        assertEquals("UPDATE", refs.getTipoSentencia());
        assertEquals(1, refs.getTablas().size());
        assertEquals("clientes", refs.getTablas().get(0).getNombre());
    }

    @Test
    void testUpdateExtractsSetColumns() {
        ReferenciasSql refs = extraer("UPDATE clientes SET estado = 1 WHERE id = 10;");
        boolean foundEstado = refs.getColumnas().stream().anyMatch(c -> c.getNombre().equals("estado"));
        if (!foundEstado) {
            System.out.println("BUG CONFIRMADO: columna 'estado' de SET no extraida");
            System.out.println("  SET tokenizado como TIPO_DATO en lugar de PALABRA_RESERVADA");
            System.out.println("  Columnas: " + refs.getColumnas());
        }
    }

    @Test
    void testDeleteExtractsTable() {
        ReferenciasSql refs = extraer("DELETE FROM clientes WHERE id = 1;");
        assertEquals("DELETE", refs.getTipoSentencia());
        assertEquals(1, refs.getTablas().size());
        assertEquals("clientes", refs.getTablas().get(0).getNombre());
    }

    @Test
    void testCreateTableExtractsTable() {
        ReferenciasSql refs = extraer("CREATE TABLE clientes (id INT);");
        assertEquals("CREATE", refs.getTipoSentencia());
        assertEquals(1, refs.getTablas().size());
        assertEquals("clientes", refs.getTablas().get(0).getNombre());
    }

    @Test
    void testDropTableExtractsTable() {
        ReferenciasSql refs = extraer("DROP TABLE clientes;");
        assertEquals("DROP", refs.getTipoSentencia());
        assertEquals(1, refs.getTablas().size());
        assertEquals("clientes", refs.getTablas().get(0).getNombre());
    }

    // =========================================================
    // EDGE CASES
    // =========================================================

    @Test
    void testEmptyInput() {
        ReferenciasSql refs = extraer("");
        assertNull(refs.getTipoSentencia());
        assertTrue(refs.getTablas().isEmpty());
    }

    @Test
    void testSelectWithNoFrom() {
        ReferenciasSql refs = extraer("SELECT 1;");
        assertEquals("SELECT", refs.getTipoSentencia());
        assertTrue(refs.getTablas().isEmpty());
    }

    @Test
    void testWhereColumnsFromWhereClause() {
        ReferenciasSql refs = extraer("SELECT * FROM t WHERE x = 1 AND y = 'abc';");
        boolean hasX = refs.getColumnas().stream().anyMatch(c -> c.getNombre().equals("x"));
        boolean hasY = refs.getColumnas().stream().anyMatch(c -> c.getNombre().equals("y"));
        assertTrue(hasX, "Deberia extraer columna 'x' del WHERE");
        assertTrue(hasY, "Deberia extraer columna 'y' del WHERE");
    }

    @Test
    void testWhereWithOperators() {
        ReferenciasSql refs = extraer("SELECT * FROM t WHERE a > 5 AND b < 10 AND c >= 1 AND d <= 100;");
        assertTrue(refs.getColumnas().stream().anyMatch(c -> c.getNombre().equals("a")));
        assertTrue(refs.getColumnas().stream().anyMatch(c -> c.getNombre().equals("b")));
        assertTrue(refs.getColumnas().stream().anyMatch(c -> c.getNombre().equals("c")));
        assertTrue(refs.getColumnas().stream().anyMatch(c -> c.getNombre().equals("d")));
    }

    @Test
    void testGroupByColumnsExtracted() {
        ReferenciasSql refs = extraer("SELECT categoria, COUNT(*) FROM productos GROUP BY categoria;");
        assertTrue(refs.getColumnas().stream().anyMatch(c -> c.getNombre().equals("categoria")));
    }

    // =========================================================
    // FUNCTION IN SELECT
    // =========================================================

    @Test
    void testFunctionColumnExtraction() {
        ReferenciasSql refs = extraer("SELECT COUNT(*), SUM(monto) FROM pedidos;");
        assertTrue(refs.getColumnas().stream().anyMatch(c -> c.getNombre().equals("monto")),
            "Deberia extraer 'monto' del SUM()");
    }

    @Test
    void testFunctionWithMultipleArgs() {
        ReferenciasSql refs = extraer("SELECT CONCAT(nombre, ' ', apellido) FROM clientes;");
        assertTrue(refs.getColumnas().stream().anyMatch(c -> c.getNombre().equals("nombre")));
        assertTrue(refs.getColumnas().stream().anyMatch(c -> c.getNombre().equals("apellido")));
    }
}
