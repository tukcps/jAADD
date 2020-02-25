package exprParser

import jAADD.AADD
import jAADD.BDD
import jAADD.DD
import java.util.*

/**
 * A function call of a user-defined function.
 * It defines a local scope by mapping the parameters to the variables used during declaration of the function.
 * It searches for variables only in the local scope.
 */
open class ExprTreeFunction : ExprTree {

    var param: ArrayList<ExprTree> = ArrayList<ExprTree>()
    internal var ast: ExprTree? = null
        set(value) { field = if (value==null) null else value.copy() }

    internal constructor()

    /**
     * @method constructor
     * @param val
     * @param name
     * @param param
     */
    internal constructor(value: DD<*>, name: String, vararg param: ExprTree) : super(value, name) {
        this.param = ArrayList<ExprTree>(listOf(*param))
        // ToDo: Iterate over parameters, set parent pointer.
        // cannot set parent pointers to ExprTree as it has no children
        for (currtree in this.param) {
            currtree.parent = null
            //addPP(currtree);
        }
    }


    public override fun copy(): ExprTree {
        return try {
            val cp = javaClass.getDeclaredConstructor().newInstance()
            cp.parent = parent
            cp.id = id
            cp.value = value
            cp.ast = ast
            cp.param = ArrayList(param)
            for (i in param.indices) cp.param[i] = param[i].copy()
            cp
        } catch (e: Exception) {
            e.printStackTrace()
            return ExprTree(BDD.False, "Exception")
        }
    }

    /**
     * Processes a user-defined function call.
     * First, gets the function from the symbol table.
     * @throws ExprError
     */
    @Throws(ExprError::class)
    public override fun evalUp() {
        assert(ast != null) { "AST of function call must not be null" }
        // throw new ExprError("function "+id+" not defined."); // function not defined
        // ast.evalExpr();
        // ast = getSymTab().getFuncCall(id, param);
        ast!!.evalUpRec()
        value = ast!!.value
    }

    // Computes the complete tree recursively.
    @Throws(ExprError::class)
    public override fun evalUpRec() {
        symTab.scopes.push(this)
        for (par in param) {
            par.evalUpRec()
        }
        evalUp()
        symTab.scopes.pop()
    }

    @Throws(ExprError::class)
    fun getParam(nr: Int): ExprTree {
        if (nr >= param.size) throw ExprError("not enough parameters")
        return param[nr]
    }

    @Throws(ExprError::class)
    fun getBDDParam(nr: Int): BDD {
        if (nr >= param.size) throw ExprError("not enough parameters")
        return param[nr].bdd
    }

    @Throws(ExprError::class)
    fun getAADDParam(nr: Int): AADD {
        if (nr >= param.size) throw ExprError("not enough parameters")
        return param[nr].aadd
    }

    override fun toString(): String {
        return try {
            var s = "( " + value + " " + id
            for (par in param) {
                s += "$par "
            }
            s += if (ast != null) " -> $ast" else " -> builtin "
            s += ")"
            s
        } catch (e: Exception) {
            "Error printing function call"
        }
    }
}