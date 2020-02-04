package jAADD;

import exprParser.ExprError;
import exprParser.ExprParser;
import exprParser.ParseError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExprParserTest {
    ExprParser p;
    AADD a;
    BDD b;

    @BeforeEach
    void setUp() {
        p = new ExprParser();
    }

    @Test
    void constantExprCheck() throws ExprError, ParseError {
        System.out.println("====  Checking ExpressionParser: -1+2*-3/(3-2)  =====");
        a = p.evalAADD(" -1+2*- 3/(3-2)");
        System.out.println("      AST: "+p.getAST());
        assertEquals(-7.0, a.getValue().getX0());
    }

    @Test
    void defVarCheck() throws ExprError, ParseError {
        System.out.println("====  Checking ExpressionParser: defVar, b-a  ====");
        p.defVar("a", AADD.scalar(1.0));
        p.defVar("b", AADD.scalar(2.0));
        p.defVar("c", AADD.scalar(3.0));
        p.defVar("d", AADD.scalar(4.0));

        a = p.evalAADD("b-a*c+d");
        assertEquals(2 - 1 * 3 + 4, a.getValue().getX0());
    }

    @Test
    void comparisonCheck() throws ExprError, ParseError {
        System.out.println("====  Checking ExpressionParser: (a>b) & true  ====");
        p = new ExprParser();
        p.defVar("a", AADD.scalar(1.0));
        p.defVar("b", AADD.scalar(2.0));
        b = p.evalBDD("(a > b) & false");
        assertEquals(false, b.getValue());
    }

    @Test
    void constantsCheck() throws ExprError, ParseError {
        System.out.println("====  Checking ExpressionParser: pi+e ====");
        p.setExpr("pi+e");
        a = p.evalAADD();
        assertEquals(Math.PI + Math.E, a.getValue().getX0());
    }

    @Test
    void ExpressionParserCheck() throws ExprError, ParseError {
        System.out.println("====  Checking re-computation   =====");
        p.setExpr("a+b*c");
        System.out.println("  AST is:     " + p.getAST());
        p.defVar("a", AADD.scalar(1));
        p.defVar("b", AADD.scalar(2));
        p.defVar("c", AADD.scalar(3));
        System.out.println("  AST is now: " + p.getAST());
        a = p.evalAADD();
        assertEquals(7, a.getValue().getX0());
        p.defVar("a", AADD.scalar(10));
        a = p.evalAADD();
        assertEquals(16, a.getValue().getX0());
    }

    @Test
    void CheckFunctionCalls() throws ExprError, ParseError {
        System.out.println(("====  Parser, function calls: not(true), not(FALSE)  ===="));
        p.setExpr("not(true)");
        System.out.println("      => " + p.evalBDD());
        assertEquals(false, p.evalBDD().getValue());
        p.setExpr("not(FALSE)");
        System.out.println("      => " + p.evalBDD());
        assertEquals(true, p.evalBDD().getValue());
    }

    @Test
    void ITECheck() throws ExprError, ParseError {
        System.out.println("====  Parser, function calls: ITE(a>b, a+b, ITE(c<5,d,pi)) ====");
        p.defVar("a", AADD.scalar(1));
        p.defVar("b", AADD.scalar(2));
        p.defVar("c", AADD.range(0, 100, -1));
        p.defVar("d", AADD.scalar(3));

        p.setExpr("ITE(true, 2, 1)");
        System.out.println("     ==> ITE(true, 2, 1)=" + p.evalAADD());

        assertEquals(2, p.evalAADD().getValue().getX0());

        p.setExpr("ITE(a>b, a+b, ITE(a>c, d, pi))");
        System.out.println(p.getAST());
        // System.out.println(p.SymbolTableInfo());

        AADD a = p.evalAADD();
        System.out.println("      AST: " + p.getAST());
        System.out.println("      =>   " + a);
        assertEquals(true, a.isInternal());

        p.setExpr("ITE(true, true, false)");
        assertEquals(p.evalBDD().getValue(), true);
    }

    /** Check: Range creates an intervall, and aaf creates affine forms */
    @Test void rangeTest() throws ExprError, ParseError {
        System.out.println(("====  Parser, function calls: range, aaf  ===="));
        p.setExpr("var a := range(1, 2)");
        p.setExpr("var b := range(1, 2)");
        p.setExpr("var c := a - b");
        // Intervall von -1 bis 1.
        System.out.println("      => c=range(1,2)-range(1,2)=" + p.evalAADD("c"));
        assertEquals(1, p.evalAADD().getValue().getRadius(), 0.0001);

        p.setExpr("var a := aaf(1, 2, 2)");
        p.setExpr("var b := aaf(1, 2, 2)");
        p.setExpr("var c := a - b");
        // Skalarer Wert 0.
        System.out.println("      => c=aaf(1,2,2)-aaf(1,2,2)="+p.evalAADD("c"));
        assertEquals(0, p.evalAADD().getValue().getX0(), 0.0001);
        assertEquals(0, p.evalAADD().getValue().getRadius(), 0.0001);

    }

    @Test
    void checkExprEval() throws ExprError, ParseError {
        ExprParser p = new ExprParser();
        p.setExpr("var a := AAF(1.0, 2.0, -1)");
        p.setExpr("var b := AAF(1.0, 2.0, -1)");
        p.setExpr("var c := AAF(1.0, 1.0, -1)");
        p.setExpr("var d := a - b");
        p.setExpr("var e := (a - b + c)/c");
        // Intervall von -1 bis 1.
        System.out.println("");
        System.out.println("      => d = range(1,2) - range(1,2) = " + p.evalAADD("d"));
        System.out.println("      => e = range(1,2) - range(1,2) + range(1,1) = " + p.evalAADD("e"));
        System.out.println("");
    }

    @Test
    void UserFunctionCallsTest() throws ExprError, ParseError {
        System.out.println(("====  Parser, def function: f, ...  ===="));
        p.setExpr("fun f(x) := x*x*x");
        // System.out.println(p.SymbolTableInfo());
        // System.out.println("AST: "+p.evalExpr("f(13+2)*2"));
        p.setExpr("f(2)");
        // System.out.println("    func f(x) := x*x*x; f(2) = "+ p.eval());
        // assertEquals(8.0, result.getValue().getCentral(), 0.0001);

        p.setExpr("fun g(x) := x+1");
        assertEquals(32, p.evalAADD("g(13+2)*2").getValue().getX0(), 0.00001);
        System.out.println(("    fun g(x) := x+1; f(13+2)*2 = " + p.evalAADD().getValue()));
    }

    /** ToDo: Fix issue with recursive calls. */
    @Test
    void CheckFunctionCallsRecursion() throws ExprError, ParseError {
        ExprParser p = new ExprParser();
        // p.setExpr("func fak(x) := ite(x<2, 1, x*fak(x-1))");

        p.setExpr("fun fak(x) := 1+ite( true , 1 , 2)");
        System.out.println("   fak(3) = " + p.evalAADD("fak(1)"));
        // p.setExpr("let b = pi");
        // d must adapt with change of b.
        // System.out.println(p.evalAADD("d"));
    }
}
