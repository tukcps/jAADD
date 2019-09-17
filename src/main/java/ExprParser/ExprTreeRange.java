package ExprParser;

import jAADD.AADD;
import static jAADD.AADDMgr.REAL;

/**
 * Predefined functions: range, uses AADD with new noise symbol.
 */
class ExprTreeRange extends ExprTreeFunction {
    ExprTreeRange() {
        super(REAL, "range",
                new ExprTree("min", REAL),
                new ExprTree("max", REAL));
    }
    void evalThis() throws ExprError {
        if (param.size() != 2) throw new ExprError("range expects 2 parameters");
        double l = getAADDParam(0).Value().getMin();
        double r = getAADDParam(1).Value().getMax();
        result = new AADD(l, r, -1);
    }
}
