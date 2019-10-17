package ExprParser;

import jAADD.AADD;
import static jAADD.AADDMgr.REAL;

/**
 * Predefined functions: sqrt
 */
class ExprTreeSqrt extends ExprTreeFunction {
    ExprTreeSqrt()  { super(REAL, "sqrt", new ExprTree("x", REAL)); }
    void evalThis() throws ExprError {
        result = getAADDParam(0).sqrt();
    }
}
