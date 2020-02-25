package examples

import exprParser.ExprParser
import exprParser.ExprTreeConstrNet
import jAADD.AADD
import jAADD.AADDStream
import jAADD.display
import jAADD.displayTree
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

object jAADDdemo {

    fun piControlAADD() {
        println("\n=== PI controller example with AADD ===")
        val setval = AADD.range(0.4 .. 0.6)
        var isval  = AADD.range(0.9 .. 1.0)
        var pi_out = AADD.range(0.5 .. 0.51)
        val t = AADDStream("isval")
        var inval: AADD
        for (i in 1..100) {
            inval = setval - isval
            pi_out += inval * 0.05
            isval = isval * 0.5 + pi_out * 0.5
            t.add(isval, i.toDouble())
        }
        t.display()
    }

    /** ConstNet shall compute buttom-up with ranges. */
    fun buttomUpWithRanges() {
        AADD.toStringVerbose = true
        var p = ExprParser()
        p.defVar("a", AADD.Reals)
        p.defVar("b", AADD.Reals)
        p.defVar("c", AADD.Reals)

        p.expr = "eqn a == b+c*d"
        p.defVar("b", AADD.range(1.0..2.0))
        p.defVar("c", AADD.range(2.0..3.0))
        p.defVar("d", AADD.range(3.0..4.0))

        
        (p.AST as ExprTreeConstrNet).solve(4)
        println("AST.value="+ (p.AST as ExprTreeConstrNet).value)
        println("AST="+ p.AST)
        println("a="+p.getVar("a"))
        println("b="+p.getVar("b"))
        println("c="+p.getVar("c"))
        println("d="+p.getVar("d"))

        // center depends on approximation schemes; might cause incorrect fault iff changed.
        display(p.getVar("AADD a:").value, "a")
        Assertions.assertEquals(10.25, (p.getVar("a").value as AADD).value!!.central)
    }

    @Throws(Exception::class)
    @JvmStatic
    fun main(args: Array<String>) {
        buttomUpWithRanges()
    }
}