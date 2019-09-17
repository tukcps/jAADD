package jAADD;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import static jAADD.AADDMgr.ONE;
import static jAADD.AADDMgr.ZERO;
import static jAADD.AADDMgr.BOOL;

class BDDTest {

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    public void testCloneLeaves()  {
        // ONE should create a shallow copy as clone, not a new object.
        BDD a = ONE;
        BDD b = ZERO;
        BDD c = a.clone();
        BDD d = b.clone();

        assertSame(a, c);
        assertSame(b, d);
        assertEquals(true, c.Value());
        assertEquals(false, d.Value());
    }

    @Test
    public void testBDDexception() {
        // there shall be an assertion error if we access an index
        // that is not referring to a condition
        AADDMgr.resetConditions();
        Throwable exception = assertThrows(java.lang.AssertionError.class,
                () -> {
                    BDD a = new BDD(1, ONE, ZERO);
                });
    }


    @Test
    public void testBDDcond()
    {
        AADDMgr.resetConditions();
        BDD dummy = BOOL;

        // int i = AADDMgr.lastIndex;

        AffineForm cond = new AffineForm(1.0, 2.0, 1);
        // System.out.println("i:"+BDD.lastIndex);
        BDD a = new BDD(AADDMgr.newTopIndex(cond), ONE, ZERO);
        // System.out.println("i:"+BDD.lastIndex);
        BDD b = new BDD(AADDMgr.newBtmIndex(cond), ZERO, ONE);

        cond  = new AffineForm(2,3, -1);
        BDD c = new BDD(AADDMgr.newBtmIndex(cond), a, b);

        // System.out.println("i: "+i+"a:"+a + "  b: "+ b + "   C:  " + c);
        assertEquals(2, c.height());
        // assertEquals(2, BDD.lastIndex-i);
        assertEquals(4, c.numLeaves());
        assertEquals(4, c.numLeaves());
    }


    @Test
    void complement() {
        int cond = AADDMgr.newBtmIndex(new AffineForm(1.0, 2.0, 1));
        BDD a = new BDD(cond, ONE, ZERO);
        BDD b = new BDD(cond, ZERO, ONE);
        BDD c = a.negate();
        assertTrue(b.equals(c));
        assertFalse(a.equals(b));
    }

    @Test
    void and() {
        int cond = AADDMgr.newBtmIndex(new AffineForm(1.0, 2.0, 1));
        BDD a = new BDD(cond, ONE, ZERO);
        BDD b = new BDD(cond, ZERO, ONE);
        BDD expected = ZERO; // via reduction of BDD.
        BDD r = a.and(b);
        // System.out.println("expected="+expected);
        // System.out.println("result  ="+r);
        assertTrue(expected.equals(r));

        a = new BDD(cond, ONE, ZERO);
        b = new BDD(cond, ONE, ONE);
        r = a.and(b);
        expected = new BDD(cond, ONE, ZERO);

        assertTrue(expected.equals(r));
    }

    @Test
    void ITE() {
        int cond = AADDMgr.newBtmIndex(new AffineForm(1.0, 2.0, 1));
        BDD a = new BDD(cond, ONE, ZERO);
        BDD b = new BDD(cond, ZERO, ONE);
        BDD r = b.ITE(ONE, b);
        assertTrue(r.equals(b));
        r = b.ITE(ZERO, b);
        assertTrue(r.equals(ZERO));
        System.out.println("result = "+r);
    }

    @Test
    void checkApply() {
        // Two BDD with different index.
        AADD ai = new AADD(-1, 1, 1);
        AADD tr = new AADD(0.1);
        AADD tr2 = new AADD(0.2);
        AADD tr3 = new AADD(0.3);
        BDD  c1 = ai.Ge(tr);
        c1.sanityCheck();
        assertTrue(c1.height() == 1);
        BDD  c2 = ai.Le(tr2);
        c2.sanityCheck();
        assertTrue(c2.height() == 1);
        c2 = c1.and(c2);
        c2.sanityCheck();
        assertTrue(c2.height() == 2);
        c2 = c2.or(c2);
        c2.sanityCheck();
        assertTrue( c2.height() == 2);
        c1 = ai.Gt(tr3);
        c1 = c1.xor(c2);
        c2.sanityCheck();
        assertTrue(c1.height() == 3);
    }


}
