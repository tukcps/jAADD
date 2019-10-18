package ExprParser;

import static jAADD.AADD.REAL;

/**
 * Predefined functions: log
 */
class ExprTreeLog extends ExprTreeFunction {
    ExprTreeLog()  { super(REAL, "log", new ExprTree("x", REAL)); }
    void evalThis() throws ExprError {
        result = getAADDParam(0).log();
    }
}
