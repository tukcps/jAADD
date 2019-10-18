package jAADD;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.*;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


class AADDTest {
    private static final double PRECISION_EXP_MINUS_6 = 0.000001;
    private static final double PRECISION_EXP_MINUS_2 = 0.01;
    private static AffineForm affineForm1, largerValue, restrictedRange;

    @BeforeAll
    public static void setUp() {
        affineForm1 = new AffineForm(1.0, 2.0, -1, "affineForm1 from setUp");

        // Larger value
        HashMap<Integer, Double> terms = new HashMap<Integer, Double>();
        terms.put(1, 2.0);
        terms.put(2, 1.0);
        largerValue = new AffineForm(10.0, terms, 0.0);  //, "largerValue from setUp");

        // Restricted range through manual min/max values
        terms = new HashMap<Integer, Double>();
        terms.put(2, 0.5);
        restrictedRange = new AffineForm(1.5, terms, 0.0, 1.1, 1.9);
    }

    @Test
    void inFeasibility() {
        AADD a = new AADD(0, 1, 1);
        AADD b = new AADD(3, 4, 1);

        AffineForm.diagnostics_on=false;

        BDD c1 = a.Gt(new AADD(0.5));
        BDD c2 = a.Lt(new AADD(0.3));

        AADD d = c1.ITE(a, b);
        AADD e = c2.ITE(a, b);

        AADD f = d.add(e);
        Range minmax=f.getRange();
        System.out.println("F: "+f);
    }

    @Test
    void negate() {
        AADD a = new AADD(10.0);
        AADD r = a.negate();
        assertEquals(r.Value().getCentral(), -10.0);

        int cond = Conditions.newBtmIndex(new AffineForm(1, 2, 3));
        AADD t = new AADD(cond, a, r);
        AADD tn = t.negate();
        AADD s = tn.add(t); // adding also reduces to single node.
        // This assertion fails if merging when considering the quantization error prevents merge.
        assertTrue(s.isLeaf() );
        assertEquals( 0.0, s.Value().getCentral() );
        System.out.println(s);
        System.out.println(tn);
    }

    @Test
    void add() {
        AADD a = new AADD(10.0);
        AADD b = new AADD(1.0);
        AADD r = a.add(b);
        assertEquals(r.Value().getCentral(), 11);
    }

    @Test
    void mult() {
        AADD a = new AADD(10.0);
        AADD b = new AADD(3.0);
        AADD r = a.mult(b);
        assertEquals(r.Value().getCentral(), 30);
    }

    @Test
    void exp(){
        // Test scalar
        AADD a = new AADD(3.5);
        AADD b = new AADD(-1.0);
        AADD exp1 = a.exp();
        AADD exp2 = b.exp();

        assertEquals(Math.exp(3.5), exp1.Value().getCentral(), PRECISION_EXP_MINUS_6);
        assertEquals(Math.exp(-1.0), exp2.Value().getCentral(), PRECISION_EXP_MINUS_6);

        // Test Interval
        AADD c = new AADD(affineForm1);
        AADD exp3 = c.exp();
        assertEquals(5.06, exp3.Value().getCentral(), PRECISION_EXP_MINUS_2);
        assertEquals(0.98, exp3.Value().getR(), PRECISION_EXP_MINUS_2);
        assertEquals(2.72, exp3.Value().getMin(), PRECISION_EXP_MINUS_2);
        assertEquals(7.39, exp3.Value().getMax(), PRECISION_EXP_MINUS_2);

        AADD d = new AADD(largerValue);
        AADD exp4 = d.exp();

        assertEquals(221755.0, exp4.Value().getCentral(), 1.0);
        assertEquals(217368.0, exp4.Value().getR(), 1.0);
        assertEquals(1097.0, exp4.Value().getMin(), 1.0);
        assertEquals(442413.0, exp4.Value().getMax(), 1.0);
    }

    @Test
    public void sqrt() {
        AADD a = new AADD(affineForm1);
        AADD sqrt1 = a.sqrt();
        assertEquals(1.23, sqrt1.Value().getCentral(), PRECISION_EXP_MINUS_2);
        assertEquals(0.05, sqrt1.Value().getR(), PRECISION_EXP_MINUS_2);
        assertEquals(1.0, sqrt1.Value().getMin(), PRECISION_EXP_MINUS_2);
        assertEquals(1.45, sqrt1.Value().getMax(), PRECISION_EXP_MINUS_2);

        AADD b = new AADD(largerValue);
        AADD sqrt2 = b.sqrt();
        assertEquals(3.17, sqrt2.Value().getCentral(), PRECISION_EXP_MINUS_2);
        assertEquals(0.11, sqrt2.Value().getR(), PRECISION_EXP_MINUS_2);
        assertEquals(2.65, sqrt2.Value().getMin(), PRECISION_EXP_MINUS_2);
        assertEquals(3.69, sqrt2.Value().getMax(), PRECISION_EXP_MINUS_2);

        AADD c = new AADD(restrictedRange);
        AADD sqrt3 = c.sqrt();
        assertEquals(1.23, sqrt3.Value().getCentral(), PRECISION_EXP_MINUS_2);
        assertEquals(0.04, sqrt3.Value().getR(), PRECISION_EXP_MINUS_2);
        assertEquals(1.05, sqrt3.Value().getMin(), PRECISION_EXP_MINUS_2);
        assertEquals(1.40, sqrt3.Value().getMax(), PRECISION_EXP_MINUS_2);
    }

