package jAADD

import com.google.gson.GsonBuilder
import jAADD.Conditions.getConstraint
import jAADD.Conditions.getVariable


/**
 * The class DD implements a decision diagram as base class template leaf types.
 * It is the base class from which different kind of DD are inherited.
 * It provides the basic framework, but not the type of the leaves.
 * This involves in particular the management of the conditions and
 * index that are common for AADD and BDD.
 *
 * @author Christoph Grimm, Carna Zivkovic
 */
open class DD<ValT>(index: Int, T: DD<ValT>?, F: DD<ValT>?, value: ValT?, status: Status = Status.notSolved) {

    /**
     * The DD is ordered by the index. The index of leaves is Integer.MAXVALUE.
     * The index of other nodes grows from 0 (root) with increasing height of the graph.
     */
    internal val index: Int = index

    /** The true and false child of internal nodes, otherwise null.  */
    internal val T: DD<ValT>? = T
    internal val F: DD<ValT>? = F

    /** Value of a leaf node, otherwise null.  */
    val value: ValT? = value // internal set

    /**
     * The status of a node's path condition.
     * After instantiation of a new DD, it is not solved.
     * After solving the LP problem, paths are known to be feasible/infeasible.
     */
    enum class Status {notSolved, feasible, infeasible}
    internal var status: Status = status
    val isFeasible:   Boolean get() = status != Status.infeasible
    val isInfeasible: Boolean get() = status == Status.infeasible


    /**
     * Creates a leaf node with value val.
     * @param val Value of leaf. NOT TO BE USED WITH BDD.
     * @param feasible Specifies if the path was evaluated by LP solver to be feasible.
     */
    protected constructor(value: ValT, status: Status): this(Int.MAX_VALUE, null, null, value, status)

    /**
     * Creates an internal node with index i that is already in use and the two child.
     * @param i of condition. Index must be already assigned a condition.
     * @param T DD that will be used for T.
     * @param F DD that will be used for F.
     */
    protected constructor(i: Int, T: DD<ValT>, F: DD<ValT>): this(i, T, F, null, Status.notSolved) {
        require(getConstraint(i) != null || getVariable(i) != null)
        require(i < T.index) { "  DD insane: index " + i + " but T " + T.index }
        require(i < F.index) { "  DD insane: index " + i + " but F " + F.index }
    }

    /** Returns true if the node is a leaf.  */
    val isLeaf: Boolean
        get() = index == Int.MAX_VALUE

    /** Returns true if the node is an internal node  */
    val isInternal: Boolean
        get() = index != Int.MAX_VALUE

    /** Returns the number of leaves.  */
    fun numLeaves(): Int =
            if (isLeaf) 1 else T!!.numLeaves() + F!!.numLeaves()

    fun numInfeasible(): Int =
            if (isLeaf) { if (isInfeasible) 1 else 0 } else T!!.numInfeasible() + F!!.numInfeasible()

    /** Returns the height of the tree.  */
    fun height(): Int =
            if (isLeaf) 0 else 1 + Math.max(T!!.height(), F!!.height())

    /** Returns the value of a leaf.  */
    @Deprecated( "use getValue() instead as proper getter. ")
    fun Value(): ValT =  value!!

    /**
     * Gets the the to which the index refers, if it is a boolean variable, null.
     * @return Affine form that models a linear constraint `cond > 0`.
     */
    internal fun AFConstr(): AffineForm? = getConstraint(index)

    /** @return Returns true if the condition is a boolean variable. */
    internal fun isBoolCond(): Boolean = getConstraint(index) == null

    override fun toString(): String {
        if (isInfeasible) return "Infeasible"
        if (isLeaf) return value.toString()
        else return "ITE($index, $T, $F)"
    }

    /** Function that converts a DD to a Json string. */
    open fun toJson(): String {
        val gson = GsonBuilder().setPrettyPrinting().create()
        return gson.toJson(this)
    }
}

