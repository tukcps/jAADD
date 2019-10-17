package ExprParser;


/**
 * This Exceptions is thrown for all errors that are caused in the parsing methods.
 * It just writes an error message.
 */
public class ParseError extends Exception {
    ParseError(String msg) {
        super("Parse Error: " + msg);
    }
}