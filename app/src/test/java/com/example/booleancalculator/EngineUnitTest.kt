package com.example.booleancalculator

import com.example.booleancalculator.domain.engine.BooleanEvaluator
import com.example.booleancalculator.domain.engine.KMapEngine
import com.example.booleancalculator.domain.engine.Parser
import com.example.booleancalculator.domain.engine.SimplificationEngine
import com.example.booleancalculator.domain.engine.Tokenizer
import com.example.booleancalculator.domain.model.ASTNode
import com.example.booleancalculator.domain.model.OpType
import com.example.booleancalculator.domain.model.Token
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EngineUnitTest {

    @Test
    fun testTokenizerCompactVarsAndImplicitAnd() {
        val tokens = Tokenizer.tokenize("WX")
        assertEquals(3, tokens.size)
        assertTrue(tokens[0] is Token.Var)
        assertEquals("W", (tokens[0] as Token.Var).value)
        assertTrue(tokens[1] is Token.Op)
        assertEquals(OpType.AND, (tokens[1] as Token.Op).type)
        assertTrue(tokens[2] is Token.Var)
        assertEquals("X", (tokens[2] as Token.Var).value)
    }

    @Test
    fun testParserPrecedence() {
        val tokens = Tokenizer.tokenize("W OR X AND Y")
        val ast = Parser.parse(tokens)
        assertTrue(ast is ASTNode.BinaryOpNode)
        val binNode = ast as ASTNode.BinaryOpNode
        assertEquals(OpType.OR, binNode.type)
        assertEquals("W", (binNode.left as ASTNode.VarNode).name)
        assertTrue(binNode.right is ASTNode.BinaryOpNode)
        assertEquals(OpType.AND, (binNode.right as ASTNode.BinaryOpNode).type)
    }

    @Test
    fun testTruthTableGeneration() {
        val tokens = Tokenizer.tokenize("W AND X")
        val ast = Parser.parse(tokens)
        val vars = BooleanEvaluator.extractVars(ast)
        assertEquals(listOf("W", "X"), vars)

        val model = BooleanEvaluator.buildTruthTableModel(ast, vars)
        assertEquals(4, model.rows.size)
        assertEquals(1, model.rows[3].result)
        assertEquals(0, model.rows[0].result)
    }

    @Test
    fun testSimplificationEngine() {
        val tokens = Tokenizer.tokenize("W AND X OR W AND !X")
        val ast = Parser.parse(tokens)
        val vars = BooleanEvaluator.extractVars(ast)
        val simp = SimplificationEngine.simplifyExpression(ast, vars)
        assertEquals("W", simp.expr)
    }

    @Test
    fun testIdentitySimplification() {
        val tokens = Tokenizer.tokenize("W AND 1")
        val ast = Parser.parse(tokens)
        val steps = SimplificationEngine.buildIdentitySimplificationSteps(ast)
        assertTrue(steps.size >= 2)
        assertEquals("W", steps.last().expression)
    }

    @Test
    fun testKMapGeneration() {
        val tokens = Tokenizer.tokenize("W AND X OR Y AND Z")
        val ast = Parser.parse(tokens)
        val vars = BooleanEvaluator.extractVars(ast)
        val kmap = KMapEngine.buildKMap(ast, vars)
        assertNotNull(kmap)
        assertEquals(4, kmap.grid.size)
        assertEquals(4, kmap.grid[0].size)
    }
}
