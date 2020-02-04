package jAADD

import com.google.gson.GsonBuilder
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*


class AffineFormTest {

    init {
        // Larger value
        var terms = HashMap<Int, Double>()
        terms[1] = 2.0
        terms[2] = 1.0
        lgr = AffineForm(Range.Reals,10.0, 0.0, terms)

        // Scalar form
        scl = AffineForm(1.0)

        // Restricted range through manual min/max values
        terms = HashMap()
        terms[2] = 0.5
        rst = AffineForm(Range(1.1, 1.9), 1.5, 0.0, terms)
    }

    /** A real is the range +-MAX_VALUE. It is identified by isReal() */
    @Test fun realTest() {
        val real = AffineForm(Range.Kind.REALS)
        assertEquals(-Double.MAX_VALUE, real.min)
        assertEquals(Double.MAX_VALUE, real.max)
        assertTrue(real.isReals())
    }

    @Test
    fun overloadedOperators() {
        val a = AffineForm(1.0, 2.0, 1)
        val b = AffineForm(2.0, 3.0, 1)
        var y = a - b
        assertEquals(0.0, y.radius, 0.000001)

        y = a*b
        assertEquals(2.0, y.min, 0.000001)
        assertEquals(6.0, y.max, 0.000001)

        println("y="+y)
    }


    @Test
    fun testAddForms() {
        val sum = af1 + af2
        val noiseSymbols = sum.noiseVarKeys
        var termValueSum = 0.0
        for (v in sum.xi.values) {
            termValueSum += v
        }
        assertEquals(3.0, sum.central, PRECISION)
        assertEquals(2.0, sum.min, PRECISION)
        assertEquals(4.0, sum.max, PRECISION)
        assertEquals(0.0, sum.r, PRECISION)
        assertEquals(1.0, sum.radius, PRECISION)
        assertEquals(2, noiseSymbols.size)
        assertTrue(noiseSymbols.contains(1))
        assertTrue(noiseSymbols.contains(2))
        assertEquals(termValueSum, sum.radius, PRECISION)
    }

    @Test
    fun testAddFormsCustomRange() {
        val sum = af1 + rst!!
        assertEquals(3.0, sum.central, PRECISION)
        assertEquals(2.1, sum.min, PRECISION)
        assertEquals(3.9, sum.max, PRECISION)
        assertEquals(0.0, sum.r, PRECISION)
        // Doesn't change with artificial range!
        assertEquals(1.0, sum.radius, PRECISION)
    }

    /*
    @Test
    public void testAddScalars() {
        AffineForm sum1 = af1.scalarAdd(1.0);
        AffineForm sum2 = af1.add(scl);

        assertEquals(2.5, sum1.getCentral(), PRECISION);
        assertEquals(2.0, sum1.getMin(), PRECISION);
        assertEquals(3.0, sum1.getMax(), PRECISION);
        assertEquals(0.0, sum1.getR(), PRECISION);
        assertEquals(0.5, sum1.getRadius(), PRECISION);

        assertEquals(sum1.getCentral(), sum2.central);
    }*/

    /** Adding something to infinity results in infinity */
    @Test fun testAddInfinite() {
        val af3 = AffineForm(Double.POSITIVE_INFINITY)
        val sum1 = af3 + af1
        val sum2 = af1 + af3
        assertTrue(af3.isRealsNaN())
        assertTrue(sum1.isRealsNaN())
        assertTrue(sum2.isRealsNaN())
        assertEquals(sum1, sum2)
    }

    @Test
    fun testAddNaN() {
        val af3 = AffineForm(Double.NaN)
        val sum1 = af3 + af1
        val sum2 = af1 + af3
        assertTrue(af3.isRealsNaN())
        assertTrue(sum1.isRealsNaN())
        assertTrue(sum2.isRealsNaN())
        assertEquals(sum1, sum2)
    }

    @Test
    fun testNegation() {
        val neg: AffineForm = -af1
        assertEquals(-1.5, neg.central, PRECISION)
        assertEquals(-2.0, neg.min, PRECISION)
        assertEquals(-1.0, neg.max, PRECISION)
        assertEquals(0.0, neg.r, PRECISION)
        assertEquals(0.5, neg.radius, PRECISION)
    }


