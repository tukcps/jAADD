package jAADD

import jAADD.AADD.Companion.range
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AADDTrapTests {

    @Test
    fun arithmeticOperationsWithEmpty() {
        println("=== Testing: Operations with operands that are Empty set ===")

        // Arithmetic operations with one operand Empty shall return Empty
        val a = AADD.Empty
        val b = range(1.0, 2.0, -1)
        var c = a + b
        assertTrue(c === AADD.Empty)
        c = a * b
        assertTrue(c === AADD.Empty)
        c = a / b
        assertTrue(c === AADD.Empty)
        c = b + a
        assertTrue(c === AADD.Empty)

        // No existing number can be divided by whatever ... Result must be empty.
        c = AADD.Empty / AADD.Reals
        assertTrue(c === AADD.Empty)

        // But: A number divided by all possible Reals can result in any of the reals, but as well infinity.
        //      We include NaN in the (extended) Reals, including infinity.
        c = b/AADD.Reals
        assertTrue(c !== AADD.Reals)
        assertTrue(c !== AADD.Empty)
        assertTrue(c.getRange().min == Double.NEGATIVE_INFINITY && c.getRange().max == Double.POSITIVE_INFINITY)
    }

    @Test
    fun relationOperationsWithEmpty() {
        println("=== Testing: Relations with operands that are Empty set ===")

        // Relations with BDD shall be marked as "Infeasible"; best match for result.
        val b = range(1.0, 2.0, -1)
        var c = AADD.Empty gt b
        println("c="+c.toJson())
        assertTrue(c.isInfeasible)
    }
}