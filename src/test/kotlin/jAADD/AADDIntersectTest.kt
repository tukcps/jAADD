package jAADD
import jAADD.AADD.Companion.range
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class AADDIntersectTests {


    /**
     *  Test the intersection of an AADD with an interval; constrained by a narrower interval.
     *  The result must be the AADD, with additional linear constraints limited to the interval.
     *  This must take effect after calling the LP solver.
     */
    @Test
    fun intersectTestNarrowInterval() {
        println("=== Testing: Intersection of AADD with narrower interval ===")
        val a = range(1.0, 3.0, -1)
        val b = a.intersect(1.2, 2.2)
        println("a=$a intersect [1.2, 2.2] as AADD is: $b")
        println("a's range computed by LP solver is: " + b.getRange())  // getRange calls the LP solver.
        Assertions.assertEquals(1.2, b.getRange().min, 0.001)
        Assertions.assertEquals(2.2, b.getRange().max, 0.001)
    }

    /**
     * Test the intersection of an AADD with an interval; constrained by a wider interval
     */
    @Test
    fun intersectTestWiderInterval() {
        println("=== Testing: Intersection of AADD with wider interval (min, max) ===")
        val a = range(1.0, 3.0, -1)
        val b = a.intersect(0.5, 4.0)
        println("a=$a intersect [0.5, 4] is: $b")
        println("b's range computed by LP is:" + b.getRange())
        Assertions.assertEquals(1.0, b.getRange().min, 0.001)
        Assertions.assertEquals(3.0, b.getRange().max, 0.001)
    }

    /**
     * Test the intersection of an AADD with a Range-type object
     */
    @Test
    fun intersectTestRange() {
        println("=== Testing: Intersection of AADD with Range ===")
        val a = range(1.5, 3.0, -1)
        val c = a.intersect(0.9 .. 2.2)
        println("a = $a")
        println("a intersect b = $c")
        println("with range:" + c.getRange())
        Assertions.assertEquals(1.5, c.getRange().min, 0.001)
        Assertions.assertEquals(2.2, c.getRange().max, 0.001)
    }

    /**
     * Test the intersection of an AADD with an AffineForm
     */
    @Test
    fun intersectTestAffineForm() {
        println("=== Testing: Intersection of AADD with Affine Form ===")
        val a = range(1.5, 3.0, -1)
        val b = AffineForm(1.2, 2.2, -1)
        val c = a.intersect(b)
        println("a = $a, b = $b")
        println("a intersect b = $c")
        println("with range:" + c.getRange())
        Assertions.assertEquals(1.5, c.getRange().min, 0.001)
        Assertions.assertEquals(2.2, c.getRange().max, 0.001)
    }

    /**
     * Test the intersection of an AADD with an AADD
     */
    @Test
    fun intersectTestAADD() {
        println("=== Testing: Intersection of AADD with AADD ===")
        val a = range(2.2 .. 3.0, -1)
        val b = range(1.2 .. 2.5, -1)
        val c = a.intersect(b)
        println("a = $a, b = $b")
        println("a intersect b = $c")
        println("with range: " + c.getRange())
        Assertions.assertEquals(2.2, c.getRange().min, 0.001)
        Assertions.assertEquals(2.5, c.getRange().max, 0.001)
    }

    @Test
    fun intersectTest() {
        var a = range(0.98, 1.05, -1)
        val b = range(0.99, 1.01, -1)
        a = a.intersect(b)
        println("a=" + a + " but Range with LP solver is: " + a.getRange())
    }
}