package ExprParser;

import static jAADD.AADD.REAL;

/**
 * Predefined functions: sqrt
 */
class ExprTreeSqrt extends ExprTreeFunction {
    ExprTreeSqrt()  { super(REAL, "sqrt", new ExprTree("x", REAL)); }
    void evalThis() throws ExprError {
        result = getAADDParam(0).sqrt();
    }
}
