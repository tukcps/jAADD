package ExprParser;

import jAADD.*;

import java.util.ArrayList;

/**
 * This class provides a parser for simple expressions
 * that are equivalent to operations on AADD/BDD.
 * They are used to model dependencies between a part's
 * property and his part's properties.
 *
 * It returns an AADD with the value for expressions that can be evaluated,
 * and otherwise an attributes syntax tree.
 *
 * Lexical elements:<br>
 *
 * - Keywords "if" "then" "else" "ite"<br>
 * - ID: ( a-zA-Z ("." a-zA-Z)*  )<br>
 *   (Keywords and ID are case insensitive, converted to lower case)<br>
 * - DOUBLE: as in Java StreamTokenizer, but without sign.<br>
 * - Operators {@code + - * / & | > < = LE GE}<br>
 *
 * The parser uses the recursive descent method.
 * It recognizes the following language:
 * <pre>
 *{@code
 *
 * Stmt    :-  "var"  NAME ":=" CExpr
 *           | "func" NAME Params ":=" CExpr
 *           | "eqn"  NAME "==" CExpr
 *           | CExpr
 *
 * CExpr   :- Expr  [ (">", "<", "=", LE, GE) Expr ]
 *
 * Sum     :- Product (("+"|"-"|"|") Product)*
 *
 * Product :- Value (("*"|"/"|"&") Value)*
 *
 * Value   :- ["-"]
 *              (   FLOAT
 *                | "(" CExpr ")"
 *                | NAME [Params]
 *              )
 *
 * Params  :- "(" CExpr ("," CExpr)* ")"
 *
 * }
 * </pre>
 * Examples are in the unit test.
 */
public class ExprParser extends ExprScanner {

    public ExprTree AST;

    public ExprParser() throws ExprError {
    }

    public ExprTree setExpr(String expr) throws ParseError, ExprError {
        super.setup(expr);
        parse();
        return AST;
    }

    // Starts parsing and returns the root of the AST.
    private void parse() throws ParseError, ExprError {
        nextToken();
        AST = Stmt();

        if (token != EOL)  throw new ParseError("expected Statement");
    }


    // Evaluates a given String with the defined variables.
    // It returns an expression tree.
    public ExprTree evalExpr(String expr_str) throws ParseError, ExprError {
        super.setup(expr_str);
        parse();
        AST.evalExpr();
        return AST;
    }


    // Evaluates a given String with the defined variables.
    // It returns the AADD result from the expression tree.
    public AADD evalAADD(String expr_str) throws ParseError, ExprError {
        evalExpr(expr_str);
        return AST.getAADD();
    }


    // Evaluates a given String with the defined variables.
    // It returns the BDD result from the expression tree.
    public BDD evalBDD(String expr_str) throws ParseError, ExprError {
        evalExpr(expr_str);
        return AST.getBDD();
    }


    // Re-evaluates the  expression with the defined variables.
    // It returns the AADD result from the expression tree.
    public AADD evalAADD() throws ExprError {
        AST.evalExpr();
        return AST.getAADD();
    }


    // Re-evaluates the  expression with the defined variables.
    // It returns the BDD result from the expression tree.
    public BDD evalBDD() throws ParseError, ExprError {
        if (AST==null) throw new ExprError("Expr not evaluated");
        AST.evalExpr();
        return AST.getBDD();
    }


    // Re-evaluates the  expression with the defined variables.
    // It returns the BDD result from the expression tree.
    public DD evalDD() throws ParseError, ExprError {
        if (AST==null) throw new ExprError("Expr not evaluated");
        AST.evalExpr();
        return AST.getResult();
    }


    // stmt :-  LET NAME "=" CExpr
    //        | FUNC NAME Params "=" CExpr
    private ExprTree Stmt() throws ParseError, ExprError {
        if (nextTokenIs(VAR)) {
            String n = sval;
            nextToken(ID);
            nextToken(ASS);
            ExprTree t = CExpr();
            t.evalExpr();
            defVar(n, t);
            return t;
        }
        if (nextTokenIs(FUNC)) {
            String n = sval;
            nextToken(ID);
            ArrayList<ExprTree> params = Params();
            defFunc(n, params);
            nextToken(ASS);
            ExprTree t = CExpr();
            defFuncBody(n, t);
            return t;
        }
        return CExpr();
    }


    // Cond :- Sum [ relop Sum]
    private ExprTree CExpr() throws ParseError, ExprError {
        /* Deprecated
        if (nextTokenIs(IF)) {
            LinkedList<ExprTree> param = new LinkedList<ExprTree>();
            param.add(CExpr());
            nextToken(THEN);
            param.add(CExpr());
            nextToken(ELSE);
            param.add(CExpr());
            return setFuncParams("ite", param);
        } */

        ExprTree result = Sum();
        if ( nextTokenIs('>', '<', '=', GE, LE) ) {
            int op = lastToken;
            ExprTree t2 = Sum();
            result = new ExprTreeBinOp(result, op, t2);
        }
        return result;
    }

    // Sum :- Product ( ("+"|"-"|"|") Product )*
    ExprTree Sum() throws ParseError, ExprError {
        ExprTree result = Product();

        while (nextTokenIs('+', '-', '|') ) {
            int op = lastToken;
            ExprTree t2 = Product();
            result = new ExprTreeBinOp(result, op, t2);
        }
        return result;
    }

    // Product  :- Sum ( ("*"|"/"|"&") Sum )*
    ExprTree Product() throws ParseError, ExprError {
        ExprTree result = Value();

        while (nextTokenIs('*', '/', '&')) {
            int op = lastToken;
            ExprTree f2 = Value();
            result = new ExprTreeBinOp(result, op, f2);
        }
        return result;
    }

    // Params   :- "(" CExpr ("," CExpr)* ")"
    ArrayList<ExprTree> Params() throws ParseError, ExprError {
        if (nextTokenIs('(')) {
            ArrayList<ExprTree> parameters = new ArrayList<ExprTree>();
            ExprTree e = CExpr();
            parameters.add(e);
            while (nextTokenIs(',')) {
                e = CExpr();
                parameters.add(e);
            }
            nextToken(')');
            return parameters;
        }
        return null;
    }

    // Recognizes
    // Value :-    DOUBLE | TRUE | FALSE
    //         |  '(' ITE ("," ITE)* ')'
    //         |  ID that is a constant or variable.
    //         |  ID '(' ITE ("," ITE)* ')' where word is a function on AADD.
    ExprTree Value() throws ParseError, ExprError {
        ExprTree val;   // Value
        boolean neg = false;    // optional sign.

        neg = nextTokenIs('-'); // Negative Value.

        switch(token) {
            case DOUBLE:
                val = new ExprTree(new AADD(nval));
                nextToken();
                break;

            case '(':
                nextToken();
                val = CExpr();
                nextToken(')');
                break;

            case ID:
                String n = sval;
                nextToken();
                ArrayList<ExprTree> params = Params();
                if (params == null)
                    val = getVar(n);              // Variable from symbol table
                else
                    val = getFuncCall(n, params); // Function call; consider params in scope.
                break;

            default:
                throw new ParseError("expect value, but read: "+(char) token+" resp. "+ token);
        }
        if (neg == true)
            return new ExprTreeBinOp(new ExprTree(new AADD(0)), '-', val);
        else
            return val;
    }
}


