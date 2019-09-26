package ExprParser;

/**
 * A binary operation.
 */
class ExprTreeBinOp extends ExprTree {
    ExprTree l;
    ExprTree r;
    int op;

    ExprTreeBinOp(ExprTree l, int op, ExprTree r)  {
        super(l.result);
        assert l != null && r != null: "Operands must not be null";
        this.l = l.copy(); this.op = op; this.r = r.copy();
    }

    ExprTree copy() {
        ExprTreeBinOp cp = new ExprTreeBinOp(this.l, this.op, this.r);
        cp.setId(this.getId());
        cp.setResult(this.getResult());
        return cp;
    }


    // Computes the complete tree recursively.
    void evalExpr() throws ExprError {
        l.evalExpr();
        r.evalExpr();
        evalThis();
        return;
    }


    // Just compute operation with two operands: l and r.
    void evalThis() throws ExprError {
        switch(op) {
            case '+': result = l.getAADD().add(r.getAADD()); break;
            case '-': result = l.getAADD().sub(r.getAADD()); break;
            case '*': result = l.getAADD().mult(r.getAADD()); break;
            case '/': result = l.getAADD().div(r.getAADD()); break;
            case '>': result = l.getAADD().Gt(r.getAADD()); break;
            case '<': result = l.getAADD().Lt(r.getAADD()); break;
            case ExprScanner.GE: result = l.getAADD().Ge(r.getAADD()); break;
            case ExprScanner.LE: result = l.getAADD().Le(r.getAADD()); break;
            case '&': result = l.getBDD().and(r.getBDD()); break;
            case '|': result = l.getBDD().or(r.getBDD()); break;
            case '=': throw new ExprError("Equality of two AADD is not supported.");
            default:  throw new ExprError("Operation "+ (char) op + " resp. " + op + " not supported on AADD.");
        }
    }


    public String toString() {
        return "(" + l.toString()+(char) op +r.toString() + ")";
    }
}
