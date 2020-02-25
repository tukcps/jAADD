package exprParser

import jAADD.AADD

/**
 * Predefined functions: exp
 */
internal class ExprTreeExp : ExprTreeFunction(AADD.Reals, "exp", ExprTree(AADD.Reals, "x")) {
    @Throws(ExprError::class)
    override fun evalUp() {
        value = getAADDParam(0).exp()
    }
}