package jAADD
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertEquals

class RangeTest {
    val a = Range(-1.0, 2.0)
    val b = Range(2.0, 3.0)

    /** A real is the range +-MAX_VALUE. It is identified by isReal() */
    @Test fun realTest() {
        val real = Range(Range.Kind.REALS)
        assertEquals(-Double.MAX_VALUE, real.min)
        assertEquals(Double.MAX_VALUE, real.max)
        assertTrue(real.isReals())
    }

    @Test
    fun testPlus() {
        val c = a+b
        println("c="+c)
    }

    @Test
    fun testMinus() {
        val c = a-b
        assertTrue(c.max == Math.max(a.min-b.max, b.min-a.max))
        assertTrue(c.min == Math.min(a.min-b.max, b.min-a.max))
        println("c="+c)
    }

    @Test
    fun RangeTst() {
        Leaf(1.0 .. 2.0)
    }

    internal fun Leaf(p: ClosedFloatingPointRange<Double>) {
        println("min: "+p.start + " max: " + p.endInclusive)
    }


}