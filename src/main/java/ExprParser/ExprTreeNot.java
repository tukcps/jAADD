package ExprParser;

/**
 * Predefined functions: not
 */
class ExprTreeNot extends ExprTreeFunction {
    ExprTreeNot() { super(jAADD.AADDMgr.BOOL, "not", new ExprTree("x", jAADD.AADDMgr.BOOL)); }
    void evalThis() throws ExprError {
        if (param.size() != 1) throw new ExprError("not expects 1 parameter");
        result = getBDDParam(0).negate();
    }
}
