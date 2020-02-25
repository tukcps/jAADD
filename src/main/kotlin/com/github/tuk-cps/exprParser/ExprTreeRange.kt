package exprParser

import jAADD.AADD

/**
 * Predefined function: range, uses AADD with new noise symbol.
 */
internal class ExprTreeRange : ExprTreeFunction(AADD.Reals, "range", ExprTree(AADD.Reals, "min"), ExprTree(AADD.Reals, "max")) {
    @Throws(ExprError::class)
    override fun evalUp() {
        if (param.size != 2) throw ExprError("range expects 2 parameters")
        val l = getAADDParam(0).value!!.min
        val r = getAADDParam(1).value!!.max
        value = AADD.range(l, r, -1)
    }
}