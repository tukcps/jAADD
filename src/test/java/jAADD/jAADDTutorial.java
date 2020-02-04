package jAADD;

import exprParser.ExprError;
import exprParser.ExprParser;
import exprParser.ParseError;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class jAADDTutorial {
     @Test
        void instantiation() {
            AADD scalar  = AADD.scalar(1.0);
            AADD range   = AADD.range(2.0, 3.0, "r");
            AADD real    = AADD.Reals;
            AADD empty   = AADD.Empty;
            AADD realNaN = AADD.RealsNaN;
            System.out.println("scalar  = " + scalar);
            System.out.println("range   = " + range);
            System.out.println("real    = " + real);
            System.out.println("empty   = " + empty);
            System.out.println("realNaN = " + realNaN);

            assertEquals(1.0, scalar.getValue().getCentral());
            assertTrue(range.getValue().equals(new AffineForm(2,3,"r")));
        }

        @Test
        void computation() {
            AADD a = AADD.range(1.0, 2.0, "a");
            AADD b = AADD.range(1.0, 2.0, "b");
            System.out.println("    a-a = " + (a.minus(a)));
            System.out.println("but a-b = " + a.minus(b));
            assertEquals(0.0, a.minus(a).getValue().getX0());
            assertEquals(0.0, a.minus(a).getValue().getX0());
        }

        @Test
        void expression() {
            // Volume of ellipsoid = 4/3 pi a b c
            AADD a = AADD.range(1.0, 10.0, 1);
            AADD b = AADD.range(1.0, 10.0, 2);
            AADD c = AADD.range(1.0, 10.0, 3);
            AADD pi = AADD.range(3.141, 3.142, 4);
            AADD vol = pi.times(a.times(b.times(c)));
            vol = vol.times(AADD.scalar(4.0/3.0));
            System.out.println("Volume = " + vol);
            System.out.println("Volume = " + vol);
            assertEquals(10*10*10*3.142*4/3, vol.getValue().getMax());
            assertEquals(3.141*4/3, vol.getValue().getMin());
        }

        @Test
        void PIcontrolDouble() {
            System.out.println("\n=== PI controller example with double ===");
            var setval= 0.5;
            var isval = 1.0;
            var piout = 0.4;
            double inval;
            for(int i = 1; i<50; i++ ) {
                inval = setval - isval;
                piout += inval * 0.05;
                isval = isval*0.5 + piout*0.5;
            }
        }

        @Test
        void PIcontrol() {
            System.out.println("\n=== PI controller example with AADD ===");
            var setval = AADD.range(0.4, 0.6, "setval");
            var isval  = AADD.range(0.9, 1.0, "isval");
            var pi_out = AADD.range(0.5, 0.51, "piout");
            var t = new AADDStream("isval");
            AADD inval;
            for (int i = 1; i<50; i++) {
                inval = setval.minus(isval);
                pi_out = pi_out.plus(inval.times(AADD.scalar(0.05)));
                isval = isval.times(AADD.scalar(0.5)).plus(pi_out.times(AADD.scalar(0.5)));
                // t.add(isval, i);
            }
            // t.display();
        }

        /** Instantiation of some BDD */
        @Test
        void BDDinstantiation() {
            BDD a = BDD.constant(true); // gets BDD with Boolean value true or false
            BDD f = BDD.False;         // Constant leaf with value false
            BDD t = BDD.True;
            BDD X = BDD.variable("X");     // Constant with value true or false
            System.out.println("a="+a);
            System.out.println("f="+f);
            System.out.println("t="+t);
            System.out.println("X="+X);

            BDD d = f.and(X).or(t);
            BDD e = t.and(BDD.variable("X"));
            System.out.println("d="+ d + ", e="+e);
            assertTrue(d.getValue());
            assertTrue(e.equals(BDD.variable("X")));
        }


        @Test
        void AssumeGuaranteeExercise() {
            BDD a, b, c;
            a = BDD.False;
            b = BDD.variable("b");
            c = BDD.variable("c");

            BDD d = a.or(b.and(c.not()));
            System.out.println("The property d can be guaranteed if " + d);
            assertEquals(2, d.height());
            assertEquals(3, d.numLeaves());
        }

        @Test
        void correlatedComparisonTest() {
            System.out.println("=== Comparison of two overlapping uncorrelated AADD ===");
            // Result can be true, depending on a condition c.
            AADD a = AADD.range(1.0, 3.0, 1);
            AADD b = AADD.range(2.0, 4.0, 2);
            BDD  c = a.gt(b);
            System.out.println("c="+c);
            assertEquals(1, c.height());
        }

        @Test
        void uncorrelatedComparisonTest() {
            System.out.println("=== Comparison of two overlapping correlated AADD ===");
            // Result must be false as difference is a constant
            AADD a = AADD.range(1.0, 3.0, 1);
            AADD b = AADD.range(2.0, 4.0, 1);
            BDD  c = a.gt(b);
            System.out.println("c="+c);
            assertFalse(c.getValue(), "Comparison of two overlapping correlated AADD failed");
        }

        @Test
        void Comparison3() {
            System.out.println("=== Comparison of overlapping AADD with mix of noise variables ===");
            AADD a = AADD.range(1.0, 3.0, 1);
            AADD b = AADD.range(2.0, 4.0, 2);
            BDD  c = a.times(b).gt(a.plus(b));
            assertEquals(1, c.height(), "Comparison of AADD with two noise variables failed");
        }


        @Test
        void toJsonTest() {
            System.out.println("=== run a conversion to Json ===");
            AADD a = AADD.range(1.0, 2.0, 4);
            String s = a.toJson();
            System.out.println("s = " + s);
            assertTrue(s.length() > 20);
        }


        @Test
        /** Example shows the AADD getRange function to call LP solver */
        void ITEExample1() {
            AADD a = AADD.range(-1.0, 1.0, 1);
            a = a.le(AADD.scalar(0)).ite(a.plus(AADD.scalar(2.0)), a.minus(AADD.scalar(2.0)));
            System.out.println("a="+a);
            a.getRange();
            System.out.println("a="+a);
        }


        @Test
        void ExprParserExample1() throws ExprError, ParseError {
            ExprParser p = new ExprParser();
            p.eval("var a := aaf(1.0, 2.0, 1)");
            p.eval("var b := range(2.0, 3.0)");
            p.eval("var c := 3.0");
            System.out.println("a+b*c=" + p.evalExpr("a+b*c").getValue());
        }

        @Test
        void UserFunctionCallsTest() throws ExprError, ParseError {
            ExprParser p = new ExprParser();
            p.eval("fun f(x) := x*x*x");
            System.out.println("    func f(x) := x*x*x; f(2) = " + p.eval("f(2)"));
            assertEquals(8.0, p.evalAADD().getValue().getCentral());
        }

}
