package ExprParser;

import jAADD.AADD;
import jAADD.BDD;
import jAADD.DD;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * A function call of a user-defined function.
 * It defines a local scope by mapping the parameters to the variables used during declaration of the function.
 * It searches for variables only in the local scope.
 */
class ExprTreeFunction extends ExprTree {
    protected ArrayList<ExprTree> param;
    protected  ExprTree   ast;

    ExprTree getAst()             { return ast; }
    void     setAst(ExprTree ast) { if (ast!=null) this.ast = ast.copy(); }

    ExprTreeFunction()  { super(); param = new ArrayList<>(); }
    ExprTreeFunction(DD val, String name, ExprTree ... param)  {
        super(name, val);
        this.ast= null;
        this.param = new ArrayList<ExprTree>(Arrays.asList(param));
    }

    @Override
    ExprTree copy() {
        try {
            ExprTreeFunction cp = getClass().getDeclaredConstructor().newInstance();
            cp.setId(getId());
            cp.setResult(getResult());
            cp.setAst(getAst());
            cp.param = new ArrayList<>(param);
            for (int i = 0; i < param.size(); i++)
                cp.param.set(i, param.get(i).copy());
            return cp;
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Processes a user-defined function call.
     * First, gets the function from the symbol table.
     * @throws ExprError
     */
    @Override
    void evalThis() throws ExprError {
        assert id != null;
        assert ast != null : "AST of function call must not be null";
        // throw new ExprError("function "+id+" not defined."); // function not defined
        // ast.evalExpr();
        // ast = getSymTab().getFuncCall(id, param);
        ast.evalExpr();
        result = ast.result;
    }


    // Computes the complete tree recursively.
    @Override
    void evalExpr() throws ExprError {
        assert param != null: "expected parameters are null";
        getSymTab().scopes.push(this);

        for (ExprTree par: param) { par.evalExpr(); }
        evalThis();
        getSymTab().scopes.pop();
    }


    ExprTree getParam(int nr) throws ExprError {
        if (nr >= param.size()) throw new ExprError("not enough parameters");
        return param.get(nr);
    }


    BDD getBDDParam(int nr) throws ExprError {
        if (nr >= param.size()) throw new ExprError("not enough parameters");
        return param.get(nr).getBDD();
    }


    AADD getAADDParam(int nr) throws ExprError {
        if (nr >= param.size()) throw new ExprError("not enough parameters");
        return param.get(nr).getAADD();
    }


    @Override
    public String toString() {
        try {
            String s = "( "+ getResult() + " "+ id;
            if (param != null)
                for (ExprTree par : param) {
                    s += par + " ";
                }
            if (ast != null)
                s += " -> " + ast;
            else
                s += " -> builtin ";
            s+=")";
            return s;
        } catch  (Exception e) {
            return "Error printing function call";
        }
    }
}
