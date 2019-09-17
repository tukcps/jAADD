package ExprParser;

public class ExprError extends Exception {
    ExprError(String msg) {
        super("Error in Expression: "+msg);
    }
}