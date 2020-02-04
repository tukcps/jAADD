package jAADD

import jAADD.AADD.Companion.range
import jAADD.AADD.Companion.scalar
import jAADD.BDD.Companion.variable
import jAADD.Conditions.init
import jAADD.Conditions.newBtmConstr
import jAADD.Conditions.newConstraint
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test


internal class BDDTests {
    @BeforeEach
    fun setUp() {}

    @AfterEach
    fun tearDown() {}

    @Test
    fun testCloneLeaves() {
        // ONE should create a shallow copy as clone, not a new object.
        val a = BDD.True
        val b = BDD.False
        val c = a.clone() as BDD
        val d = b.clone() as BDD
        Assertions.assertSame(a, c)
        Assertions.assertSame(b, d)
        Assertions.assertEquals(true, c.value)
        Assertions.assertEquals(false, d.value)
    }

    @Test
    fun testBDDexception() {
        // there shall be an assertion error if we access an index
        // that is not referring to a condition
        init()
        Assertions.assertThrows(AssertionError::class.java)
            {  BDD(100, BDD.True, BDD.False) }
    }

    @Test
    fun testBDDcond() {
        init()

        // int i = AADDMgr.lastIndex;
        var cond: AffineForm? = AffineForm(1.0, 2.0, 1)
        // System.out.println("i:"+BDD.lastIndex);
        val a = BDD(newConstraint(cond!!, ""), BDD.True, BDD.False)
        // System.out.println("i:"+BDD.lastIndex);
        val b = BDD(newBtmConstr(cond, ""), BDD.False, BDD.True)
        cond = AffineForm(2.0, 3.0, -1)
        val c = BDD(newBtmConstr(cond, ""), a, b)

        // System.out.println("i: "+i+"a:"+a + "  b: "+ b + "   C:  " + c);
        Assertions.assertEquals(2, c.height())
        // assertEquals(2, BDD.lastIndex-i);
        Assertions.assertEquals(4, c.numLeaves())
        Assertions.assertEquals(4, c.numLeaves())
    }

    @Test
    fun complement() {
        val cond = newBtmConstr(AffineForm(1.0, 2.0, 1), "")
        val a = BDD(cond, BDD.True, BDD.False)
        val b = BDD(cond, BDD.False, BDD.True)
        val c = a.not()
        Assertions.assertTrue(b.equals(c))
        Assertions.assertFalse(a.equals(b))
    }

    @Test
    fun and() {
        val cond = newBtmConstr(AffineForm(1.0, 2.0, 1), "")
        var a = BDD(cond, BDD.True, BDD.False)
        var b = BDD(cond, BDD.False, BDD.True)
        var expected = BDD.False // via reduction of BDD.
        var r = a.and(b)
        // System.out.println("expected="+expected);
        // System.out.println("result  ="+r);
        Assertions.assertTrue(expected.equals(r))
        a = BDD(cond, BDD.True, BDD.False)
        b = BDD(cond, BDD.True, BDD.True)
        r = a.and(b)
        expected = BDD(cond, BDD.True, BDD.False)
        Assertions.assertTrue(expected.equals(r))
    }

    @Test
    fun BDDConstants() {
        val tru = BDD.True
        val fal = BDD.False
        val c = variable("c")
        val d = fal and c and tru
        val e = fal.or(variable("e"))
        Assertions.assertEquals(false, d.value)
        Assertions.assertEquals(false, fal.value)
        Assertions.assertEquals(true, tru.value)
        println("d = $d; e = $e")
    }

    @Test
    fun ITE() {
        val cond = newBtmConstr(AffineForm(1.0, 2.0, 1), "")
        val b = BDD(cond, BDD.False, BDD.True)
        var r = b.ite(BDD.True, b)
        Assertions.assertTrue(r.equals(b))
        r = b.ite(BDD.False, b)
        Assertions.assertTrue(r.equals(BDD.False))
        println("result = $r")
    }

    @Test
    fun checkApply() {
        // Two BDD with different index.
        val ai = range(-1.0, 1.0, 1)
        val tr = scalar(0.1)
        val tr2 = scalar(0.2)
        val tr3 = scalar(0.3)
        var c1 = ai.ge(tr)
        c1.sanityCheck()
        Assertions.assertEquals(1, c1.height())
        var c2 = ai.le(tr2)
        c2.sanityCheck()
        Assertions.assertEquals(1, c2.height())
        c2 = c1.and(c2)
        c2.sanityCheck()
        Assertions.assertEquals(2, c2.height())
        c2 = c2.or(c2)
        c2.sanityCheck()
        Assertions.assertEquals(2, c2.height())
        c1 = ai.gt(tr3)
        c1 = c1.xor(c2)
        c2.sanityCheck()
        Assertions.assertEquals(3, c1.height())
    }
}
