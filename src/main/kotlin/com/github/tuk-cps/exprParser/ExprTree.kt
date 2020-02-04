package exprParser

import jAADD.AADD
import jAADD.BDD
import jAADD.DD

/**
 * The class ExpressionTree implements an attributed syntax tree (AST).
 * The base class of the expression tree is a simple value,
 * represented by result: a constant literal or a variable.
 * For variables, there is an id (name); for literals the result is its value.
 */
open class ExprTree(value: DD<*> = BDD.Infeasible,
                    id: String = "uninitialized",
                    parent: ExprTree? = null) {

    var value: DD<*>   = value   // The result computed or the value, if leaf
    var id: String      = id       // If a variable, its name.
    internal var parent = parent   // Reference to the parent node or null, if root.

    open fun copy(): ExprTree = ExprTree(value, id, parent)

    @get:Throws(ExprError::class)
    val aadd: AADD
        get() = if (value is AADD) value as AADD else throw ExprError("wrong type. Expected REAL.")

    @get:Throws(ExprError::class)
    val bdd: BDD
        get() = if (value is BDD) value as BDD else throw ExprError("wrong type. Expected BOOL.")

    @Throws(ExprError::class)
    open fun evalUp() {
        if (id == "") return  // a number literal.
        val v: ExprTree = symTab.getVar(id)
        value = v.value
    }

    open fun evalDown() {
        if (id == "") return // a number literal
        val v: ExprTree = symTab.getVar(id)
        if (this.value is AADD)  {
            this.value = (this.value as AADD).intersect(v.aadd)
        }
    }

    /** This method computes the expression from the leaves to the root. */
    @Throws(ExprError::class)
    open fun evalUpRec() = evalUp()

    open fun evalDownRec() = evalDown()

    override fun toString(): String
        = if (id.isBlank()) "" + value else "(" + id + ": " + symTab.getVar(id).value + ")"

    companion object {
        lateinit var symTab: SymbolTable
    }
}