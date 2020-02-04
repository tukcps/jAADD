// package examples

import jAADD.*
import jAADD.NiceApi.endS
import jAADD.NiceApi.ifS
import org.junit.jupiter.api.Assertions

/**
 * Some examples from a recent tutorial on AADD.
 */
class AADDTutorial {
    fun instantiation() {
        val scalar   = AADD.scalar(1.0)
        val range    = AADD.range(2.0..3.0, "r")
        val real     = AADD.Reals
        val empty    = AADD.Empty
        val realNaN  = AADD.RealsNaN
        println("scalar   = $scalar")
        println("interval = $range")
        println("real     = $real")
        println("empty    = $empty")
        println("realNaN  = $realNaN")
    }

    fun computation() {
        val a = AADD.range(1.0..2.0, "a")
        val b = AADD.range(1.0..2.0, "b")
        println("    a-a = " + (a-a))
        println("but a-b = " + (a-b))
    }

    fun expression() { // Volume of ellipsoid = 4/3 pi a b c
        val a = AADD.range(1.0..10.0, "a")
        val b = AADD.range(1.0..10.0, "b")
        val c = AADD.range(1.0..10.0, "c")
        val pi = AADD.range(3.141..3.142, "pi")
        var vol = AADD.scalar(4.0 / 3.0) *pi*a*b*c
        println("Volume = $vol")
        AADD.toStringVerbose = true
        println("Volume = $vol")
        println("Noise variables: "+ NoiseVariables)
    }

    fun piControlDouble() {
        println("\n=== PI controller example with double ===")
        val setval = 0.5
        var isval = 1.0
        var piout = 0.4
        var inval: Double
        for (i in 1..50) {
            inval = setval - isval
            piout += inval * 0.05
            isval = isval * 0.5 + piout * 0.5
        }
    }

    fun piControl() {
        println("\n=== PI controller example with AADD ===")
        val setval = AADD.range(0.4 .. 0.6)
        var isval  = AADD.range(0.9 .. 1.0)
        var piout  = AADD.range(0.5 .. 0.51)
        val t = AADDStream("isval")
        var inval: AADD
        for (i in 1..50) {
            inval = setval - isval
            piout += inval * 0.05
            isval = isval * 0.5 + piout * 0.5
            println(" At t=$i, isval: ${isval.getRange()}, " +
                    "setval: ${setval.getRange()}, pi: ${piout.getRange()}")
            t.add(isval, i.toDouble())
        }
        t.display()
    }

    /** Instantiation of some BDD  */
    fun BDDinstantiation() {
        val a = BDD.constant(true); // gets BDD with Boolean value true or false
        val f = BDD.False // Constant leaf with value false
        val t = BDD.True
        val X = BDD.variable("X") // Constant with value true or false
        System.out.println("a="+a);
        println("f=$f")
        println("t=$t")
        println("X=$X")
        val d = (f and X) or t
        val e = t and X
        println("d=$d, e=$e")
        Assertions.assertTrue(d.value!!)
        Assertions.assertTrue(e == X)
    }

    fun comparison() {
        val a = AADD.range(1.0..3.0, "a")
        val b = AADD.range(2.0..4.0, "b")
        val c = a gt b
        println("c = " + c)
    }

    fun comparison2() {
        val a = AADD.range(1.0 .. 3.0, "a")
        val b = AADD.range(2.0 .. 4.0, "a")
        val c = a gt b
        println("c=$c")
    }

    fun comparison3() {
        val a = AADD.range(1.0 .. 3.0, 1)
        val b = AADD.range(2.0 .. 4.0, 2)
        val c = (a * b) gt (a + b)
        println("c=$c")
        c.display("c")
    }

    fun jsonExample() {
        val a = AADD.range(1.0, 2.0, 4)
        val s = a.toJson()
        println("s = $s")
    }

    fun cavExampleArithmetic() {
        var a = AADD.range(1.0..3.0, "a")    // [1, 3]; uses noise symbol w/ index 1
        var b = AADD.range(0.0..2.0, "b")   // [0, 2]; uses noise symbol w/ index 1
        var x = a*b
        var y = a-b
        println("a*b=$x and a-b=$y")
        display(displayTree(x, "AADD x"), displayTree(y,"AADD y"))
    }

    fun cavExampleControlFlow() {
        var x = AADD.range(1.0..3.0, "x")   // [1, 3]; uses noise symbol w/ index 1
        var y = AADD.range(1.0..2.0, "y")   // [0, 2]; uses noise symbol w/ index 1

        ifS((x * y) gt x.exp())
        x = x.assignS(x- AADD.scalar(1.5))
        endS()
        println("x=$x")
        display(displayTree(x, "AADD x"))
    }
}

fun main() {
    val tutorial = AADDTutorial()
    tutorial.BDDinstantiation()
    tutorial.cavExampleArithmetic()
    tutorial.cavExampleControlFlow()
    tutorial.piControl()
}