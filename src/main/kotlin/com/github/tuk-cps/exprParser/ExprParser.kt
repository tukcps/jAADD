package exprParser

import jAADD.AADD
import jAADD.AADD.Companion.scalar
import jAADD.BDD
import jAADD.DD
import java.util.*


/**
 * This class provides a parser for simple expressions
 * that are equivalent to operations on AADD/BDD.
 * They are used to model dependencies between a part's
 * property and its part's properties.
 *
 * It returns an AADD with the value for expressions that can be evaluated,
 * and otherwise an attributes syntax tree.
 *
 * Lexical elements:<br>
 *
 * - Keywords "var" "eqn"
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
 *           | "fun" NAME Params ":=" CExpr
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
class ExprParser(): ExprScanner() {

    var AST: ExprTree? = null     // holds the attributed syntax tree
        get()                        { return field}
        internal set(ast: ExprTree?) { field = ast}

    /** Sets an expression string, parses it, but does not evaluate it. */
    var expr: String = ""
        set(value) {super.setup(value); AST=Stmt()}

    /**
     * Evaluates a given String with the defined variables.
     * @param expr_str String that is parsed.
     * @return The AADD result from the evaluated syntax tree.
     * @throws ParseError
     * @throws ExprError
     */
    @Throws(ParseError::class, ExprError::class)
    fun evalAADD(expr_str: String): AADD { evalExpr(expr_str); return AST!!.aadd }

    /**
     * Evaluates a given String with the defined variables.
     * It returns the BDD result from the expression tree.
     */
    @Throws(ParseError::class, ExprError::class)
    fun evalBDD(expr_str: String): BDD { evalExpr(expr_str); return AST!!.bdd }

    /**
     * Evaluates a given String with the defined variables.
     * It parses the parameter string, creates an AST and returns the value as DD.
     * The DD can be either a BDD or AADD.
     */
    @Throws(ParseError::class, ExprError::class)
    fun eval(expr_str: String): DD<*>? {
        evalExpr(expr_str)
        return AST?.value
    }

    /**
     * Re-evaluates the expression with the defined variables.
     * It returns the result from the expression tree as an AADD.
     */
    @Throws(ExprError::class)
    fun evalAADD(): AADD {
        if (AST == null) throw ExprError("Expr not evaluated")
        AST?.evalUpRec()
        return AST!!.aadd
    }


    // Re-evaluates the  expression with the defined variables.
    // It returns the BDD result from the expression tree.
    @Throws(ParseError::class, ExprError::class)
    fun evalBDD(): BDD {
        if (AST == null) throw ExprError("Expr not evaluated")
        AST!!.evalUpRec()
        return AST!!.bdd
    }


    /**
     * Parses and evaluates a given String with the defined variables.
     * @param expr_str A string to be parsed.
     * @return The syntax tree with symbol table.
     */
    @Throws(ParseError::class, ExprError::class)
    fun evalExpr(expr_str: String): ExprTree {
        super.setup(expr_str)
        AST = Stmt()
        AST?.evalUpRec()
        return AST!!
    }


    /**
     * stmt :-  VAR NAME ":=" CExpr
     *        | FUN NAME Params "=" CExpr
     *        | VAL NAME "==" CExpr
     *        | CExpr
     */
    @Throws(ParseError::class, ExprError::class)
    private fun Stmt(): ExprTree {
        var t: ExprTree?
        when (token)
        {
            VAR -> {
                nextToken()
                val n = sval!!
                nextToken(ID)
                nextToken(ASS)
                t = CExpr()
                t.evalUpRec()
                defVar(n, t)
                return t
            }
            FUN -> {
                nextToken()
                val n = sval!!
                nextToken(ID)
                val params = Params()
                defFunc(n, params!!)
                nextToken(ASS)
                t = CExpr()
                defFuncBody(n, t)
                return t
            }
            EQN -> {
                nextToken()
                val n = sval!!
                nextToken(ID)
                nextToken(EE)
                t = CExpr()
                return ExprTreeConstrNet(getVar(n), t)
            }
            else -> {
                t = CExpr()
            }
        }
        if (token != EOL) throw ParseError("expected Statement")
        return t
    }

    /**
     * Cond :- Sum [ relop Sum]
     */
    @Throws(ParseError::class, ExprError::class)
    private fun CExpr(): ExprTree {
        var result = Sum()
        if (nextTokenIs('>'.toInt(), '<'.toInt(), '='.toInt(), GE, LE)) {
            val op = lastToken
            val t2 = Sum()
            result = ExprTreeBinOp(result, op, t2) // getting an error here
        }
        return result
    }

    /**
     * Sum :- Product ( ("+"|"-"|"|") Product )*
     */
    @Throws(ParseError::class, ExprError::class)
    private fun Sum(): ExprTree {
        var result = Product()
        while (nextTokenIs('+'.toInt(), '-'.toInt(), '|'.toInt())) {
            val op = lastToken
            val t2 = Product()
            result = ExprTreeBinOp(result, op, t2)
        }
        return result
    }

    /** Product :- Value ( ("*"|"/"|"&") Value )* */
    @Throws(ParseError::class, ExprError::class)
    private fun Product(): ExprTree {
        var result = Value()
        while (nextTokenIs('*'.toInt(), '/'.toInt(), '&'.toInt())) {
            val op = lastToken
            val f2 = Value()
            result = ExprTreeBinOp(result, op, f2)
        }
        return result
    }

    /**
     *     Params   :- "(" CExpr ("," CExpr)* ")"
     *              |  // nothing.
     */
    @Throws(ParseError::class, ExprError::class)
    fun Params(): ArrayList<ExprTree>? {
        val parameters = ArrayList<ExprTree>()
        if (nextTokenIs('('.toInt())) {
            var e = CExpr()
            parameters.add(e)
            while (nextTokenIs(','.toInt())) {
                e = CExpr()
                parameters.add(e)
            }
            nextToken(')'.toInt())
            return parameters
        }
        return null
    }

    /**
     * Value :-    DOUBLE | TRUE | FALSE
     * |  '(' ITE ("," ITE)* ')'
     * |  ID that is a constant or variable.
     * |  ID '(' ITE ("," ITE)* ')' where word is a function on AADD.
     */
    @Throws(ParseError::class, ExprError::class)
    fun Value(): ExprTree {
        val value: ExprTree                // Value
        var neg = nextTokenIs('-'.toInt()) // Negative Value.
        when (token) {
            DOUBLE -> {
                value = ExprTree(scalar(nval),"")
                nextToken()
            }
            '('.toInt() -> {
                nextToken()
                value = CExpr()
                nextToken(')'.toInt())
            }
            ID -> {
                val n = sval!!
                nextToken()
                val params = Params()
                value = if (params==null) getVar(n) else getFuncCall(n, params)
            }
            else -> throw ParseError("expect value, but read: " + token.toChar() + " resp. " + token)
        }
        return if (neg) ExprTreeBinOp(ExprTree(scalar(0.0), "0.0"), '-'.toInt(), value) else value
    }
}