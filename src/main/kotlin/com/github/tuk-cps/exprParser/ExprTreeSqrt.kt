package exprParser

import jAADD.AADD

/**
 * Predefined functions: sqrt
 */
internal class ExprTreeSqrt : ExprTreeFunction(AADD.Reals, "sqrt", ExprTree(AADD.Reals, "x")) {
    @Throws(ExprError::class)
    override fun evalUp() {
        value = getAADDParam(0).sqrt()
    }
}