    @Test
    fun testMultiplication() {
        val terms2 = hashMapOf(1 to -2.0, 3 to 1.0)
        val af3 = AffineForm(Range.Reals,10.0, 0.0, terms2)
        val mult: AffineForm = lgr!!.times(af3)
        assertTrue(mult.isRanges())
        assertEquals(100.0, mult.central, PRECISION)
        assertEquals(71.0, mult.min, PRECISION)
        assertEquals(129.0, mult.max, PRECISION)
        assertEquals(9.0, mult.r, PRECISION)
    }

    @Test
    fun testExp() {
        // Test scalar
        val af3 = AffineForm(3.5)
        val af4 = AffineForm(-1.0)
        val exp1 = scl!!.exp()
        val exp2 = af3.exp()
        val exp3 = af4.exp()
        assertEquals(Math.E, exp1.central, PRECISION)
        assertEquals(Math.exp(3.5), exp2.central, PRECISION)
        assertEquals(Math.exp(-1.0), exp3.central, PRECISION)

        // Test interval
        val exp4 = af1.exp()
        assertEquals(5.06, exp4.central, 0.01)
        assertEquals(0.98, exp4.r, 0.01)
        assertEquals(2.72, exp4.min, 0.01)
        assertEquals(7.39, exp4.max, 0.01)
        val exp5 = lgr!!.exp()
        assertEquals(221755.0, exp5.central, 1.0)
        assertEquals(217368.0, exp5.r, 1.0)
        assertEquals(1097.0, exp5.min, 1.0)
        assertEquals(442413.0, exp5.max, 1.0)
    }

    @Test
    fun testLog() {
        val log1 = af1.log()
        assertEquals(0.38, log1.central, 0.01)
        assertEquals(0.02, log1.r, 0.01)
        assertEquals(0.0, log1.min, 0.01)
        assertEquals(0.75, log1.max, 0.01)
        val log2 = lgr!!.log()
        assertEquals(2.28, log2.central, 0.01)
        assertEquals(0.02, log2.r, 0.01)
        assertEquals(1.95, log2.min, 0.01)
        assertEquals(2.61, log2.max, 0.01)
        val log3 = rst!!.log()
        assertEquals(0.39, log3.central, 0.01)
        assertEquals(0.02, log3.r, 0.01)
        assertEquals(0.09, log3.min, 0.01)
        assertEquals(0.68, log3.max, 0.01)
    }

    @Test
    fun testSqrt() {
        val sqrt1 = af1.sqrt()
        assertEquals(1.23, sqrt1.central, 0.01)
        assertEquals(0.05, sqrt1.r, 0.01)
        assertEquals(1.0, sqrt1.min, 0.01)
        assertEquals(1.45, sqrt1.max, 0.01)
        val sqrt2 = lgr!!.sqrt()
        assertEquals(3.17, sqrt2.central, 0.01)
        assertEquals(0.11, sqrt2.r, 0.01)
        assertEquals(2.65, sqrt2.min, 0.01)
        assertEquals(3.69, sqrt2.max, 0.01)
        val sqrt3 = rst!!.sqrt()
        assertEquals(1.23, sqrt3.central, 0.01)
        assertEquals(0.04, sqrt3.r, 0.01)
        assertEquals(1.05, sqrt3.min, 0.01)
        assertEquals(1.40, sqrt3.max, 0.01)
    }

    /** Inverse of range, including 1/0 and 1/(inf ... inf) */
    @Test fun testInv() {
        // Around zero
        val af3 = AffineForm(-2.0, 2.0, -1)
        val inv = af3.inv()
        assertTrue(inv.isRealsNaN())

        // Infinity should be preserved
        val inf = AffineForm(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, -1)
        val inv2 = inf.inv()
        assertTrue(inv2.isRealsNaN())

        // Regular
        val inv3 = af1.inv()
        assertEquals(0.75, inv3.central, PRECISION)
        assertEquals(0.5, inv3.min, PRECISION)
        assertEquals(1.0, inv3.max, PRECISION)
        assertEquals(0.125, inv3.r, PRECISION)
        assertEquals(0.125, inv3.radius, PRECISION)
    }

    @Test
    fun testDiv() {
        // Div by zero should return infinity or reals
        // Regular division is tested by inversion + multiplication
        val zero = AffineForm(0.0)
        val div = af1 / zero
        assertTrue(div.isRealsNaN())
    }

