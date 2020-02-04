package exprParser

import jAADD.AADD
import jAADD.BDD
import jAADD.BDD.Companion.variable

/**
 * The ITE function.
 */
internal class ExprTreeITE : ExprTreeFunction(AADD.Reals, "ite", ExprTree(variable("x"), "i"), ExprTree(AADD.Reals, "t"), ExprTree(AADD.Reals, "e")) {
    // ITE function
    @Throws(ExprError::class)
    override fun evalUp() {
        require(param.size == 3) { "3 parameters expected" }
        value = if (getParam(1).value is BDD) getBDDParam(0).ite(getBDDParam(1), getBDDParam(2))
        else getBDDParam(0).ite(getAADDParam(1), getAADDParam(2))
    }

    // ITE parameters with lazy evaluation.
    @Throws(ExprError::class)
    override fun evalUpRec() {
        symTab.scopes.push(this)
        getParam(0).evalUpRec() // Evaluate i
        val i = getBDDParam(0) //
        if (i === BDD.True) {
            getParam(1).evalUpRec()
        } else if (i === BDD.False) {
            getParam(2).evalUpRec()
        } else {
            getParam(1).evalUpRec()
            getParam(2).evalUpRec()
        }
        // now do ITE function.
        evalUp()
        symTab.scopes.pop()
    }
}