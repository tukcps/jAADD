package jAADD

import com.google.gson.GsonBuilder
import jAADD.AADD.Companion.range
import jAADD.AADD.Companion.scalar
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class AADDstreamTest {
    var a: AffineForm? = null
    var b: AffineForm? = null
    var c: AffineForm? = null
    var aa: AADD? = null
    var bb: AADD? = null
    var cc: AADD? = null
    var dd: AADD? = null
    var cond: BDD? = null
    var gson = GsonBuilder().setPrettyPrinting().create()

    @BeforeEach
    fun setUp() {
        // System.out.println("=== IOManagerTests: SetUp ===");
        a = AffineForm(1.0, 3.0, 1)
        b = AffineForm(2.0, 4.0, 2)
        c = AffineForm(3.0, 5.0, 3)
        aa = range(1.0, 3.0, 1)
        bb = range(2.0, 4.0, 2)
        cc = range(2.0, 5.0, 3)
        cond = aa!! gt scalar(2.0) // a > 0.0
        dd = cond!!.ite(aa!!, bb!!)

        // System.out.println("condheight =" + cond.toString());
        // System.out.println("condheight =" + dd.toString());
        Assertions.assertEquals(cond!!.height(), 1)
        Assertions.assertEquals(dd!!.height(), 1)
    }
}