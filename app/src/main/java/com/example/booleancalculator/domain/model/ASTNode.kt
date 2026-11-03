package com.example.booleancalculator.domain.model

sealed class ASTNode {
    abstract val id: String
    abstract fun copyWithId(newId: String): ASTNode
    abstract val children: List<ASTNode>

    data class VarNode(val name: String, override val id: String = "") : ASTNode() {
        override fun copyWithId(newId: String): ASTNode = copy(id = newId)
        override val children: List<ASTNode> get() = emptyList()
    }

    data class NumNode(val value: String, override val id: String = "") : ASTNode() {
        override fun copyWithId(newId: String): ASTNode = copy(id = newId)
        override val children: List<ASTNode> get() = emptyList()
    }

    data class NotNode(val child: ASTNode, override val id: String = "") : ASTNode() {
        override fun copyWithId(newId: String): ASTNode = copy(id = newId)
        override val children: List<ASTNode> get() = listOf(child)
    }

    data class BinaryOpNode(
        val type: OpType,
        val left: ASTNode,
        val right: ASTNode,
        override val id: String = ""
    ) : ASTNode() {
        override fun copyWithId(newId: String): ASTNode = copy(id = newId)
        override val children: List<ASTNode> get() = listOf(left, right)
    }
}
