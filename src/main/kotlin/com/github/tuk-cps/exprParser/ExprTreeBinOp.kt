package exprParser

/**
 * @class ExprTreeBinOp
 * A binary operation.
 *
 * @author Christoph Grimm, Jack D. Martin
 */
internal class ExprTreeBinOp(l: ExprTree, op: Int, r: ExprTree) : ExprTree(l.value, "internal")  {

    var l: ExprTree = l.copy()
    var r: ExprTree = r.copy()
    var op = op

    init {
        l.parent = this
        r.parent = this
    }

    override fun copy(): ExprTreeBinOp {
        val cp = ExprTreeBinOp(l, op, r)
        cp.parent = parent
        cp.id = id
        cp.value = value
        return cp
    }

    /** Computes operands from leaves upwards */
    @Throws(ExprError::class)
    override fun evalUpRec() {
        l.evalUpRec()
        r.evalUpRec()
        evalUp()
    }

    /** Computes one level upwards, from childs to parent */
    @Throws(ExprError::class)
    override fun evalUp() {
        value = when (op) {
            '+'.toInt() -> l.aadd + r.aadd
            '-'.toInt() -> l.aadd - r.aadd
            '*'.toInt() -> l.aadd * r.aadd
            '/'.toInt() -> l.aadd / r.aadd
            '>'.toInt() -> l.aadd gt r.aadd
            '<'.toInt() -> l.aadd lt r.aadd
            ExprScanner.GE -> l.aadd ge r.aadd
            ExprScanner.LE -> l.aadd le r.aadd
            '&'.toInt() -> l.bdd and r.bdd
            '|'.toInt() -> l.bdd or r.bdd
            '='.toInt() -> throw ExprError("Equality of two AADD is not supported.")
            else -> throw ExprError("Operation " + op.toChar() + " resp. " + op + " not supported on AADD.")
        }
        println("computed up: $this")
    }

    /** Computes one level downwards, from parent to childs */
    override fun evalDown() {
        val prevL = l.aadd
        val prevR = r.aadd
        when (op) {
            '+'.toInt() -> { l.value = aadd - prevR; r.value = aadd - prevL }
            '-'.toInt() -> { l.value = aadd + prevR; r.value = aadd - prevL }
            '*'.toInt() -> { l.value = aadd / prevR; r.value = aadd / prevL }
            '/'.toInt() -> { l.value = aadd * prevR; r.value = prevL / aadd }
            else -> { }
        }
        l.aadd.intersect(prevL)
        r.aadd.intersect(prevR)
        println("computed down: $this")
    }

    override fun evalDownRec() {
        evalDown()
        l.evalDownRec()
        r.evalDownRec()
    }


    override fun toString() = "(id: $id $value ) = "+ l + op.toChar() + r
}