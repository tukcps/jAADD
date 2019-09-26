package jAADD;

import jAADD.*;
import ExprParser.ParseError;
import ExprParser.ExprError;
import ExprParser.ExprParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ExprParserTest {

    @BeforeEach
    void setUp() {
    }

    @Test
    void ExpressionParserCheck() throws ExprError, ParseError {
        ExprParser p = new ExprParser();
        AADD a;
        BDD b;

        System.out.println("====  Checking ExpressionParser: -1+2*-3/(3-2)  =====");
        a = p.evalAADD(" -1+2*- 3/(3-2)");
        System.out.println("      AST: " + p.AST.toString());
        assertEquals(-7.0, a.Value().getCentral());

        System.out.println("====  Checking ExpressionParser: defVar, b-a  ====");
        p.defVar("a", new AADD(1.0));
        p.defVar("b", new AADD(2.0));
        p.defVar("c", new AADD(3.0));
        p.defVar("d", new AADD(4.0));
        // Deprecated: IF-THEN-ELSE. Replaced with ITE
        // a = p.evalAADD("if a>b then c else d");
        // assertEquals(4.0, a.Value().getCentral());
        a=p.evalAADD("b-a*c+d");
        assertEquals(2-1*3+4, a.Value().getCentral());

        System.out.println("====  Checking ExpressionParser: (a>b) & true  ====");
        p = new ExprParser();
        p.defVar("a", new AADD(1.0));
        p.defVar("b", new AADD(2.0));
        b = p.evalBDD("(a>b) & true");
        assertEquals(false, b.Value());

        System.out.println("====  Checking ExpressionParser: pi+e ====");
        p.setExpr("pi+e");
        a = p.evalAADD();
        assertEquals(Math.PI + Math.E, a.Value().getCentral());

        System.out.println("====  Checking re-computation   =====");
        p.setExpr("a+b*c");
        p.defVar("a", new AADD(1));
        p.defVar("b", new AADD(2));
        p.defVar("c", new AADD(3));
        a = p.evalAADD();
        assertEquals(7, a.Value().getCentral());
        p.defVar("a", new AADD(10));
        a = p.evalAADD();
        assertEquals(16, a.Value().getCentral());
    }

    @Test
    void CheckFunctionCalls() throws ExprError, ParseError {
        ExprParser p = new ExprParser();

        System.out.println(("====  Parser, function calls: not(true), not(FALSE)  ===="));
        p.setExpr("not(true)");
        System.out.println("      => " + p.evalBDD());
        assertEquals(false, p.evalBDD().Value());
        p.setExpr("not(FALSE)");
        System.out.println("      => " + p.evalBDD());
        assertEquals(true, p.evalBDD().Value());

        System.out.println("====  Parser, function calls: ITE(a>b, a+b, ITE(c<5,d,pi)) ====");

        p.defVar("a", new AADD(1));
        p.defVar("b", new AADD(2));
        p.defVar("c", new AADD(0, 100, -1));
        p.defVar("d", new AADD(3));

        p.setExpr("ITE(true, 2, 1)");
        System.out.println("     ==> ITE(true, 2, 1)="+p.evalAADD());
        assertEquals(2, p.evalAADD().Value().getCentral());

        p.setExpr("ITE(a>b, a+b, ITE(a>c, d, pi))");
        System.out.println(p.AST);
        System.out.println(p.SymbolTableInfo());

        AADD a = p.evalAADD();
        System.out.println("      AST: " + p.AST);
        System.out.println("      =>   " + a);
        assertEquals(true, a.isInternal());

        p.setExpr("ITE(true, true, false)");
        assertEquals(p.evalBDD().Value(), true);

        System.out.println(("====  Parser, function calls: range, aaf  ===="));

        p.setExpr("var a := range(1, 2)");
        p.setExpr("var b := range(1, 2)");
        p.setExpr("var c := a - b");
        // Intervall von -1 bis 1.
        System.out.println("      => c=range(1,2)-range(1,2)=" + p.evalAADD("c"));
        assertEquals(1, p.evalAADD().Value().getRadius(), 0.0001);

        p.setExpr("var a := aaf(1, 2, 2)");
        p.setExpr("var b := aaf(1, 2, 2)");
        p.setExpr("var c := a - b");
        // Skalarer Wert 0.
        System.out.println("      => c=aaf(1,2,2)-aaf(1,2,2)="+p.evalAADD("c"));
        assertEquals(0, p.evalAADD().Value().getCentral(), 0.0001);
        assertEquals(0, p.evalAADD().Value().getRadius(), 0.0001);
    }
    @Test
    void UserFunctionCallsTest() throws ExprError, ParseError {
        ExprParser p = new ExprParser();

        System.out.println(("====  Parser, def function: f, fak, ...  ===="));
        // p.setExpr("func fak(x) = ite(x<2, 1, x*fak(x-1))");
        p.evalAADD("var y := -10");
        p.setExpr("func f(x) := x+1");
        System.out.println(p.SymbolTableInfo());
        System.out.println("AST: "+p.evalExpr("f(13+2)*2"));
        assertEquals(32, p.evalAADD("f(13+2)*2").Value().getCentral(), 0.00001);
        System.out.println(("      var x := -10; f(x) = x+1; f(13+2)*2 = " + p.evalAADD().Value().getCentral()));
    }

    @Test
    void CheckFunctionCallsRecursion() throws ExprError, ParseError {
        ExprParser p = new ExprParser();
        System.out.println(p.setExpr("func fak(x) := 1+ite( true , 1 , 2)")); // fak(x-1))");
        // System.out.println("   fak(3) = " + p.evalAADD("fak(3)"));
        // p.setExpr("let b = pi");
        // d must adapt with change of b.
        // System.out.println(p.evalAADD("d"));
    }
}