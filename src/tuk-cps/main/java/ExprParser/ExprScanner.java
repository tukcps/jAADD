package ExprParser;

import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;

/**
 * The scanner for the AADD Expressions.
 * It is based on the Java standard class StreamTokenizer.
 * It is configured to read from a string, not a file.
 */
class ExprScanner extends SymbolTable {
    final static int DOUBLE = -100;
    final static int ID     = -101;
    final static int VAR    = -102;
    final static int FUNC   = -103;
    final static int EQN    = -104;
    final static int EE     = -107;
    final static int ASS    = -108;
    final static int GE     = -110;
    final static int LE     = -111;
    final static int EOL    = -112;

    private StreamTokenizer strtok;
    int    token;
    double nval;
    String sval;

    ExprScanner() throws ExprError { }

    public void setup(String expr_string) {
        Reader r = new StringReader(expr_string);
        strtok   = new StreamTokenizer(r);
        strtok.resetSyntax();
        strtok.lowerCaseMode(true);
        strtok.parseNumbers();
        strtok.wordChars('a', 'z');
        strtok.wordChars('A', 'Z');
        strtok.wordChars('0', '9');
        strtok.wordChars('.', '.');
        strtok.whitespaceChars(' ', ' ');
        strtok.whitespaceChars('\t', '\t');
        strtok.slashSlashComments(false);
        strtok.slashStarComments(false);
        strtok.ordinaryChar('-'); // otherwise part of wordChars!
        ExprTree.setSymTab(this);
    }

    protected int nextToken() throws ParseError {
        try {
            token = strtok.nextToken();
            switch (token) {
                case StreamTokenizer.TT_EOF:
                case StreamTokenizer.TT_EOL:
                    token = EOL;
                    break;

                case StreamTokenizer.TT_NUMBER:
                    // we don't represent negative numbers here. Instead, we split it in
                    // one token '-' and one VALUE token with the positive value.
                    nval = strtok.nval;
                    token = DOUBLE;
                    if (nval < 0) {
                        token = '-';
                        strtok.nval = -nval;
                        strtok.pushBack();
                    }
                    break;

                case StreamTokenizer.TT_WORD:
                    sval = strtok.sval;
                    token = ID;
                    if (sval.equals("var")) {
                        token = VAR;
                    } else if (sval.equals("func")) {
                        token = FUNC;
                    } else if (sval.equals("eqn")) {
                        token = EQN;
                    }
                    break;

                case '(':
                case ')':
                case '+':
                case '-':
                case '*':
                case '/':
                case '&':
                case '|':
                    break;
                case ':':
                    if ( strtok.nextToken() == '=' ) token = ASS;
                    break;
                case '=':
                    if ( strtok.nextToken() == '=' ) token = EE;
                    else strtok.pushBack();
                    break;

                case ',':
                    // Accept it.
                    break;

                case '>':
                    if ( strtok.nextToken() == '=' ) token = GE;
                    else strtok.pushBack();
                    break;

                case '<':
                    if ( strtok.nextToken() == '=' ) token = LE;
                    else strtok.pushBack();
                    break;

                default:
                    throw new ParseError("unsupported token:" + this);
            }
            // useful for debugging:
            // System.out.println("  Read token: " + this + ", ");
        }
        catch (IOException e) {
            throw new ParseError("unsupported symbol in expression string: "+(char) token);
        }
        return token;
    }

    // Checks for an expected token and reads next if found.
    // For start of production if expected token is found.
    protected int lastToken;
    protected boolean nextTokenIs(int... options) throws ParseError {
        for (int t: options)
        {
            if (token == t) {
                lastToken=t;
                nextToken();
                return true;
            }
        }
        return false;
    }

    // Checks for an expected token.
    // If the token is not there, it is a syntax error.
    protected void nextToken(int expected) throws ParseError {
        if (token == expected) {
            nextToken();
        } else {
            throw new ParseError("expected token: "+ (char) expected);
        }
    }

    public String toString() {
        switch (token) {
            case DOUBLE: return "DOUBLE: " + strtok.nval;
            case ID:     return "ID: " + strtok.sval;
            case GE:     return ">=";
            case LE:     return "<=";
            case ASS:    return ":=";
            case EE:     return "==";
            case VAR:    return "VAR";
            case FUNC:   return "FUNC";
            case EOL:    return "<EOL/EOF>";
            default:     return "" + (char) token;
        }
    }
}
