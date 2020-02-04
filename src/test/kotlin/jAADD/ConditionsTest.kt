package jAADD

import jAADD.Conditions.btmIndex
import jAADD.Conditions.topIndex
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import jAADD.NoiseVariables.noiseVar

class ConditionsTest {

    val a = AADD.range(1.0, 3.0, -1)
    val b = AADD.range(2.0, 4.0, noiseVar("Symbol of b"))
    val c = AADD.range(3.0, 5.0, "Symbol o c")


    @Test
    fun createCondTest() {
        println("=== Testing: Creating conditions and writing to Json file ===")

        Conditions.init()
        val cond = a ge b
        Assertions.assertEquals(1,topIndex)
        println("        "+cond)
        val cond2 = b le c
        println("        "+cond2)
        Assertions.assertEquals(2, topIndex)

        Conditions.newVariable("unknown Boolean")
        Assertions.assertEquals(3, topIndex)
        var directory = java.io.File("out");
        if (! directory.exists()) directory.mkdir()
        Conditions.toJson("out/Conditions.json")
    }

   @Test
    fun readCondTest() {
        println("=== Testing: Reading conditions from Json file ===")

        Conditions.init()
        Conditions.fromJson("out/Conditions.json")
        Assertions.assertEquals(3, Conditions.X.size )
        Assertions.assertEquals(3, topIndex)
        Assertions.assertEquals(0, btmIndex)
    }
}