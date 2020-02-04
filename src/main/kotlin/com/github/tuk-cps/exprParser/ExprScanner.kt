package exprParser

import java.io.IOException
import java.io.Reader
import java.io.StreamTokenizer
import java.io.StringReader

/**
 * @class ExprScanner
 * The scanner for the AADD Expressions.
 * It is based on the Java standard class StreamTokenizer.
 * It is configured to read from a string, not a file.
 */
open class ExprScanner: SymbolTable() {
    private var strtok: StreamTokenizer? = null
    protected var token = 0
    protected var nval = 0.0
    protected var sval: String? = null

    protected fun setup(expr_string: String) {
        val r: Reader = StringReader(expr_string)
        strtok = StreamTokenizer(r)
        strtok!!.resetSyntax()
        strtok!!.lowerCaseMode(true)
        strtok!!.parseNumbers()
        strtok!!.wordChars('a'.toInt(), 'z'.toInt())
        strtok!!.wordChars('A'.toInt(), 'Z'.toInt())
        strtok!!.wordChars('0'.toInt(), '9'.toInt())
        strtok!!.wordChars('.'.toInt(), '.'.toInt())
        strtok!!.whitespaceChars(' '.toInt(), ' '.toInt())
        strtok!!.whitespaceChars('\t'.toInt(), '\t'.toInt())
        strtok!!.slashSlashComments(false)
        strtok!!.slashStarComments(false)
        strtok!!.ordinaryChar('-'.toInt()) // otherwise part of wordChars!
        ExprTree.symTab = this
        nextToken()
    }

    @Throws(ParseError::class)
    protected fun nextToken(): Int {
        try {
            token = strtok!!.nextToken()
            when (token) {
                StreamTokenizer.TT_EOF, StreamTokenizer.TT_EOL -> token = EOL
                StreamTokenizer.TT_NUMBER -> {
                    // we don't represent negative numbers here. Instead, we split it in
                    // one token '-' and one VALUE token with the positive value.
                    nval = strtok!!.nval
                    token = DOUBLE
                    if (nval < 0) {
                        token = '-'.toInt()
                        strtok!!.nval = -nval
                        strtok!!.pushBack()
                    }
                }
                StreamTokenizer.TT_WORD -> {
                    sval = strtok!!.sval
                    token = ID
                    when (sval) {
                        "var" -> token = VAR
                        "val" -> token = VAL
                        "fun" -> token = FUN
                        "eqn" -> token = EQN
                    }
                }
                '('.toInt(), ')'.toInt() , '+'.toInt(), '-'.toInt(), '*'.toInt(), '/'.toInt(), '&'.toInt(), '|'.toInt() -> { }
                ':'.toInt() -> if (strtok!!.nextToken() == '='.toInt()) token = ASS
                '='.toInt() -> if (strtok!!.nextToken() == '='.toInt()) token = EE else strtok!!.pushBack()
                ','.toInt() -> { }
                '>'.toInt() -> if (strtok!!.nextToken() == '='.toInt()) token = GE else strtok!!.pushBack()
                '<'.toInt() -> if (strtok!!.nextToken() == '='.toInt()) token = LE else strtok!!.pushBack()
                else -> throw ParseError("unsupported token:$this")
            }
            // System.out.println("  Read token: " + this + ", "); // useful for debugging:
        } catch (e: IOException) {
            throw ParseError("unsupported symbol in expression string: " + token.toChar())
        }
        return token
    }

    /**
     * Checks for an expected token and reads next if found.
     * For start of production if expected token is found.
     */
    protected var lastToken = 0

    @Throws(ParseError::class)
    protected fun nextTokenIs(vararg options: Int): Boolean {
        for (t in options) {
            if (token == t) {
                lastToken = t
                nextToken()
                return true
            }
        }
        return false
    }

    /**
     * @method nextToken
     * Checks for an expected token.
     * If the token is not there, it is a syntax error.
     */
    @Throws(ParseError::class)
    protected fun nextToken(expected: Int) {
        if (token == expected) {
            nextToken()
        } else {
            throw ParseError("expected token: " + expected.toChar())
        }
    }

    /**
     * @method toString
     * @return String
     */
    override fun toString(): String {
        return when (token) {
            DOUBLE -> "DOUBLE: " + strtok!!.nval
            ID     -> "ID: " + strtok!!.sval
            GE     -> ">="
            LE     -> "<="
            ASS    -> ":="
            EE     -> "=="
            VAR    -> "VAR"
            VAL    -> "VAL"
            FUN    -> "FUN"
            EQN    -> "EQN"
            EOL    -> "<EOL/EOF>"
            else   -> "" + token.toChar()
        }
    }

    internal companion object {
        const val DOUBLE = -100
        const val ID = -101
        const val VAR = -102
        const val FUN = -103
        const val EQN = -104
        const val VAL = -105
        const val EE = -107
        const val ASS = -108
        const val GE = -110
        const val LE = -111
        const val EOL = -112
    }
}