    @Test
    fun testSqr() {
        val mult1 = af1 * af1
        val sqr1 = af1.sqr()
        val mult2 = lgr!! * lgr!!
        val sqr2 = lgr!!.sqr()
        val mult3 = rst!! * rst!!
        val sqr3 = rst!!.sqr()
        assertEquals(mult1, sqr1) // small interval
        assertEquals(mult2, sqr2) // larger, multiple symbols
        assertEquals(mult3, sqr3) // artificially restricted bounds
    }

    @Test
    fun testCreation() {
        assertEquals(af1.central, af2.central, PRECISION)
        assertEquals(af1.min, af2.min, PRECISION)
        assertEquals(af1.max, af2.max, PRECISION)
        assertEquals(af1.r, af2.r, PRECISION)
        assertEquals(af1.radius, af2.radius, PRECISION)
        assertEquals(1.5, af2.central, PRECISION)
        assertEquals(1.0, af2.min, PRECISION)
        assertEquals(2.0, af2.max, PRECISION)
        assertEquals(0.0, af2.r, PRECISION)
        assertEquals(0.5, af2.radius, PRECISION)
        assertEquals(1.0, scl!!.central, PRECISION)
        assertEquals(scl!!.min, scl!!.max, PRECISION)
        assertEquals(0.0, scl!!.radius, PRECISION)
    }

    @Test
    fun testEquality() {
        val af3 = AffineForm(1.0, 2.0, 2)
        val af4 = af2.clone() as AffineForm
        assertFalse(af1.equals(af2))
        assertTrue(af3.equals(af2))
        assertTrue(af4.equals(af2))
    }

    @Test
    fun testAroundZero() {
        val af3 = AffineForm(-1.0, 2.0, -1)
        val af4 = AffineForm(0.0, 2.0, -1)
        val af5 = AffineForm(0.0000001, 2.0, -1)
        val af6 = AffineForm(-2.0, 0.0, -1)
        val af7 = AffineForm(-2.0, -0.0000001, -1)
        assertFalse(af3.isWeaklyPositive)
        assertFalse(af3.isWeaklyNegative)
        assertTrue(af4.isWeaklyPositive)
        assertFalse(af4.isStrictlyPositive)
        assertFalse(af4.isWeaklyNegative)
        assertTrue(af5.isWeaklyPositive)
        assertTrue(af5.isStrictlyPositive)
        assertFalse(af5.isWeaklyNegative)
        assertTrue(af6.isWeaklyNegative)
        assertFalse(af6.isStrictlyNegative)
        assertFalse(af6.isWeaklyPositive)
        assertTrue(af7.isWeaklyNegative)
        assertTrue(af7.isStrictlyNegative)
        assertFalse(af7.isWeaklyPositive)
    }


    @get:Test
    val isSimilarTest: Unit
        get() {
            val a = AffineForm(1.0, 2.0, 1)
            var b: AffineForm? = AffineForm(1.0, 2.0, 1)
            assertTrue(a.isSimilar(b!!, 0.000001))
            b = AffineForm(1.0, 2.0, 2)
            assertFalse(a.isSimilar(b, 0.000001))
        }

    @Test
    fun joinTest() {
        val a = AffineForm(1.0, 2.0, 1)
        var b: AffineForm? = AffineForm(2.0, 3.0, 1)
        assertEquals(2.0, a.join(b!!).central)
        b = AffineForm(2.0, 3.0, 2)
        assertEquals(2.0, a.join(b).central)
    }

    @Test
    fun testInfiniteTerm() {
        val terms = HashMap<Int, Double>()
        terms[1] = Double.POSITIVE_INFINITY
        val af3 = AffineForm(Range.Reals, 0.0, 0.0, terms)
        println("a3=$af3")
        assertTrue(af3.isRealsNaN())
    }

    @Test
    fun jsonTest() {
        val sum = af1 + af2
        val gson = GsonBuilder().setPrettyPrinting().create()

        // Gson gson = new Gson();
        val json = gson.toJson(sum)
        println("JSON string of af1+af2: $json")
    }

    companion object {
        private const val PRECISION = 0.000001
        private val af1 = AffineForm(1.0, 2.0, 1)
        private val af2 = AffineForm(1.0, 2.0, 2)
        private var scl: AffineForm? = null
        private var lgr: AffineForm? = null
        private var rst: AffineForm? = null
    }
}