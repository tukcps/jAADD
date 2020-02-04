package jAADD

import exprParser.ExprError
import exprParser.ExprParser
import exprParser.ParseError
import jAADD.AADD.Companion.Empty
import jAADD.AADD.Companion.Reals
import jAADD.AADD.Companion.range
import jAADD.AADD.Companion.scalar
import jAADD.BDD.Companion.False
import jAADD.BDD.Companion.True
import jAADD.BDD.Companion.constant
import jAADD.BDD.Companion.variable
import jAADD.NiceApi.endS
import jAADD.NiceApi.ifS
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class AADDTutorialKotlin {
    @Test
    fun instantiation() {
        val scalar   = scalar(1.0)
        val interval = range(2.0 .. 3.0, 1) // min, max are doubles, index is int
        val real     = Reals
        val empty    = Empty
        val realNaN  = AADD.RealsNaN
        println("scalar   = $scalar")
        println("interval = $interval")
        println("real     = $real")
        println("empty    = $empty")
        println("realNaN  = $realNaN")
        assertEquals(1.0, scalar.value!!.central)
        assertTrue(interval.value!!.equals(AffineForm(2.0, 3.0, 1)))
    }

    @Test
    fun computation() {
        val a = range(1.0..2.0, "a")
        val b = range(1.0..2.0, "b")
        println("    a-a = " + (a-a))
        println("but a-b = " + (a-b))
    }

    @Test
    fun expression() { // Volume of ellipsoid = 4/3 pi a b c
        val a = range(1.0 .. 10.0, "a")
        val b = range(1.0 .. 10.0, "b")
        val c = range(1.0 .. 10.0, "c")
        val pi = range(3.141 .. 3.142, "pi")
        var vol = scalar(4.0/3.0)*pi*a*b*c
        println("Volume = $vol")
        AADD.toStringVerbose = true
        println("Volume = $vol")
        println("Noise variables: "+NoiseVariables)
        assertEquals(10 * 10 * 10 * 3.142 * 4 / 3, vol.value!!.max)
        assertEquals(3.141 * 4 / 3, vol.value!!.min)
    }

    @Test
    fun PIcontrolDouble() {
        println("\n=== PI controller example with doubles ===")
        val setval: Double = 1.0
        var isval:  Double = 1.0
        var pi_out: Double = 0.0

        for (t in 1..10) {
            pi_out += (setval - isval) * 0.5
            isval = pi_out
            println("  At t=$t, the is-value is: $isval")
        }
    }

    /** Simple PI controller model  */
    @Test
    fun PIcontrolAADD() {
        println("\n=== PI controller example with AADD ===")
        val setval = AADD.scalar(1.0)
        var isval  = AADD.range(0.0, 2.0, "Uncertainty isval")
        var pi_out = AADD.range(0.0, 2.0, "Uncertainty initial state")
        val str = AADDStream("a")
        for (i in 1..10) {
            pi_out += (setval - isval) * 0.5
            isval = pi_out
            println("  At t=$i, the is-value is: $isval")
            str.add(isval, i.toDouble())
        }
        str.display()
        assertEquals(1.0, isval.value!!.x0, 0.01)
    }

    /** Instantiation of some BDD  */
    @Test
    fun BDDinstantiation() {
        val a = constant(true); // gets BDD with Boolean value true or false
        val f = False // Constant leaf with value false
        val t = True
        val X = variable("X") // Constant with value true or false
        System.out.println("a="+a);
        println("f=$f")
        println("t=$t")
        println("X=$X")
        val d = (f and X) or t
        val e = t and X
        println("d=$d, e=$e")
        assertTrue(d.value!!)
        assertTrue(e == X)
    }

    @Test
    fun AssumeGuaranteeExercise() {
        val a: BDD = False
        val b: BDD = variable("b")
        val c: BDD = variable("c")
        val d = a or (b and c.not() )
        println("The property d can be guaranteed if $d")
        assertEquals(2, d.height())
        assertEquals(3, d.numLeaves())
    }

    @Test
    fun Comparison() {
        val a = range(1.0 .. 3.0, "a")
        val b = range(2.0 .. 4.0, "b")
        val c = a gt b
        println("c = " + c)
        assertEquals(1, c.height())
    }

    @Test
    fun Comparison2() {
        val a = range(1.0, 3.0, "a")
        val b = range(2.0, 4.0, "a")
        val c = a gt b
        println("c=$c")
        assertFalse(c.value!!)
    }

    @Test
    fun Comparison3() {
        val a = range(1.0, 3.0, 1)
        val b = range(2.0, 4.0, 2)
        val c = (a * b) gt (a + b)
        println("c=$c")
        assertEquals(1, c.height())
    }

    @Test
    fun JsonExample() {
        val a = range(1.0, 2.0, 4)
        val s = a.toJson()
        println("s = $s")
    }

    /** Example shows the AADD getRange function to call LP solver  */
    @Test fun ITEExample1() {
        var a = range(-1.0 .. 1.0, "a")
        a = (a le scalar(0.0)).ite(a + scalar(2.0), a - scalar(2.0))
        println("a=$a")
        a.getRange()
        println("a=$a")
    }

    @Test @Throws(ExprError::class, ParseError::class)
    fun ExprParserExample1() {
        val p = ExprParser()
        p.eval("var a := aaf(1.0, 2.0, 1)")
        p.eval("var b := range(2.0, 3.0)")
        p.eval("var c := 3.0")
        println("a+b*c=" + p.evalExpr("a+b*c").value)
    }

    @Test
    @Throws(ExprError::class, ParseError::class)
    fun UserFunctionCallsTest() {
        println("\n=== Definition of a user function ===")
        val p = ExprParser()
        p.eval("fun f(x) := x*x*x")
        println("    fun f(x) := x*x*x; f(2) = " + p.eval("f(2)"))
        assertEquals(8.0, p.evalAADD().value?.central)
    }

    @Test
    fun CAVexampleArithmetic() {
        var a = range(1.0 .. 3.0, "a")    // [1, 3]; uses noise symbol w/ index 1
        var b = range(0.0 .. 2.0, "b")   // [0, 2]; uses noise symbol w/ index 1
        var x = a*b
        var y = a-b
        println("a*b=$x and a-b=$y")
    }

    @Test
    fun CAVexampleControlFlow() {
        var x = range(1.0 .. 3.0, "x")   // [1, 3]; uses noise symbol w/ index 1
        var y = range(1.0 .. 2.0, "y")   // [0, 2]; uses noise symbol w/ index 1

        ifS ((x * y) gt x.exp() )
            x = x.assignS(x- scalar(1.5))
        endS()

        println("x=$x")
        if(0.0 .. 0.9 in x) println("feasible")
        else println("infeasible")
    }

}
