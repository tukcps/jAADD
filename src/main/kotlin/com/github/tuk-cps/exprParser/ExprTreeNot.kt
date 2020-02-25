package exprParser

import jAADD.BDD.Companion.variable

/**
 * Predefined functions: not
 */
internal class ExprTreeNot : ExprTreeFunction(variable("x"), "not", ExprTree(variable("x"), "x")) {
    @Throws(ExprError::class)
    override fun evalUp() {
        if (param.size != 1) throw ExprError("not expects 1 parameter")
        value = getBDDParam(0).not()
    }
}