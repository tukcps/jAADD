package jAADD

import jAADD.Conditions.newVariable
import java.util.function.BiFunction

/**
 * The class BDD implements an ROBDD.
 * It is derived from the super class DD.
 * It has exactly two leaves that are either ONE or ZERO.
 * @author Christoph Grimm, Carna Zivkovic
 */
class BDD : DD<Boolean>, Cloneable {

    /** Constructor without parametes; don't use it. Only for compatibility with Spring, etc. */
    constructor(): super (0, null, null, null, Status.notSolved)


    /**
     * Creates a new leaf node.
     * The class is not needed for users as in BDD there exist only three leaf nodes,
     * ONE, ZERO, INFEASIBLE. It is used only to create these leaves.
     * Therefore it is private.
     * DONT USE THIS CONSTRUCTOR unless you know what you do.
     */
    protected constructor(value: Boolean, status: Status = Status.notSolved) : super(value, status)


    /**
     * Creates an internal node with a given index.
     * index must refer to an existing condition.
     * It must be smaller than maxIndex.
     * @param index is the index of the node level.
     * @param T is the true child; no copy is made.
     * @param F is the false child; no copy is made.
     */
    internal constructor(index: Int, T: BDD, F: BDD) : super(index, T, F)

    /**
     * Clone method. Copies the tree structure, but not conditions.
     * The leaves are not copied for BDD, where ONE and ZERO are merged.
     */
    override public fun clone(): Any {
        return if (isInternal)
                internal(index,
                        T()!!.clone() as BDD,
                        F()!!.clone() as BDD)
               else this
    }

    internal fun T(): BDD? = T as BDD?
    internal fun F(): BDD? = F as BDD?

    /**
     * Applies a unary operator on a BDD and returns its result.
     * It works recursively.
     * @param op a function on the BDD with a parameter.
     * @return a new BDD that is the result of the applied function.
     */
    private fun apply(block: Boolean.() -> Boolean): BDD =
            if (isLeaf) constant(block(value!!))
            else internal(index, T()!!.apply(block), F()!!.apply(block))

    operator fun not(): BDD = this.apply(Boolean::not)


    /**
     * Applies a binary operator passed as las parameter on the BDD
     * passed as first two parameters and returns result.
     * @param op the operation
     * @param g parameter 2
     * @return result of binary operation on the parameters
     */
    fun apply(op: BiFunction<Boolean, Boolean, Boolean>, g: BDD): BDD {
        val fT: BDD?
        val fF: BDD?
        val gT: BDD?
        val gF: BDD? // T, F of this and/or g
        val idx: Int
        // Iteration to leaves.
        // It ends iteration and applies operation.
        if (this === Infeasible || g === Infeasible) return Infeasible
        if (isLeaf && g.isLeaf) return constant(op.apply(value!!, g.value!!))

        // Recursion, with new node that has
        // the *largest* indices.
        if (index <= g.index) {
            idx = index
            fT = T()
            fF = F()
        } else {
            idx = g.index
            fF = this
            fT = fF
        }
        if (g.index <= index) {
            gT = g.T()
            gF = g.F()
        } else {
            gF = g
            gT = gF
        }
        // do the recursion
        val Tr = fT!!.apply(op, gT!!)
        val Fr = fF!!.apply(op, gF!!)
        // now, the operation is finished.
        return internal(idx, Tr, Fr)
    }

    infix fun and(other: BDD): BDD =
            this.apply(BiFunction { a: Boolean, b: Boolean -> java.lang.Boolean.logicalAnd(a, b) }, other)
    infix fun or(other: BDD): BDD =
            this.apply(BiFunction { a: Boolean, b: Boolean -> java.lang.Boolean.logicalOr(a, b) }, other)
    infix fun xor(other: BDD): BDD =
            this.apply(BiFunction { a: Boolean, b: Boolean -> java.lang.Boolean.logicalXor(a, b) }, other)
    infix fun nand(other: BDD): BDD =
            this.apply(BiFunction { a: Boolean, b: Boolean -> !(a && b) }, other)
    infix fun nor(other: BDD): BDD =
            this.apply(BiFunction { a: Boolean, b: Boolean -> !(a || b) }, other)
    infix fun xnor(other: BDD): BDD =
            this.apply(BiFunction { a: Boolean, b: Boolean -> a == b }, other)

    /**
     * Compares this BDD with other BDD for equality.
     * Two BDD are equal, if internal nodes have the same index, and leaves have the same value.
     * @param other
     * @return
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this.javaClass != other.javaClass) return false
        return if (isLeaf) other === this
               else T()!! == (other as BDD).T()!! && F()!! == other.F()!!
    }

    /**
     * The ITE function merges  BDD by an if-then-else-function.
     * Note, that the condition itself that is this BDD, is also a BDD.
     * The parameters are not changed.
     */
    fun ite(t: BDD, e: BDD): BDD {
        if (this === Infeasible) return Infeasible
        if (this === True) return t.clone() as BDD
        if (this === False) return e.clone() as BDD
        return (this and t) or (this.not() and e)
    }

    /**
     * The ITE function merges two AADD by an if-then-else-function.
     * Note, that the condition itself that is this BDD, is also a BDD.
     * The parameters are not changed.
     */
    fun ite(t: AADD, e: AADD): AADD {
        if (this === Infeasible) return AADD.Infeasible
        if (this === True) return t.clone() as AADD
        if (this === False) return e.clone() as AADD
        return (t * this) + (e * this.not() )
    }

    /**
     * Returns the number of leaves that hold the value true.
     * Also sometimes called numSat() which is the same function.
     */
    fun numTrue(): Int =
         if (isLeaf) if (this === True) 1 else 0 else T()!!.numTrue() + F()!!.numTrue()

    /**
     * Returns the number of leaves that hold the value false.
     * Also sometimes called numUnSat() which is the same function.
     */
    fun numFalse(): Int =
         if (isLeaf) if (this === False) 1 else 0 else T()!!.numFalse() + F()!!.numFalse()

    override fun hashCode(): Int = javaClass.hashCode()

    companion object {
        /**
         * True and False are the only leaf nodes that may be used.
         * Infeasible models a leaf whose path is infeasible.
         */
        @JvmField val True       = BDD(true)    // Leaf of value True
        @JvmField val False      = BDD(false)   // Leaf of value False
        @JvmField val Infeasible = BDD(true, Status.infeasible)    // Leaf with infeasible path cond.
        @JvmField val NaB        = BDD(false)   // Leaf with invalid result

        /** Use this to get a leaf node of a given boolean value.  */
        @JvmStatic
        fun constant(value: Boolean): BDD =
                if (value) True else False

        /** Use this to get a leaf node of a given boolean value whose path can be infeasible  */
        @JvmStatic
        fun constant(value: Boolean, status: Status): BDD =
                if (status == Status.infeasible) BDD.Infeasible
                else if (value) True
                else False


        /**
         * Creates an internal node with a given, existing index.
         * The function also reduces the BDD.
         * @param index is the index of the node level.
         * @param T is the true child; no copy is made.
         * @param F is the false child; no copy is made.
         */
        internal fun internal(index: Int, T: BDD, F: BDD): BDD =
                if (T === F) T
                else if (T.isInfeasible)  F
                else if (F.isInfeasible) T else BDD(index, T, F)


        /** This methods adds a new Boolean variable to the conditions  */
        @JvmStatic
        fun variable(varname: String): BDD =
                internal(newVariable(varname), True, False)
    }
}