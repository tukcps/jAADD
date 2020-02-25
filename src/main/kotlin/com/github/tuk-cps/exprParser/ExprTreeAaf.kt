package exprParser

import jAADD.AADD
import jAADD.AffineForm

/**
 * Predefined function aaf that generates a new Affine Form with min, max, and the
 * key of a new noise symbol as parameters.
 */
internal class ExprTreeAaf: ExprTreeFunction(AADD.Reals, "aaf",
        ExprTree(AADD.Reals, "min"),
        ExprTree(AADD.Reals, "max"),
        ExprTree(AADD.Reals, "key")) {
    @Throws(ExprError::class)
    override fun evalUp() {
        if (param.size != 3) throw ExprError("aaf requires 3 parameters: min, max, symbol no.")
        val l = getAADDParam(0).value!!.min
        val r = getAADDParam(1).value!!.max
        value = AADD.leaf(AffineForm(l, r, Math.round(getAADDParam(2).value!!.x0).toInt()))
    }
}