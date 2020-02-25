@file:JvmName("NiceApi4Java")

package jAADD


import java.util.*


/**
 * For an assignment in a condition block.
 * x = x.assignS(thenval)
 */
fun AADD.assignS(thenval: AADD)
        = NiceApi.conds.peek().ite(thenval.clone() as AADD, this.clone() as AADD)

operator fun ClosedFloatingPointRange<Double>.contains(r: AADD): Boolean {
    return if (r.isLeaf) {
        if (r.value!!.max < endInclusive )  true
        else if (r.value.min > start)  false
        else true
    } else r.T()!!.contains(this) || r.F()!!.contains(this)
}

/**
 * Functions for comfortable modeling of control flow.
 * ifS, endS, elseS allow modeling of if/else statement.
 *
 */
object NiceApi {
    internal var conds: Stack<BDD> = Stack<BDD>()
    @JvmStatic fun ifS(cond: BDD)= conds.push(cond)
    @JvmStatic fun endS(): BDD = conds.pop()
    @JvmStatic fun elseS(): BDD = conds.push(endS().not())
}
