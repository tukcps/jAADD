package exprParser

import jAADD.AADD
import jAADD.BDD
import jAADD.DD
import java.util.*
import kotlin.collections.HashMap

/**
 * @class SymbolTable
 * A simple symbol table:
 * - a hash map for globel symbols.
 * - a stack of locals in the parameters of function calls.
 * @author Christoph Grimm, Jack D. Martin
 */
open class SymbolTable {
    // Global (HashMap) and local variables (Stack)
    private val globals: HashMap<String, ExprTree> = HashMap()
    internal val scopes: Stack<ExprTreeFunction> = Stack()

    /**
     * @method defVar
     * @param name
     * @param tree
     */
    fun defVar(name: String, tree: ExprTree) = defVar(name, tree.value)

    /**
     * @method defVar
     * Defines a variable of type AADD, initialized with a given AADD.
     * @param name
     * @param value
     */
    fun defVar(name: String, value: DD<*>) { globals[name] = ExprTree(value, name) }

    /**
     * @method defFunc
     * Adds a function, represented by an expression tree, to the entry in the
     * table. Parameters are added when calling.
     *
     * @param name The name of the function.
     * @param func An ExprTree node that processes the function.
     */
    @Throws(ExprError::class)
    fun defFunc(name: String, func: ExprTreeFunction) { globals[name] = func }

    /**
     * @method defFunc
     * For a user-defined function, it sets the parameters in the symbol table.
     *
     * @param name  name of the function in symbol table.
     * @param param list of parameters.
     * @throws ExprError
     */
    @Throws(ExprError::class)
    fun defFunc(name: String, vararg param: ExprTree) {
        val call = ExprTreeFunction(BDD.True, name)
        call.param = ArrayList(Arrays.asList(*param))
        globals[name] = call
    }

    /**
     * @method defFunc
     * For a user-defined function, it sets the parameters in the symbol table.
     * @param name  name of the function in symbol table.
     * @param param list of parameters.
     * @throws ExprError
     */
    @Throws(ExprError::class)
    fun defFunc(name: String, param: ArrayList<ExprTree>) {
        val call = ExprTreeFunction(BDD.True, name)
        call.param = param
        globals[name] = call
    }

    /**
     * @method ExprTreeFunction
     * For a user-defined function, sets the expr. tree in the object from the symbol table.
     * @param name the name of the function
     * @param ast the expression tree representing the function
     * @return
     * @throws ExprError
     */
    @Throws(ExprError::class)
    fun defFuncBody(name: String, ast: ExprTree): ExprTreeFunction {
        val r = globals[name]
        if (r == null || r !is ExprTreeFunction) throw ExprError("identifier $name not a declared function")
        r.ast = ast
        return r
    }

    /**
     * @method getVar
     * During execution, it gets the entry for a variable from the symbol table.
     * @param name name of a variable to be searched in the symbol table or the parameters of a function.
     * @return Expression tree.
     */
    fun getVar(name: String): ExprTree {
        // First search in params of last function call on the call stack.
        if (!scopes.empty()) { // we are in function that is interpreted.
            val params = scopes.peek().param
            for (p in params)
                if (p.id == name) return p.copy()
        }
        // Then search in globals; it not there, let it be a Real.
        var r = globals[name]
        if (r == null) r = ExprTree(AADD.Reals, name)
        return r
    }

    /**
     * @method getFuncCall
     * @detail This method builds the expression tree for a function call.
     * It sets the parameters of a function in the local symbol table.
     * @param name
     * @param parameters
     * @return The Expression tree
     * @throws ExprError
     */
    @Throws(ExprError::class)
    fun getFuncCall(name: String, parameters: ArrayList<ExprTree>): ExprTreeFunction {
        val dec = globals[name]
        if (dec == null || dec !is ExprTreeFunction)
            throw ExprError("identifier $name not a declared function")
        if (dec.param.size != parameters.size)
            throw ExprError("number of parameters for function $name not matching declaration")
        val fCall = dec.copy() as ExprTreeFunction
        for (i in fCall.param.indices) { // Parameters from Call ...
            fCall.param[i] = parameters[i]
            // Name from formal parameters.
            fCall.param[i].id = dec.param[i].id
        }
        return fCall
    }

    /**
     * @method SymbolTableInfo
     * @detail For documentation
     * Useful for debugging ..
     * @return String that documents symbol table
     */
    fun symbolTableInfo(): String {
        var s = "Symbol table: \n"
        for (key in globals.keys) {
            s = "    " + s + key + ": " + globals[key] + "\n"
        }
        return s
    }

    /**
     * @method SymbolTable
     * @detail constructor
     */
    init {
        defVar("real", AADD.Reals)
        defVar("bool", BDD.variable("X"))
        defVar("pi", AADD.scalar(Math.PI))
        defVar("e", AADD.scalar(Math.E))
        defVar("true", BDD.True)
        defVar("false", BDD.False)
        defFunc("not", ExprTreeNot())
        defFunc("ite", ExprTreeITE())
        defFunc("range", ExprTreeRange())
        defFunc("aaf", ExprTreeAaf())
        defFunc("sqrt", ExprTreeSqrt())
        defFunc("exp", ExprTreeExp())
        defFunc("log", ExprTreeLog())
    }
}