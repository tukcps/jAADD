package jAADD

import jAADD.BDD.Companion.internal
import jAADD.Conditions.getConstraint
import jAADD.Conditions.newConstraint
import org.apache.commons.math3.optim.linear.*
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType
import java.util.*
import java.util.function.BiFunction
import kotlin.math.abs
import kotlinx.coroutines.*

/**
 * The class AADD implements an Affine Arithmetic Decision Diagram (AADD).
 * An AADD is, in very brief, a decision diagram (class DD) whose leave nodes
 * take values of type AffineForm. AADD are, like DD, ordered.
 * AADD objects are immutable.
 *
 * @author Christoph Grimm, Carna Zivkovic, Jack D. Martin
 */
class AADD: DD<AffineForm>, Cloneable {

    protected constructor(index: Int, T: AADD?, F: AADD?, value: AffineForm?, status: Status) :
            super(index, T, F, value, status)

    protected constructor():
            super (0, null, null, null, Status.notSolved)

    /**
     * Creates a new leaf node with a value given as parameter.
     * @param value Center value of Affine Form at leaf node.
     * @param feasible True if the path is feasible.
     */
    protected constructor(value: AffineForm, status: Status = Status.notSolved) :
            super(value, status)

    @Deprecated("Use factory method") internal constructor(value: Double) :
            super(AffineForm(value), Status.notSolved)
    @Deprecated("Use factory method") constructor(min: Double, max: Double, doc: String) :
            super(AffineForm(min, max, doc), Status.notSolved)
    @Deprecated("Use factory method") constructor(min: Double, max: Double, index: Int) :
            super(AffineForm(min, max, index), Status.notSolved)

    /**
     * Creates a new internal node with a given index.
     * index must refer to an existing condition. It must be smaller than maxIndex.
     * @param index is the index of the node level.
     * @param T is the true child.
     * @param F is the false child.
     */
    internal constructor(index: Int, T: AADD, F: AADD) : super(index, T, F)

    /**
     * Clone method. Makes a deep copy of the tree structure.
     * The leaves are not copied for BDD, where ONE and ZERO are merged.
     * Note that the clone method does also a reduction where possible.
     */
    override public fun clone(): Any =
            if (isLeaf) leaf(value!!, Status.notSolved)
            else internal(index, T()!!.clone() as AADD, F()!!.clone() as AADD)

    /**
     * Applies a unary operator on an AADD and returns its AADD result.
     * @param op operator to be applied on this AADD, returning result. This remains unchanged.
     * @return result of operation.
     */
    protected fun apply(block: AffineForm.() -> AffineForm): AADD {
        if (isInfeasible) return Infeasible
        return if (isLeaf) leaf(block(value!!) )
               else internal(index, T()!!.apply(block), F()!!.apply(block))
    }

    /** Negates a given AADD. @return negative value of this.  */
    fun negate(): AADD = this.apply(AffineForm::unaryMinus)

    /** Computes the exponentiation of a given AADD. @return exponentiation of this.  */
    fun exp(): AADD = this.apply(AffineForm::exp)

    /** Computes the square root of a given AADD. @return square root of this.  */
    fun sqrt(): AADD = this.apply(AffineForm::sqrt)

    /** Computes the natural logarithm of a given AADD. @return natural logarithm of this.  */
    fun log(): AADD = this.apply(AffineForm::log)

    /** Computes reciprocal of a given AADD. @return reciprocal of this.  */
    fun inv(): AADD = this.apply(AffineForm::inv)

    /**
     * @method intersect computes the set intersection of an affine form and an upper and lower
     * bound given as Doubles
     * @param lb lower bound of constraint
     * @param ub upper bound of constraint
     * @return constrained version of this.
     */
    fun intersect(lb: Double, ub: Double) =
            ((this ge scalar(lb)) and (this le scalar(ub))).ite(this, Empty)


    /**
     * @method intersect
     * @param lb lower bound of constraint
     * @param ub upper bound of constraint
     * @return constrained version of this.
     */
    infix fun intersect(other: ClosedFloatingPointRange<Double>) =
            ((this ge scalar(other.start)) and (this le scalar(other.endInclusive))).ite(this, Empty)


