package jAADD;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @class JavaAPITest
 * Test suite for AADD API
 * @author Christoph Grimm
 */
class JavaAPITest {


    /** The methods for creating AADD */
    @Test void factoryAPITestAADD() {
        System.out.println("=== Testing: Factory API of AADD from Java ===");

        AADD scalar  = AADD.scalar(1.0);
        AADD range   = AADD.range(2.0, 3.0, "r");
        AADD real    = AADD.Reals;
        AADD empty   = AADD.Empty;
        AADD realNaN = AADD.RealsNaN;

        System.out.println("scalar   = " + scalar);
        System.out.println("interval = " + range);
        System.out.println("real     = " + real);
        System.out.println("empty    = " + empty);

        assertEquals(1.0, scalar.getValue().getX0());
        assertTrue(range.getValue().equals(new AffineForm(2,3,"r")));
        assertTrue(real.getValue().isReals());
        assertTrue(empty.getValue().isEmpty());
        assertTrue(realNaN.getValue().isRealsNaN());
    }

    @Test
    void factoryAPITestBDD() {
        System.out.println("=== Testing: Factory API of BDD from Java ===");

        var a = BDD.True;
        var b = BDD.False;
        var c = BDD.variable("c");
        var d = BDD.constant(true);
        var f = b.and(c);
        var e = b.or(BDD.variable("e"));
        System.out.println("        d = "+f+"; e = "+e);
        assertEquals(false, f.getValue());
        assertEquals(2, e.numLeaves());
    }

    @Test
    void operationsAPITest() {
        System.out.println("=== Testing: Arithmetic operations API ===");

        // Check if negation works for Scalar: value negates is - value.
        AADD a = AADD.scalar(10.0);
        AADD r = a.negate();
        assertEquals(-10.0, r.getValue().getX0());

        // Check if binary operations on AADD work.
        AADD b = a.plus(AADD.range(1.0, 2.0, "APITest"));
        AADD zero = b.minus(b);
        assertEquals(0.0, zero.getValue().getCentral(), 0.0001);

        b = a.times(a);
        assertEquals(100.0, b.getValue().getCentral());

        b = a.div(AADD.scalar(2.0));
        assertEquals(5.0, b.getValue().getCentral());

        // other functions and operations should be there as well.
        b = a.div(a);
        b = a.exp();
        b = a.inv();
        b = a.log();
        b = a.sqrt();
    }

    @Test
    void comparisonAPITest() {

    }
}