package exprParser

import jAADD.AADD

/**
 * Predefined functions: log
 */
internal class ExprTreeLog : ExprTreeFunction(AADD.Reals, "log", ExprTree(AADD.Reals, "x")) {
    @Throws(ExprError::class)
    override fun evalUp() {
        value = getAADDParam(0).log()
    }
}