package jAADD
import exprParser.ExprParser
import exprParser.ExprTreeConstrNet

import jAADD.AADD.Companion.scalar
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Work in progress for future release.
 */
class ConstraintNetTest {



    /** ConstNet shall compute buttom-up with scalars. */
    @Test
    fun buttomUpWithScalars() {
        var p = ExprParser()
        p.defVar("a", AADD.Reals)
        p.defVar("b", AADD.Reals)
        p.defVar("c", AADD.Reals)

        p.expr = "eqn a == b+c*d"
        p.defVar("b", AADD.scalar(1.0))
        p.defVar("c", AADD.scalar(2.0))
        p.defVar("d", AADD.scalar(3.0))

        (p.AST as ExprTreeConstrNet).solve(3)
        println("AST="+ p.AST)
        println("a="+p.getVar("a"))
        println("b="+p.getVar("b"))
        println("c="+p.getVar("c"))
        println("d="+p.getVar("d"))
        assertEquals(7.0, (p.getVar("a").value as AADD).value!!.central)
    }


    /** ConstNet shall compute buttom-up with ranges. */
    @Test
    fun buttomUpWithRanges() {
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
        // only outside tests display((displayTree("a", p.getVar("a").value)))
        assertEquals(10.25, (p.getVar("a").value as AADD).value!!.central)
    }

    /** ConstNet shall compute top-down with scalars. */
    @Test
    fun topDownWithScalars() {
        var p = ExprParser()
        p.defVar("a", AADD.Reals)
        p.defVar("b", AADD.Reals)
        p.defVar("c", AADD.Reals)

        p.expr = "eqn a == b+c*d"
        p.defVar("a", AADD.scalar(1.0))
        // B is now unknown REAL => a-c*d = 1-2*3 = -5
        p.defVar("c", AADD.scalar(2.0))
        p.defVar("d", AADD.scalar(3.0))

        (p.AST as ExprTreeConstrNet).solve(3)
        println("AST="+ p.AST)
        println("a="+p.getVar("a"))
        println("b="+p.getVar("b"))
        println("c="+p.getVar("c"))
        println("d="+p.getVar("d"))
//        assertEquals(-5.0, (p.getVar("b").value as AADD).value!!.central)
    }


    /** ConstNet shall compute buttom-up with ranges. */
    @Test
    fun topDownWithRanges() {
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
        assertEquals(10.25, (p.getVar("a").value as AADD).value!!.central)
    }
}