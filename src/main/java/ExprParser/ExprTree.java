package ExprParser;

import jAADD.AADD;
import jAADD.BDD;
import jAADD.DD;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * The class ExpressionTree implements an attributed syntax tree (AST).
 *
 * The base class of the expression tree is a simple value,
 * represented by result: a constant literal or a variable.
 * For variables, there is an id (name); for literals the result is its value.
 */
public class ExprTree {
    protected DD result;
    protected String id;
    protected static SymbolTable symTab;

    ExprTree()                  { result = null; id = "uninitialized"; }
    ExprTree(DD val)            { result = val;  id = ""; }
    ExprTree(String id, DD val) { setResult(val); setId(id); }
    ExprTree copy()             { return new ExprTree(getId(), getResult()); }


    public DD           getResult()                 { return result; }
    public void         setResult(DD v)             { result = v; }
    public String       getId()                     { return id; }
    public void         setId(String id)            { this.id = new String(id); }
    public static SymbolTable getSymTab()           { return symTab; }
    public static void  setSymTab(SymbolTable s)    { symTab = s; }

    AADD getAADD() throws ExprError {
        if (this.getResult() instanceof AADD) return (AADD) getResult();
        else throw new ExprError("wrong type. Expect REAL.");
    }

    BDD getBDD() throws ExprError {
        if (this.getResult() instanceof BDD) return (BDD) getResult();
        else throw new ExprError("wrong type. Expect BOOL.");
    }

    void evalThis() throws ExprError {
        if (id.equals("")) return;        // a literal.
        ExprTree v = symTab.getVar(id);
        result = v.result;
    }

    /**
     * This method computes the expression from the leavese to the root.
     * @throws ExprError
     */
    void evalExpr() throws ExprError { evalThis(); }

    /**
     * This method computes bi-directional constraint-net.
     * @throws ExprError
     */
    void propagateExpr() throws ExprError {

    }

    @Override
    public String toString() {
        if (id == null ||id.isBlank()) return ""+getResult();
        else return "("+id+": "+getResult()+")";
    }
}


