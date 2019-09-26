package ExprParser;

import jAADD.AADD;
import jAADD.AffineForm;
import static jAADD.AADDMgr.BOOL;
import jAADD.DD;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Stack;

import static jAADD.AADDMgr.ONE;
import static jAADD.AADDMgr.ZERO;

/**
 * A simple symbol table:
 *  - a hash map for globel symbols.
 *  - a stack of locals in the parameters of function calls.
 */
class SymbolTable {

    // Global (HashMap) and local variables (Stack)
    private HashMap<String, ExprTree> globals;
    Stack<ExprTreeFunction> scopes;

    SymbolTable() throws ExprError {
        globals = new HashMap<>();
        scopes = new Stack<>();
        defVar("real", new AADD(AffineForm.INFINITE));
        defVar("bool", BOOL);
        defVar("pi", new AADD(Math.PI));
        defVar("e", new AADD(Math.E));
        defVar("true", new ExprTree(ONE));
        defVar("false", new ExprTree(ZERO));
        defFunc("not", new ExprTreeNot());
        defFunc("ite", new ExprTreeITE());
        defFunc("range", new ExprTreeRange());
        defFunc("aaf", new ExprTreeAaf());
        defFunc("sqrt", new ExprTreeSqrt());
        defFunc("exp", new ExprTreeExp());
        defFunc("log", new ExprTreeLog());
    }


    public void defVar(String name, ExprTree tree) {
        assert tree != null : "Definition of variable must not be null";
        defVar(name, tree.getResult());
    }


    /**
     * Defines a variable of type AADD, initialized with a given AADD.
     *
     * @param name
     * @param value
     */
    public void defVar(String name, DD value) {
        assert value != null : "Definition of variable must not be null";
        globals.put(name, new ExprTree(name, value));
    }


    /**
     * Adds a function, represented by an expression tree, to the entry in the
     * table. Parameters are added when calling.
     *
     * @param name The name of the function.
     * @param func An ExprTree node that processes the function.
     */
    public void defFunc(String name, ExprTreeFunction func) throws ExprError {
        assert name != null : "name must not be null";
        assert func != null : "function must be given";
        globals.put(name, func);
    }


    /**
     * For a user-defined function, it sets the parameters in the symbol table.
     *
     * @param name  name of the function in symbol table.
     * @param param list of parameters.
     * @throws ExprError
     */
    public void defFunc(String name, ExprTree... param) throws ExprError {
        assert name != null : "name must not be null";
        ExprTreeFunction call = new ExprTreeFunction();
        call.id = name;
        call.param = new ArrayList<ExprTree>(Arrays.asList(param));
        globals.put(name, call);
    }


    /**
     * For a user-defined function, it sets the parameters in the symbol table.
     *
     * @param name  name of the function in symbol table.
     * @param param list of parameters.
     * @throws ExprError
     */
    public void defFunc(String name, ArrayList<ExprTree> param) throws ExprError {
        assert name != null : "name must not be null";
        ExprTreeFunction call = new ExprTreeFunction();
        call.id = name;
        call.param = param;
        globals.put(name, call);
    }


    /**
     * For a user-defined function, sets the expr. tree in the object from the symbol table.
     * @param name the name of the function
     * @param ast the expression tree representing the function
     * @return
     * @throws ExprError
     */
    public ExprTreeFunction defFuncBody(String name, ExprTree ast) throws ExprError {
        assert name != null && ast != null: "name or ast must not be null";
        ExprTree r = globals.get(name);
        if (r == null || !(r instanceof ExprTreeFunction))
            throw new ExprError("identifier " + name + " not a declared function");
        ((ExprTreeFunction) r).setAst(ast);
        return (ExprTreeFunction) r;
    }


    /**
     * During execution, it gets the entry for a variable from the symbol table.
     * @param name name of a variable to be searched in the symbol table or the parameters of a function.
     * @return Expression tree.
     */
    public ExprTree getVar(String name) {

        // First search in params of last function call on call stack.
        if ( !scopes.empty() ) { // we are in function that is interpreted.
            ArrayList<ExprTree> params = scopes.peek().param;
            ExprTree found;
            for (ExprTree p : params) {
                if (p.id.equals(name)) {
                    return p.copy();
                }
            }
        }

        // Then search in globals.
        ExprTree r = globals.get(name);
        if (r == null) r = new ExprTree(name,new AADD(AffineForm.INFINITE) );

        return r.copy();
    }


    /**
     * This method builds the expression tree for a function call.
     * It sets the parameters of a function in the local symbol table.
     * @param name
     * @param parameters
     * @return The Expression tree .
     * @throws ExprError
     */
    public ExprTreeFunction getFuncCall(String name, ArrayList<ExprTree> parameters) throws ExprError {
        ExprTree dec = globals.get(name);

        if (dec == null || !(dec instanceof ExprTreeFunction))
            throw new ExprError("identifier " + name + " not a declared function");
        ExprTreeFunction fDec = (ExprTreeFunction) dec;

        if (fDec.param.size() != parameters.size())
            throw new ExprError("number of parameters for "+ name + " not matching declaration");

        ExprTreeFunction fCall = (ExprTreeFunction) fDec.copy();
        for (int i=0; i < fCall.param.size(); i++) {
            // Parameters from Call ...
            fCall.param.set(i, parameters.get(i));
            // Name from formal parameters.
            fCall.param.get(i).setId(fDec.param.get(i).getId() );
        }
        return fCall;
    }


    /**
     * For debugging useful ..
     * @return String that documents symbol table
     */
    public String SymbolTableInfo() {
        String s = "Symbol table: \n";
        for (String key : globals.keySet()) {
            s = "    " + s + key + ": " + globals.get(key) + "\n";
        }
        return s;
    }
}