    @Test
    public void logarithm(){
        AADD a = new AADD(affineForm1);
        AADD log1 = a.log();
        assertEquals(0.38, log1.Value().getCentral(), PRECISION_EXP_MINUS_2);
        assertEquals(0.02, log1.Value().getR(), PRECISION_EXP_MINUS_2);
        assertEquals(0.0, log1.Value().getMin(), PRECISION_EXP_MINUS_2);
        assertEquals(0.75, log1.Value().getMax(), PRECISION_EXP_MINUS_2);

        AADD b = new AADD(largerValue);
        AADD log2 = b.log();
        assertEquals(2.28, log2.Value().getCentral(), PRECISION_EXP_MINUS_2);
        assertEquals(0.02, log2.Value().getR(), PRECISION_EXP_MINUS_2);
        assertEquals(1.95, log2.Value().getMin(), PRECISION_EXP_MINUS_2);
        assertEquals(2.61, log2.Value().getMax(), PRECISION_EXP_MINUS_2);

        AADD c = new AADD(restrictedRange);
        AADD log3 = c.log();
        assertEquals(0.39, log3.Value().getCentral(), PRECISION_EXP_MINUS_2);
        assertEquals(0.02, log3.Value().getR(), PRECISION_EXP_MINUS_2);
        assertEquals(0.09, log3.Value().getMin(), PRECISION_EXP_MINUS_2);
        assertEquals(0.68, log3.Value().getMax(), PRECISION_EXP_MINUS_2);
    }

    @Test
    public void inverse(){
        // Around zero
        AffineForm affineForm3 = new AffineForm(-2.0, 2.0, -1);
        AADD af3_node = new AADD(affineForm3);
        AADD inv = af3_node.inverse();
        assertEquals(Range.Trap.INFINITE, inv.Value().getTrap());

        // Infinity should be preserved
        AffineForm inf = new AffineForm(Double.POSITIVE_INFINITY);
        AADD inf_node = new AADD(inf);
        AADD inv2 = inf_node.inverse();
        assertEquals(Range.Trap.INFINITE, inv2.Value().getTrap());

        // Regular
        AADD inv3_node = new AADD(affineForm1);
        AADD inv3 = inv3_node.inverse();
        assertEquals(0.75, inv3.Value().getCentral(), PRECISION_EXP_MINUS_6);
        assertEquals(0.5, inv3.Value().getMin(), PRECISION_EXP_MINUS_6);
        assertEquals(1.0, inv3.Value().getMax(), PRECISION_EXP_MINUS_6);
        assertEquals(0.125, inv3.Value().getR(), PRECISION_EXP_MINUS_6);
        assertEquals(0.125, inv3.Value().getRadius(), PRECISION_EXP_MINUS_6);
    }

    @Test
    public void div() {
        // Div by zero should return infinite
        AffineForm zero = new AffineForm(0.0);
        AADD zero_node = new AADD(zero);
        AADD affineForm1_node = new AADD(affineForm1);
        AADD div = affineForm1_node.div(zero_node);
        assertEquals(Range.Trap.INFINITE, div.Value().getTrap());

        // Regular division
        AADD a = new AADD(10.0);
        AADD b = new AADD(5.0);
        AADD result = a.div(b);
        assertEquals(result.Value().getCentral(), 2);

        //Division tested by inversion + multiplication
        result = a.mult(b.inverse());
        assertEquals(result.Value().getCentral(), 2);
    }

    @Test
    void checkApply() {
        // Two BDD with different index.
        AADD ai = new AADD(-1, 1, 1);
        AADD tr = new AADD(0.1);
        AADD tr2 = new AADD(0.2);
        BDD  c1 = ai.Ge(tr);
        BDD  c2 = ai.Le(tr2);
        assertTrue( c1.index != c2.index );

        AADD a = new AADD(1.0);
        AADD b = new AADD(2.0);

        AADD d = c1.ITE(a, b);
        assertTrue( c1.index == d.index);
        assertTrue(c1.height() == 1);

        AADD e = c2.ITE(a, b);
        assertTrue(c2.index == e.index);
        assertTrue(e.height() == c2.height() );

        AADD f = e.add(d);
        assertTrue( f.height() == 2);

        // System.out.println(" d = " + d);
        // System.out.println(" e = " + e);
        // System.out.println(" f = " + f);
    }

    @Test
    void ITEtest() {

        AADD a = new AADD(-1, 1, 1);
        AADD b = new AADD(0.0);
        System.out.println("=== Testing ITE function of AADD: ===");
        System.out.println("  ITE of an AADD leave with a BDD of height 1 must result in merged AADD with height 1.");
        System.out.println("  AADD a = "+a);
        System.out.println("  AADD b = "+b);
        BDD c = a.Ge(b);
        assert(c.height() == 1);
        System.out.println("  BDD c = "+c);
        a = c.ITE(a, b);
        System.out.println("  ITE(c, a, b): a="+a);
        assertEquals(a.height(), 1);

    }

