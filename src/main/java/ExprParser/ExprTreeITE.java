package ExprParser;

import jAADD.AADD;
import jAADD.BDD;
import static jAADD.AADDMgr.BOOL;
import static jAADD.AADDMgr.ONE;
import static jAADD.AADDMgr.ZERO;
import static jAADD.AADDMgr.REAL;

/**
 * The ITE function.
 */
class ExprTreeITE extends ExprTreeFunction {

    ExprTreeITE()  throws ExprError {
        super(REAL, "ite", new ExprTree("i", BOOL), new ExprTree("t", REAL), new ExprTree("e", REAL));
    }

    // ITE function
    @Override
    void evalThis() throws ExprError {
        assert param.size() == 3: "3 parameters expected";
        if (getParam(1).getResult() instanceof BDD)
            result = getBDDParam(0).ITE(getBDDParam(1), getBDDParam(2));
        else
            result = getBDDParam(0).ITE(getAADDParam(1), getAADDParam(2));
    }

    // ITE parameters with lazy evaluation.
    @Override
    void evalExpr() throws ExprError {
        getSymTab().scopes.push(this);

        getParam(0).evalExpr(); // Evaluate i
        BDD i=getBDDParam(0);   //
        if (i == ONE ) { getParam(1).evalExpr();}
        else if (i == ZERO) { getParam(2).evalExpr(); }
        else {
            getParam(1).evalExpr();
            getParam(2).evalExpr();
        }

        // now do ITE function.
        evalThis();

        getSymTab().scopes.pop();
    }
}
