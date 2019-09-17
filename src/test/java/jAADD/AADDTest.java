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
        Range minmax=f.getMinMax();
        System.out.println("F: "+f);
    }

    @Test
    void negate() {
        AADD a = new AADD(10.0);
        AADD r = a.negate();
        assertEquals(r.Value().getCentral(), -10.0);

        int cond = AADDMgr.newBtmIndex(new AffineForm(1, 2, 3));
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
        assertEquals(AffineForm.Type.INFINITE, inv.Value().getType());

        // Infinity should be preserved
        AffineForm inf = new AffineForm(Double.POSITIVE_INFINITY);
        AADD inf_node = new AADD(inf);
        AADD inv2 = inf_node.inverse();
        assertEquals(AffineForm.Type.INFINITE, inv2.Value().getType());

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
        assertEquals(AffineForm.Type.INFINITE, div.Value().getType());

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
       Range r =add1.getMinMax();
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


    /**
     * Stress test: Discrete-time water level monitor.
     *
     * - level is the water level of a water container.
     * - It shall stay in the bounds of 0.5 (never empty) to 11.5 (never overflow).
     * - Sensors check for lower bound 2.0 and upper bound 10.0 of level.
     * - If level <= 2.0, pump fills in water with rate in_rate.
     * - If level >= 10.0, pump removes water with rate out_rate.
     */
    @Test
    void waterlevelStressTest() {
        System.out.println("==== Stupid water level monitor as stress test ====");

        // some constants with uncertain value.
        AADD outrate = new AADD(-1, -0.6, 1, "Uoutrate", "l/sec", "Symbol models uncertain amount of water leaving the tank if pump removes water");
        AADD inrate  = new AADD( 0.6, 1, 2, "Uinrate", "l/sec", "Symbol models uncertain amount of water entering the tank if pump fills it with water");

        // the starting state. 5 l are inside, it's filling with inrate.
        AADD level = new AADD(4, 5, 3,  "Ulevel", "l", "Uncertainty in  amount of water in the tank.");
        AADD rate = inrate;

        // for discrete-time approximation of a time step
        AADD change;

        for (Double time = 0.0; time < 100; time = time + 1.0) {     // Sampled over 25 sec.

            Range wl = level.getMinMax();                // Solves LP to compute accurate bounds of AADD.
            AADDMgr.addAADDSample("level", level, time); // Writes level into stream of samples of AADD.

            // Some tracing.
            System.out.print("  At time: " + time + " sec. ");
            double lb = wl.getMin();
            double ub = wl.getMax();
            System.out.println("water level is: "+wl);

            // Testing some invariants of the model.
            assertTrue(lb > 0.1); // Invariant: no empty tank.
            assertTrue(ub < 11.9);// Invariant: no overflow of tank.

            BDD too_high = level.Gt(new AADD(10.0));      // Checks for upper threshold.
            BDD too_low = level.Lt(new AADD(2.0));       // Checks for lower threshold.

            rate = too_high.ITE(outrate, rate);            // If Ge, then set pump to outrate, else keep rate.
            rate = too_low.ITE(inrate, rate);             // If Lt, then set pump to inrate, else keep rate.

            // To be correct, we would have to compute change = rate * time step (numerical integration).
            // We should also consider the inaccuracy of discrete integration fault.
            // change = rate.mult(new AADD(1.0) );
            level = level.add(rate);                         // The level has changed in 1 sec. with rate.
        }

        // DD.io.writeToJson(); // Writes results of tracing into file level.json.
        // System.out.println();
        // DD.symbolMgr.PrintInfo();   // Prints summary to screen.

        // For Debug uncomment the following to print Json to screen:
        // Gson gson = new Gson();
        // System.out.println("JSON string of level: " + gson.toJson(level););
    }

    @Test
    void waterlevelDoubleTest() {
        System.out.println("==== Stupid water level monitor with double numbers ====\n");

        // some constants with uncertain value.
        double outrate = -0.8;
        double inrate  = 0.8;

        // the starting state. 5 l are inside, it's filling with inrate.
        double level = 4.5;
        double rate = inrate;

        // for discrete-time approximation of a time step
        double change;

        for (Double time = 0.0; time < 25; time = time + 1.0) {     // Sampled over 25 sec.

            // Some tracing.
            System.out.print("  At time: " + time + " sec. ");
            System.out.println("water level is: "+level);

            // The obvious invariants that must hold.
            assertTrue(level > 0.5);
            assertTrue( level < 11.5);

            // Testing some invariants of the model, could be from AADD-Simulation !
            // assertTrue(level >= level.bounds.getLb() ); // Invariant: no empty tank.
            // assertTrue(level <= level.bounds.getUb()  );// Invariant: no overflow of tank.

            // level.io.addAADDSample("level", level, time); // Writes level into stream of samples of AADD.

            Boolean too_high = ( level >= 10.0) ; // Checks for upper threshold.
            if ( too_high ) rate = outrate;       // If Ge, then set pump to outrate, else keep rate.
            else rate = rate;

            Boolean too_low = ( level < 2.0) ;   // Checks for lower threshold.
            if (too_low) rate = inrate;          // If Lt, then set pump to inrate, else keep rate.
            else rate = rate;

            // to be correct, we would have to compute change = rate * time step (numerical integration).
            // to be correct, we should also consider the inaccuracy of discrete integration fault.
            change = rate * 1.0;
            level = level + change;                         // The level has changed in 1 sec. with rate.
        }
    }
}