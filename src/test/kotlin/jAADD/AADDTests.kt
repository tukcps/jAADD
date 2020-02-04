package jAADD

import jAADD.AADD.Companion.leaf
import jAADD.AADD.Companion.range
import jAADD.AADD.Companion.scalar
import jAADD.Conditions.newConstraint
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.util.*


/**
 * @class AADDTest
 * Test suite for AADD data type
 * @author Christoph Grimm, Jack D. Martin
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class AADDTest {
    private var affineForm1: AffineForm? = null
    private var largerValue:AffineForm? = null
    private var restrictedRange:AffineForm? = null

    @BeforeAll
    fun setUp() {
        affineForm1 = AffineForm(1.0, 2.0, -1)

        // Larger value
        var terms = HashMap<Int, Double>()
        terms[1] = 2.0
        terms[2] = 1.0
        largerValue = AffineForm(Range.Reals, 10.0, 0.0, terms)

        // Restricted range through manual min/max values
        terms = HashMap()
        terms[2] = 0.5
        restrictedRange = AffineForm(Range(1.1, 1.9), 1.5, 0.0, terms)
    }


    @Test
    fun inFeasibilityTest() {
        // Check if infeasible paths are detected and leaves are marked.
        val i = NoiseVariables.noiseVar("n")
        val a = range(0.0, 1.0, i)
        val b = range(3.0, 4.0, i)
        val c1 = a gt scalar(0.5)
        val c2 = a lt scalar(0.3)
        val d = c1.ite(a, b)
        val e = c2.ite(a, b)
        val f = d.plus(e)
        f.getRange()
        Assertions.assertEquals(1, f.numInfeasible())
    }

    @Test
    fun negate() {
        // Check if negation works for Scalar: value negates is - value.
        val a = scalar(10.0)
        val r = a.negate()
        Assertions.assertEquals(-10.0, r.value!!.x0)

        // Check if negation works for AADD: value + negated value = 0
        val cond = newConstraint(AffineForm(1.0, 2.0, 3), "")
        val t = AADD(cond, a, r)
        val tn = t.negate()
        val s = tn.plus(t)
        Assertions.assertEquals(0.0, s.value!!.x0)
        Assertions.assertEquals(0.0, s.getRange().min)
        Assertions.assertEquals(0.0, s.getRange().max)
        // This assertion or above assertions fail if merging when considering the quantization error prevents merge.
        Assertions.assertTrue(s.isLeaf)
    }

    @Test
    fun add() {
        val a = scalar(10.0)
        val b = scalar(1.0)
        val r = a.plus(b)
        Assertions.assertEquals(r.value!!.x0, 11.0)
    }

    @Test
    fun mult() {
        val a = scalar(10.0)
        val b = scalar(3.0)
        val r = a.times(b)
        Assertions.assertEquals(r.value!!.x0, 30.0)
    }

    @Test
    fun exp() {
        // Test scalar
        val a = scalar(3.5)
        val b = scalar(-1.0)
        val exp1 = a.exp()
        val exp2 = b.exp()
        Assertions.assertEquals(Math.exp(3.5), exp1.value!!.x0, PRECISION_EXP_MINUS_6)
        Assertions.assertEquals(Math.exp(-1.0), exp2.value!!.x0, PRECISION_EXP_MINUS_6)

        // Test Interval
        val c = leaf(affineForm1!!)
        val exp3 = c.exp()
        Assertions.assertEquals(5.06, exp3.value!!.x0, PRECISION_EXP_MINUS_2)
        Assertions.assertEquals(0.98, exp3.value!!.r, PRECISION_EXP_MINUS_2)
        Assertions.assertEquals(2.72, exp3.value!!.min, PRECISION_EXP_MINUS_2)
        Assertions.assertEquals(7.39, exp3.value!!.max, PRECISION_EXP_MINUS_2)
        val d = leaf(largerValue!!)
        val exp4 = d.exp()
        Assertions.assertEquals(221755.0, exp4.value!!.x0, 1.0)
        Assertions.assertEquals(217368.0, exp4.value!!.r, 1.0)
        Assertions.assertEquals(1097.0, exp4.value!!.min, 1.0)
        Assertions.assertEquals(442413.0, exp4.value!!.max, 1.0)
    }

    @Test
    fun sqrt() {
        val a = leaf(affineForm1!!)
        val sqrt1 = a.sqrt()
        Assertions.assertEquals(1.23, sqrt1.value!!.x0, PRECISION_EXP_MINUS_2)
        Assertions.assertEquals(0.05, sqrt1.value!!.r, PRECISION_EXP_MINUS_2)
        Assertions.assertEquals(1.0, sqrt1.value!!.min, PRECISION_EXP_MINUS_2)
        Assertions.assertEquals(1.45, sqrt1.value!!.max, PRECISION_EXP_MINUS_2)
        val b = leaf(largerValue!!)
        val sqrt2 = b.sqrt()
        Assertions.assertEquals(3.17, sqrt2.value!!.x0, PRECISION_EXP_MINUS_2)
        Assertions.assertEquals(0.11, sqrt2.value!!.r, PRECISION_EXP_MINUS_2)
        Assertions.assertEquals(2.65, sqrt2.value!!.min, PRECISION_EXP_MINUS_2)
        Assertions.assertEquals(3.69, sqrt2.value!!.max, PRECISION_EXP_MINUS_2)
        val c = leaf(restrictedRange!!)
        val sqrt3 = c.sqrt()
        Assertions.assertEquals(1.23, sqrt3.value!!.x0, PRECISION_EXP_MINUS_2)
        Assertions.assertEquals(0.04, sqrt3.value!!.r, PRECISION_EXP_MINUS_2)
        Assertions.assertEquals(1.05, sqrt3.value!!.min, PRECISION_EXP_MINUS_2)
        Assertions.assertEquals(1.40, sqrt3.value!!.max, PRECISION_EXP_MINUS_2)
    }

    @Test
    fun logarithm() {
        val a = leaf(affineForm1!!)
        val log1 = a.log()
        Assertions.assertEquals(0.38, log1.value!!.x0, PRECISION_EXP_MINUS_2)
        Assertions.assertEquals(0.02, log1.value!!.r, PRECISION_EXP_MINUS_2)
        Assertions.assertEquals(0.0, log1.value!!.min, PRECISION_EXP_MINUS_2)
        Assertions.assertEquals(0.75, log1.value!!.max, PRECISION_EXP_MINUS_2)
        val b = leaf(largerValue!!)
        val log2 = b.log()
        Assertions.assertEquals(2.28, log2.value!!.x0, PRECISION_EXP_MINUS_2)
        Assertions.assertEquals(0.02, log2.value!!.r, PRECISION_EXP_MINUS_2)
        Assertions.assertEquals(1.95, log2.value!!.min, PRECISION_EXP_MINUS_2)
        Assertions.assertEquals(2.61, log2.value!!.max, PRECISION_EXP_MINUS_2)
        val c = leaf(restrictedRange!!)
        val log3 = c.log()
        Assertions.assertEquals(0.39, log3.value!!.x0, PRECISION_EXP_MINUS_2)
        Assertions.assertEquals(0.02, log3.value!!.r, PRECISION_EXP_MINUS_2)
        Assertions.assertEquals(0.09, log3.value!!.min, PRECISION_EXP_MINUS_2)
        Assertions.assertEquals(0.68, log3.value!!.max, PRECISION_EXP_MINUS_2)
    }

    @Test
    fun inverse() {
        // Around zero; division by Zero should result in infinity
        val af3_node = range(-2.0 .. 2.0, -1)
        val inv = af3_node.inv()
        Assertions.assertTrue(inv.value!!.isRealsNaN())

        // Infinity should be preserved
        val inf_node = AADD.RealsNaN
        val inv2 = inf_node.inv()
        println("1/inf = $inv2 (shall be inf)")
        Assertions.assertTrue(inv2.value!!.isRealsNaN())

        // Regular
        val inv3_node = leaf(affineForm1!!)
        val inv3 = inv3_node.inv()
        Assertions.assertEquals(0.75, inv3.value!!.x0, PRECISION_EXP_MINUS_6)
        Assertions.assertEquals(0.5, inv3.value!!.min, PRECISION_EXP_MINUS_6)
        Assertions.assertEquals(1.0, inv3.value!!.max, PRECISION_EXP_MINUS_6)
        Assertions.assertEquals(0.125, inv3.value!!.r, PRECISION_EXP_MINUS_6)
        Assertions.assertEquals(0.125, inv3.value!!.radius, PRECISION_EXP_MINUS_6)
    }

    @Test
    fun div() {
        // Div by zero should return infinite
        val zero_node = scalar(0.0)
        val affineForm1_node = leaf(affineForm1!!)
        val div = affineForm1_node.div(zero_node)
        Assertions.assertTrue(div.value!!.isRealsNaN())

        // Regular division
        val a = scalar(10.0)
        val b = scalar(5.0)
        var result = a.div(b)
        Assertions.assertEquals(result.value!!.x0, 2.0)

        //Division tested by inversion + multiplication
        result = a.times(b.inv())
        Assertions.assertEquals(result.value!!.x0, 2.0)
    }

    @Test
    fun checkApply() {
        // Two BDD with different index.
        val ai = range(-1.0, 1.0, 1)
        val tr = scalar(0.1)
        val tr2 = scalar(0.2)
        val c1 = ai.ge(tr)
        val c2 = ai.le(tr2)
        Assertions.assertTrue(c1.index != c2.index)
        val a = scalar(1.0)
        val b = scalar(2.0)
        val d = c1.ite(a, b)
        Assertions.assertTrue(c1.index == d.index)
        Assertions.assertTrue(c1.height() == 1)
        val e = c2.ite(a, b)
        Assertions.assertTrue(c2.index == e.index)
        Assertions.assertTrue(e.height() == c2.height())
        val f = e.plus(d)
        Assertions.assertTrue(f.height() == 2)

        // System.out.println(" d = " + d);
        // System.out.println(" e = " + e);
        // System.out.println(" f = " + f);
    }

    @Test
    fun ITEtest() {
        var a = range(-1.0, 1.0, 1)
        val b = scalar(0.0)
        println("=== Testing ITE function of AADD: ===")
        println("  ITE of an AADD leave with a BDD of height 1 must result in merged AADD with height 1.")
        println("  AADD a = $a")
        println("  AADD b = $b")
        val c = a.ge(b)
        assert(c.height() == 1)
        println("  BDD c = $c")
        a = c.ite(a, b)
        println("  ITE(c, a, b): a=$a")
        Assertions.assertEquals(a.height(), 1)
    }

    @Test
    fun compare1() {
        // create affine form 2+2*e3+2*e4
        var affineForm = AffineForm(-2.0, 2.0, 3)
        println(affineForm)
        affineForm = affineForm.plus(AffineForm(0.0, 4.0, 4))
        println(affineForm)
        val a = leaf(affineForm)
        println("a: $a\n")

        // compares a with 0 (a>=0)
        val bdd1 = a.ge(scalar(0.0))
        println("Result of first comparison:......")
        println("Bdd1: $bdd1")
        val cons1 = bdd1.AFConstr() // constraint of the root of BDD x
        println("Condition:......")
        println("""
    ${cons1.toString()}
    
    """.trimIndent())

        // a<4.0
        val bdd2 = a.lt(scalar(4.0))
        println("Result of second comparison:......")
        println("Bdd2: $bdd2\n")
        var d = AffineForm(0.5)
        d = d.plus(affineForm.times(0.5))
        // System.out.println("d: "+d);

        // Build AADD with ITE
        var add1 = bdd1.ite(a, leaf(d))
        println("AADD1 with ITE: $add1\n")

        // check add1 < 0
        println("Result of third comparison:......")
        println("add1 = $add1")
        val bdd3 = add1.lt(scalar(0.0))
        println("BDD3: $bdd3\n")

        // Create affine form 5.0+10.0*e4+4.0*e3
        val it: Iterator<Int> = affineForm.xi.keys.iterator()
        val terms = HashMap<Int, Double>()
        terms[it.next()] = 4.0
        terms[it.next()] = 10.0
        val affineForm2 = AffineForm(Range.Reals, 5.0, 0.0, terms)
        println("AffineForm2: $affineForm2")

        // Construct AADD with 2+2*e3+2*e4 for bdd3=ONE and 5.0+10.0*e4+4.0*e3 bdd3=ZERO
        add1 = bdd3.ite(a, leaf(affineForm2))
        println("aadd1: $add1")

        // Compute Total Lower and Upper Bounds of add1
        val r = add1.getRange()
        println("BOUNDS of AAD1 COMPUTED: [LB: " + r.min + ", UB: " + r.max + "]")

        // t <=4.0
        val t = range(0.0, 4.0, -1)
        val bdd4 = t.le(scalar(4.0))
        println("BDD4: $bdd4")
    }

    @Test
    fun compare2() {

        // create affine form 2+2*e3+2*e4
        val affineForm1 = AffineForm(-4.0, 4.0, -1)
        val affineForm2 = AffineForm(0.0, 1.0, -1)
        var a = leaf(affineForm1)
        val b = leaf(affineForm2)
        println("a: $a")
        println("b: $b")
        val cond = a gt scalar(0.0) // a > 0.0
        val cond1 = a gt scalar(2.0) // a > 2.0
        var at = a + (b * scalar(0.4)) - scalar(1.5) // value for cond=true at= a+b*0.4-1.5
        var af = a.plus(b.times(scalar(0.4)).minus(scalar(0.4))) // value for cond=false af= a+b*0.4-0.4
        val a1 = cond1.ite(at, af)
        println("a1: $a1")
        val cond2 = a.gt(scalar(-2.0)) // a > -2.0
        at = a.plus(b.times(scalar(0.5)).plus(scalar(0.6))) // at=a+b*0.5+0.6
        af = a.plus(b.times(scalar(0.7)).plus(scalar(1.5))) // af=a+b*0.7+1.5
        val a2 = cond2.ite(at, af)
        println("a2: $a2")
        a = cond.ite(a1, a2)
        println("a: $a")
    }

    @Test
    fun reduceTest() {
        // Two BDD with different index.
        val ai = range(-1.0, 1.0, 1)
        val tr = scalar(0.1)
        val tr2 = scalar(0.2)
        val c1 = ai.ge(tr)
        val c2 = ai.le(tr2)
        val a = range(1.0, 2.0, 1)
        val b = range(2.0, 3.0, 1)
        val d = c1.ite(a, b)
        val e = c2.ite(a, b)
        Assertions.assertTrue(c2.index == e.index)
        Assertions.assertTrue(e.height() == c2.height())
        val f = e.plus(d)
        Assertions.assertTrue(f.height() == 2)
        println("f=$f")
    }

    companion object {
        private const val PRECISION_EXP_MINUS_6 = 0.000001
        private const val PRECISION_EXP_MINUS_2 = 0.01
        private var affineForm1: AffineForm? = null
        private var largerValue: AffineForm? = null
        private var restrictedRange: AffineForm? = null

        @BeforeAll
        fun setUp() {
            affineForm1 = AffineForm(1.0, 2.0, -1)

            // Larger value
            var terms = HashMap<Int, Double>()
            terms[1] = 2.0
            terms[2] = 1.0
            largerValue = AffineForm(Range.Reals, 10.0, 0.0, terms)

            // Restricted range through manual min/max values
            terms = HashMap()
            terms[2] = 0.5
            restrictedRange = AffineForm(Range(1.1, 1.9), 1.5, 0.0, terms)
        }
    }
}