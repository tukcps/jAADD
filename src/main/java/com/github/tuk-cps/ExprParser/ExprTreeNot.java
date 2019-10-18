package ExprParser;

import jAADD.BDD;

/**
 * Predefined functions: not
 */
class ExprTreeNot extends ExprTreeFunction {
    ExprTreeNot() { super(BDD.BOOL, "not", new ExprTree("x", BDD.BOOL)); }
    void evalThis() throws ExprError {
        if (param.size() != 1) throw new ExprError("not expects 1 parameter");
        result = getBDDParam(0).negate();
    }
}