    /**
     * @method intersect
     * @param lb lower bound of constraint
     * @param ub upper bound of constraint
     * @return constrained version of this.
     */
    fun intersect(other: AADD): AADD {
        val notTooLow = ge(scalar(other.getRange().min))
        val notTooLarge = le(scalar(other.getRange().max))
        val overlap = notTooLow.and(notTooLarge)
        return overlap.ite(this, Empty)
    }


    /**
     * Applies a binary operator passed as parameter on the AADD
     * passed as first two parameters and returns result.
     * @param op the operation
     * @param g parameter to be applied on this.
     * @return result of binary operation on this and g.
     */
    private fun apply(op: BiFunction<AffineForm, AffineForm, AffineForm>, g: AADD): AADD {
        val fT: AADD?
        val fF: AADD?
        val gT: AADD?
        val gF: AADD?

        // Check for the terminals. It ends iteration and applies operation.
        if (isInfeasible || g.isInfeasible) return Infeasible
        if (isLeaf && value!!.isEmpty()) return Empty
        if (g.isLeaf && g.value!!.isEmpty()) return Empty
        if (isLeaf && g.isLeaf) return leaf(op.apply(value!!, g.value!!))

        // Otherwise, recursion following the T/F childs with largest index.
        val idx = Math.min(index, g.index)
        if (index <= g.index) {
            fT = T()
            fF = F()
        } else {
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
        val Tr = fT!!.apply(op, gT!!)
        val Fr = fF!!.apply(op, gF!!)
        return internal(idx, Tr, Fr)
    }

    /**
     * Applies a multiplication of the AADD with a BDD
     * passed as a parameter and returns result. The BDD is
     * interpreted as 1.0 for true and 0.0 for false.
     *
     * The result is an AADD where the 0/1 are replaced with 0/AffineForm of the AADD.
     *
     * @param g parameter to be multiplied with this.
     * @return result of binary operation on this and g.
     */
     operator fun times(g: BDD): AADD {
        val fT: AADD?
        val fF: AADD?
        val gT: BDD?
        val gF: BDD?
        val idx: Int

        // Check for the terminals of the BDD g. It ends iteration and applies operation.
        if (isInfeasible || g.isInfeasible) return Infeasible
        // ToDo: this prevents intersect() from running properly.
        // if (this.isLeaf && this.value!!.isEmpty()) return AADD.Empty;
        // NOTE: multiplication EMPTY * False = 0.0
        if (g === BDD.False) return scalar(0.0)
        if (g === BDD.True) return clone() as AADD

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
        val Tr = fT!! * gT!!
        val Fr = fF!! * gF!!
        return internal(idx, Tr, Fr)
    }

    /**
     * Adds parameter to this and returns result
     * @param b parameter to be added to this.
     * @return result of this + other.
     */
    operator fun plus(b: AADD): AADD =
            this.apply(BiFunction { obj: AffineForm, other: AffineForm -> obj.plus(other) }, b)

    operator fun minus(b: AADD): AADD =
            this.apply(BiFunction { obj: AffineForm, other: AffineForm -> obj.minus(other) }, b)

    operator fun times(b: AADD): AADD =
            this.apply(BiFunction { obj: AffineForm, other: AffineForm -> obj.times(other) }, b)

    operator fun times(b: Double): AADD =
            this.apply(BiFunction { obj: AffineForm, other: AffineForm -> obj.times(other) }, scalar(b))

    operator fun div(b: AADD): AADD =
            this.apply(BiFunction { obj: AffineForm, other: AffineForm -> obj.div(other) }, b)


    /**
     * Implements the relational operator less than `<`.
     * It compares an AADD with AADD passed as a parameter and calls the LP solver to compute min and max.
     * @param other - AADD to be compared with this
     * @return BDD
     */
    infix fun lt(other: AADD): BDD {
        val temp = (this-other)
        temp.getRange()
        return temp.checkObjective("<") // this-g < 0
    }

    /**
     * Implements relational operator less or equal than `<=`
     * @param other - AADD to be compared with this
     * @return BDD
     */
    infix fun le(other: AADD): BDD {
        val temp = (this - other)
        temp.getRange()
        return temp.checkObjective("<=") // this-g <=0
    }

    /**
     * computes the relational operator greater than `>`
     * @param other An AADD that is compared with this.
     * @return A BDD that represents the comparison of the leaves.
     */
    infix fun gt(other: AADD): BDD {
        val temp = this - other
        temp.getRange()
        return temp.checkObjective( ">") // this-other > 0
    }

    /**
     * Implements relational operator greater or equal than `>=`
     * @param other - AADD to be compared with this
     * @return A BDD that represents the comparison of the leaves.
     */
    infix fun ge(other: AADD): BDD {
        val temp = this - other
        temp.getRange()
        return temp.checkObjective(">=") // this-other >= 0
    }

    /**
     * This method computes the Range of an AADD considering
     *  *  the conditions as linear constraints.
     *  *  the noise symbol's limitations to -1 to 1.
     *  *  The affine forms at the leaves as objective functions to be min/max.
     */
    fun getRange(): Range {
        val height = height()
        val indexes = IntArray(height)
        val signs = BooleanArray(height)
        var r = Range.RealsNaN
        runBlocking {
             r = computeBounds(indexes, signs, 0)
        }
        return r
    }

    /**
     * Collects bounds of all leaves.
     * When the AADD is an internal node, it collects condition Xp,v on path to leave v.
     * For each leaf, it calls callLPSolver to compute bounds.
     * The method is called by getRange.
     */
    private suspend fun computeBounds(indexes: IntArray, ge: BooleanArray, len: Int): Range {
        if (isLeaf) {
            if (isInfeasible || value!!.isEmpty()) return Range.Empty
            if (value.isFinite()
                    && indexes.size > 0
                    && value.radius > LPCallTh
                    && status == Status.notSolved)
                callLPSolver(indexes, ge, len)
            return  if (isInfeasible || value.isEmpty()) Range.Empty
                    else Range(value.min, value.max)
        }
        if (!isBoolCond()) {
            var result: Range = Range.Empty
            indexes[len] = index
            withContext(Dispatchers.Default) {
                val resT = async() {
                    val ops = ge.copyOf()
                    ops[len] = true
                    T()!!.computeBounds(indexes.copyOf(), ops, len + 1)
                }
                val ops = ge
                ops[len] = false
                val resF = F()!!.computeBounds(indexes, ops, len + 1)
                result = resT.await().join(resF)
            }
            return result
        }
        val res = T()!!.computeBounds(indexes, ge, len)
        return res.join(F()!!.computeBounds(indexes, ge, len))
    }

    /**
     * Computes bounds of a leaf node using LP solver that considers the linear constraints of the internal nodes.
     * Called by computeBounds that collects bounds of all leaves of AADD.
     * It calls LP solver from Math3 package to solve the underlying LP optimization problem.
     * The model of the problem is defined by:
     * - Objective function, defined by the AffineForm of leaf node Value()
     * - Constraints, defined by the conditions in the internal nodes on the path from root to leaf.
     *
     * @param indexes the indexes from the path from root to the respective leave; set of conditions
     * @param ge Array with the results of conditions on the path to the respective leave.
     * @param len the sizes of the arrays.
     * @return
     */
    private fun callLPSolver(indexes: IntArray, ge: BooleanArray, len: Int) {
        require(len > 0) {"len of arrays must be >= 1"}

        // set union of noise symbols in Value() and conditions
        // creates dense set of variables for LP problem
        val symbols: MutableSet<Int> = TreeSet(value!!.xi.keys)
        for (i in 0 until len)
            symbols.addAll(getConstraint(indexes[i])!!.xi.keys)

        // holds partial deviations xi*ei in affine forms of conditions and value of leaf node
        // the last term is for r.
        val partial_terms = DoubleArray(symbols.size)
        // add constraints on noise symbols Ei and r:
        // -1 <= Ei <= 1, -1

        val constraints: MutableCollection<LinearConstraint> = ArrayList()
        for (i in symbols.indices) {
            partial_terms[i] = 1.0 // stores partial term Ei*1.0;
            constraints.add(LinearConstraint(partial_terms, Relationship.GEQ, -1.0))
            constraints.add(LinearConstraint(partial_terms, Relationship.LEQ, 1.0))
            partial_terms[i] = 0.0
        }
        // constraints from conditions of the internal nodes, incl. r.
        for (i in 0 .. len-1) {
            val condition = getConstraint(indexes[i])
            var k = 0
            for (symb in symbols) {
                partial_terms[k++] = condition!!.xi.getOrDefault(symb, 0.0)
            }
            if (ge[i])
                constraints.add(LinearConstraint(partial_terms, Relationship.GEQ, -condition!!.x0 - condition.r))
            else
                constraints.add(LinearConstraint(partial_terms, Relationship.LEQ, -condition!!.x0 + condition.r))
        }
        // creates objective function
        var k = 0
        for (symb in symbols) {
            partial_terms[k++] = value.xi.getOrDefault(symb, 0.0)
        }
        // if (AADD.debugLPsolver) printInequationSystem("InequationSystem", constraints, partial_terms, value)

        // Call LP solver
        val solver = SimplexSolver(1.0e-3, 100, 1.0e-10)
        try {
            val constraintSet = LinearConstraintSet(constraints)
            val solMax = solver.optimize(LinearObjectiveFunction(partial_terms, value.x0 + value.r), constraintSet, GoalType.MAXIMIZE, NonNegativeConstraint(false))
            value.max = java.lang.Double.min(value.max, solMax.value)
            val solMin = solver.optimize(LinearObjectiveFunction(partial_terms, value.x0 - value.r), constraintSet, GoalType.MINIMIZE, NonNegativeConstraint(false))
            value.min = java.lang.Double.max(value.min, solMin.value)
            status = Status.feasible
        } catch (e: NoFeasibleSolutionException) {
            // Infeasible leaf, we mark the leaf as infeasible; the value is the empty range.
            // value = null
            status = Status.infeasible
        } catch (e: UnboundedSolutionException) {
            // This should never happen. We write the inequation in a textfile for debugging.
            // Might be issue in LP solver. Modify Simplex cutoff and other parameters.
            printInequationSystem("InequationSystemOfUnboundedError.txt", constraints, partial_terms, value)
            sanityCheck()
            throw RuntimeException("AADD-Error: unbounded solution; maybe numerical issue in Simplex.")
        }
    }

    /**
     * Creates a BDD, depending on the result of a comparison.
     * The result can either be True, False, or unknown, ich which case we add a new level to the BDD.
     * @param indexes and operations of conditions from root to a leaf node
     * @param op
     * @return A BDD, set up recursively.
     */
    private fun checkObjective(op: String): BDD {

        if (isLeaf) {
            if (isInfeasible || value!!.isEmpty()) return BDD.Infeasible

            when(op) {
                ">=" -> {   if (value.min.compareTo(0.0) > 0 || abs(value.min) < 2 * Double.MIN_VALUE) return BDD.True
                            if (value.max.compareTo(0.0) < 0) return BDD.False
                        }
                ">" ->  {   if (value.min.compareTo(0.0) > 0) return BDD.True
                            if (value.max.compareTo(0.0) < 0 || abs(value.max) < 2 * Double.MIN_VALUE) return BDD.False
                        }
                "<=" -> {   if (value.min.compareTo(0.0) > 0) return BDD.False
                            if (value.max.compareTo(0.0) < 0 || abs(value.max) < 2 * Double.MIN_VALUE) return BDD.True
                        }
                "<" ->  {   if (value.min.compareTo(0.0) > 0 || abs(value.min) < 2 * Double.MIN_VALUE) return BDD.False
                            if (value.max.compareTo(0.0) < 0) return BDD.True
                        }
            }
            return if (op === ">=" || op === ">") internal(newConstraint(value), BDD.True, BDD.False)
                   else internal(newConstraint(value), BDD.False, BDD.True)
        }

        /* Recursion step. */
        val Tr: BDD = T()!!.checkObjective(op)
        val Fr: BDD = F()!!.checkObjective(op)
        return internal(index, Tr, Fr)
    }


    /** Casts T to AADD. @return T with type AADD. */
    internal fun T(): AADD? = T as AADD?

    /** Casts F to AADD. @return F with type AADD. */
    internal fun F(): AADD? = F as AADD?

    /**  Returns the number of leaves. */
    fun numFeasibleLeaves(): Int {
        if (isLeaf)
            return if (value!!.isTrap()) 0 else 1
        else
            return T()!!.numFeasibleLeaves() + F()!!.numFeasibleLeaves()
    }

    /** Double in AADD. Allows us writing "Double in AADD" */
    operator fun contains(x: Double): Boolean {
        if (isLeaf) {
            if (x>value!!.max) return false
            if (x<value.min) return false
            return true
        } else {
            return T()!!.contains(x) || F()!!.contains(x)
        }
    }

    /** Overriden operator "in" that allows us to check "Double .. Double in AADD" -> Boolean */
    operator fun contains(x: ClosedFloatingPointRange<Double>): Boolean {
        if (isLeaf) {
            if (x.start > value!!.max) return false
            if (x.endInclusive < value.min) return false
            return true
        } else {
            return T()!!.contains(x) || F()!!.contains(x)
        }
    }

    override fun toString(): String {
        getRange()
        if (isInfeasible) return "Infeasible"
        if (isLeaf) return value.toString()
        else return "ITE($index, $T, $F)"
    }

    companion object {
        /**
         * Constants.
         *  *  Real models an arbitrary real number without any constraints.
         *     It is between -infinity and +infinity.
         *  *  Empty models an empty range, also a non-existing number, e.g. NaN.
         *  * Infeasible models a leaf whose path condition is infeasible.
         */
        @JvmField val Reals      = AADD(AffineForm.Reals, Status.notSolved)
        @JvmField val Empty      = AADD(AffineForm.Empty, Status.notSolved)
        @JvmField val Infeasible = AADD(AffineForm.Empty, Status.infeasible)
        @JvmField val RealsNaN   = AADD(AffineForm.RealsNaN, Status.notSolved)


        /** Settings for some numerical parameters. */
        var LPCallTh = 0.001 // If the radius is below this value, the LP Solver will not be called to compute a smaller range.
        var joinTh  = 0.001
        var toStringVerbose = false

        /** Factory: Creates a new leaf with an affine form as value.  */
        @JvmStatic
        fun scalar(value: Double): AADD =
                AADD(AffineForm(value), Status.notSolved)

        /** Factory: Creates a new leaf with an affine form as a value.  */
        @JvmStatic
        fun range(min: Double, max: Double, index: Int): AADD {
            require(min < max) { "min must be less than max; range expected" }
            require((index > 0) or (index == -1)) { "index must be -1 or > 0" }
            return AADD(AffineForm(min, max, index), Status.notSolved)
        }

        fun range(r: ClosedFloatingPointRange<Double>) : AADD =
                AADD(AffineForm(r.start, r.endInclusive, -1), Status.notSolved)

        fun range(r: ClosedFloatingPointRange<Double>, doc: String) : AADD =
                AADD(AffineForm(r.start, r.endInclusive, doc), Status.notSolved)

        fun range(r: ClosedFloatingPointRange<Double>, index: Int) : AADD =
                AADD(AffineForm(r.start, r.endInclusive, index), Status.notSolved)


        /** Factory: Creates a new leaf with a documenting string */
        @JvmStatic
        fun range(min: Double, max: Double, id: String): AADD {
            require(min < max) { "min must be less than max; range expected" }
            return AADD(AffineForm(min, max, id), Status.notSolved)
        }

        /** Factory: Creates a new leaf with an affine form as value.  */
        internal fun leaf(value: AffineForm, status: Status = Status.notSolved) =
                if (status == Status.infeasible) AADD.Empty
                else if (value.isEmpty()) Empty else AADD(value, Status.notSolved)

        /** Factory: Creates a new leaf with an affine form as value.  */
        internal fun leaf(value: AffineForm) =
                if (value.isEmpty()) Empty else AADD(value, Status.notSolved)

        /**
         * Creates a new internal node with index index and child nodes T and F.
         * The method also does reduction for
         * * infeasible nodes
         * * internal nodes that have the same child
         * * similar leaves.
         */
        internal fun internal(index: Int, T: AADD, F: AADD): AADD {

            // Reduction of nodes with the same childs or infeasible paths:
            if (T === F || T.isInfeasible) {
                return AADD(F.index, F.T(), F.F(), F.value, F.status)
            }
            if (F.isInfeasible) { // System.out.println("Reduction of AADD ...");
                return AADD(T.index, T.T(), T.F(), T.value, T.status)
            }
            // Reduction of similar leaves:
            return if (T.isLeaf && F.isLeaf && T.value!!.isSimilar(F.value!!, joinTh))
                        leaf(T.value.join(F.value))
                   else AADD(index, T, F, null, Status.notSolved)
        }
    }
}