    @Test
    void compare1() {
        // create affine form 2+2*e3+2*e4
        AffineForm affineForm = new AffineForm(-2.0, 2.0, 3);
        affineForm=affineForm.add(new AffineForm(0,4,4));
        System.out.println(affineForm);
        AADD a=new AADD(affineForm);
        System.out.println("a: " +a+"\n");

        // compares a with 0 (a>=0)
        BDD bdd1=a.Ge(new AADD(0.0));
        System.out.println("Result of first comparison:......");
        System.out.println("Bdd1: " + bdd1 );
        AffineForm cons1=bdd1.Cond(); // constraint of the root of BDD x
        System.out.println("Condition:......");
        System.out.println(cons1+"\n");

        // a<4.0
        BDD bdd2=a.Lt(new AADD(4.0));
        System.out.println("Result of second comparison:......");
        System.out.println("Bdd2: "+ bdd2 + "\n");

        AffineForm d=new AffineForm(0.5);
        d=d.add(affineForm.mult(0.5));
        // System.out.println("d: "+d);

        // Build AADD with ITE
        AADD add1=bdd1.ITE(a, new AADD(d));
        System.out.println("AADD1 with ITE: "+add1 + "\n");

        // check add1 < 0
        System.out.println("Result of third comparison:......");
        System.out.println("add1 = " + add1);
        BDD bdd3=add1.Lt(new AADD(0.0));
        System.out.println("BDD3: " + bdd3 + "\n");

        // Create affine form 5.0+10.0*e4+4.0*e3
        Iterator<Integer> it=affineForm.xi.keySet().iterator();
        HashMap<Integer, Double> terms=new HashMap<>();
        terms.put(it.next(), 4.0);
        terms.put(it.next(), 10.0);
        AffineForm affineForm2=new AffineForm(5.0, terms, 0.0);
        System.out.println("AffineForm2: "+affineForm2);

        // Construct AADD with 2+2*e3+2*e4 for bdd3=ONE and 5.0+10.0*e4+4.0*e3 bdd3=ZERO
        add1=bdd3.ITE(a, new AADD(affineForm2));
        System.out.println("aadd1: "+add1);

       // Compute Total Lower and Upper Bounds of add1
       Range r =add1.getRange();
       System.out.println("BOUNDS of AAD1 COMPUTED: [LB: "+r.getMin()+", UB: "+r.getMax()+"]");

        // t <=4.0
       AADD t=new AADD(new AffineForm(0, 4, -1));
       BDD bdd4=t.Le(new AADD(4.0));
        System.out.println("BDD4: " + bdd4);


    }

    @Test
    void compare2() {

        // create affine form 2+2*e3+2*e4
        AffineForm affineForm1 = new AffineForm(-4., 4., -1);
        AffineForm affineForm2 = new AffineForm(0, 1.0, -1);

        AADD a=new AADD(affineForm1);
        AADD b=new AADD(affineForm2);

        System.out.println("a: " +  a);
        System.out.println("b: " +  b);

        BDD cond=a.Gt(new AADD(0.0)); // a > 0.0
        BDD cond1=a.Gt(new AADD(2.0)); // a > 2.0

        AADD at=a.add((b.mult(new AADD(0.4))).sub(new AADD(1.5))); // value for cond=true at= a+b*0.4-1.5
        AADD af=a.add((b.mult(new AADD(0.4))).sub(new AADD(0.4))); // value for cond=false af= a+b*0.4-0.4

        AADD a1=cond1.ITE(at, af);

        System.out.println("a1: " +  a1);

        BDD cond2=a.Gt(new AADD(-2.0)); // a > -2.0

        at=a.add((b.mult(new AADD(0.5))).add(new AADD(0.6))); // at=a+b*0.5+0.6

        af=a.add((b.mult(new AADD(0.7))).add(new AADD(1.5))); // af=a+b*0.7+1.5

        AADD a2=cond2.ITE(at, af);

        System.out.println("a2: " +  a2);

        a=cond.ITE(a1, a2);

        System.out.println("a: " +  a);
    }

    @Test
    void reduceTest() {
        // Two BDD with different index.
        AADD ai = new AADD(-1, 1, 1);
        AADD tr = new AADD(0.1);
        AADD tr2 = new AADD(0.2);
        BDD  c1 = ai.Ge(tr);
        BDD  c2 = ai.Le(tr2);

        AADD a = new AADD(1, 2,1);
        AADD b = new AADD(2,3,1);

        AADD d = c1.ITE(a, b);
        AADD e = c2.ITE(a, b);
        assertTrue(c2.index == e.index);
        assertTrue(e.height() == c2.height() );
        AADD f = e.add(d);
        assertTrue( f.height() == 2);

        System.out.println("f="+f);
    }



}