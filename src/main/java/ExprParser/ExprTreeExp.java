package ExprParser;

import static jAADD.AADDMgr.REAL;

/**
 * Predefined functions: exp
 */
class ExprTreeExp extends ExprTreeFunction {
    ExprTreeExp() { super(REAL, "exp", new ExprTree("x", REAL)); }
    void evalThis() throws ExprError {
        result = getAADDParam(0).exp();
    }
}
