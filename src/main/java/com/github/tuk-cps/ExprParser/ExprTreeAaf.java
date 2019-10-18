package ExprParser;

import jAADD.AADD;
import static jAADD.AADD.REAL;

/**
 * Predefined function aaf that generates a new Affine Form with min, max, and the
 * key of a new noise symbol as parameters.
 */
class ExprTreeAaf extends ExprTreeFunction {
    ExprTreeAaf() {
        super(REAL, "aaf",
                new ExprTree("min", REAL),
                new ExprTree("max", REAL),
                new ExprTree("key", REAL));
    }

    void evalThis() throws ExprError {
        if (param.size() != 3) throw new ExprError("aaf requires 3 parameters: min, max, symbol no.");
        double l = getAADDParam(0).Value().getMin();
        double r = getAADDParam(1).Value().getMax();
        result = new AADD(l, r, (int) Math.round(getAADDParam(2).Value().getCentral()));
    }
}
