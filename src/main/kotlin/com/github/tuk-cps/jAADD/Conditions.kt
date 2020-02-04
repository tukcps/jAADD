package jAADD

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.FileWriter
import java.util.*
import kotlin.collections.HashMap

/**
 * The object Conditions implements the sets of conditions X and Xb.
 * The conditions are global (static) and shared by all BDD and AADD.
 * There are two types of conditions:
 * Unknown Boolean variables and Relational operations.
 *
 * The relational operations are, for each index, each of the form
 * `AffineForm >= 0, with -1 <= ei <= 1`
 * They are saved in the hashmap X and shared among all AADD/BDD.
 *
 * The unknown Boolean variables are saved in a hashmap Xb and shared among all AADD/BDD.
 * The unknown Boolean variables are modeled by the enum BoolX.
 */

object Conditions {

    enum class XBool { True, False, X, AF }

    /** The set of all conditions is saved in the HashMap X */
    internal var X = HashMap<Int, Condition>()
    internal var topIndex = 0 // last index used for increasing index selection.
    internal var btmIndex = 0 // last index used for decreasing index selection.

    @JvmStatic
    fun init() {
        topIndex = 0
        btmIndex = 0
        X = HashMap()
    }

    /** States of the Boolean decision variables including X for unknown and AF for a constraint */
    // enum class XBool { True, False, X, AF }

    /** A condition can be a boolean variable or a constraint, represented by an affine form */
    internal class Condition(var name: String, var boolVar: XBool=XBool.AF, var constr: AffineForm?=null) {
        constructor(a: AffineForm, name: String = "") : this(name, XBool.AF, a)
        constructor(b: XBool, name: String = ""): this(name, b)
        override fun toString(): String {
            return "$name -> " +
                when (boolVar) {
                    XBool.AF    -> "Constraint $name $constr > 0"
                    XBool.True  -> "True"
                    XBool.False -> "False"
                    XBool.X     -> "Bool "+ name
                }
        }
    }


    /**
     * Adds a constraint in form of an affine form.
     * @return index of the new condition.
     */
    @JvmStatic
    fun newConstraint(c: AffineForm, name: String = ""): Int {
        X[++topIndex] = Condition(c, name)
        return topIndex
    }

    /**
     * Adds a Boolean decision variable with a documentation string.
     * @return index of the new condition.
     */
    @JvmStatic
    fun newVariable(name: String = ""): Int {
        X[++topIndex] = Condition(XBool.X, name)
        return topIndex
    }

    /** Adds a new constraint at the bottom of the indexes */
    @JvmStatic
    fun newBtmConstr(c: AffineForm, name: String = ""): Int {
        X[--btmIndex] = Condition(c, name)
        return btmIndex
    }

    /**
     * The method gets the condition x_i from set of conditions X
     * @param i index of X
     * @return x_i if x_i is an affine form, otherwise null.
     */
    @JvmStatic
    fun getConstraint(i: Int): AffineForm? {
        assert(i in btmIndex .. topIndex) { "index out of range accessed: $i" }
        assert(i != Int.MIN_VALUE)
        assert(X[i] != null) { "condition not defined" }
        return X[i]!!.constr
    }

    /**
     * The method gets a boolean condition x_i from X
     * @param i, an index
     * @return x_i if x_i is a boolean variable, otherwise null.
     */
    @JvmStatic
    fun getVariable(i: Int): XBool? {
        assert(i in btmIndex .. topIndex) { "index out of range accessed: $i" }
        assert(i != Int.MIN_VALUE)     { "index reserved for leaves: $i"}
        assert(X[i] != null)
        return X[i]!!.boolVar
    }

    /** Creates a string that documents the set of all conditions and constraints */
    override fun toString(): String {
        var s = "Conditions: \n"
        for (i in btmIndex..topIndex) s += "  Index $i -> "+ getConstraint(i) + "\n"
        return s
    }


    internal val gson = GsonBuilder().setPrettyPrinting().create()

    fun toJson(): String = gson.toJson(this.X)

    fun toJson( filename: String ) {
        val fw = FileWriter(filename)
        gson.toJson(this.X, fw)
        fw.close()
    }

    /**
     * read conditions from a json file
     */
    fun fromJson( filename: String) {
        val empMapType = object : TypeToken<HashMap<Int, Condition>>() {}.type
        val file = java.io.FileReader(filename)
        X.clear()
        X = gson.fromJson(file, empMapType)

        X.forEach {
            k,_ -> topIndex=Math.max(k, topIndex)
                   btmIndex=Math.min(k, btmIndex)
        }
    }

}