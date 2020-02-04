package jAADD

import jAADD.AADD.Companion.scalar
import jAADD.NiceApi.ifS
import jAADD.NiceApi.endS
import jAADD.AADD.Companion.range
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertTrue


class PerformanceTestsKotlin {
    /**
     * Stress test: Discrete-time water level monitor.
     * - level is the water level of a water container.
     * - It shall stay in the bounds of 0.5 (never empty) to 11.5 (never overflow).
     * - Sensors check for lower bound 2.0 and upper bound 10.0 of level.
     * - If level <= 2.0, pump fills in water with rate in_rate.
     * - If level >= 10.0, pump removes water with rate out_rate.
     */
    @Test
    fun waterlevelStressTest() {
        println("==== Stupid water level monitor as stress test ====")
        val outrate = range(-1.0 .. -0.6, "uncertainty of outrate")
        val inrate  = range(0.6 .. 1.0, "uncertainty of inrate")
        var level   = range(4.0 .. 5.0, "uncertainty of initial level.")

        var rate = inrate
        for  (time in 0 .. 30) {
            println("  At time: $time sec. water level is: "+level.getRange())
            assertTrue(level in 0.99 .. 11.01)
            ifS (level gt scalar(10.0) )
                rate = rate.assignS(outrate); endS()
            ifS (level lt scalar(2.0) )
                rate = rate.assignS(inrate); endS()
            level += rate
        }
    }

    @Test
    fun waterlevelDoubleTest() {
        println("==== Stupid water level monitor with double numbers ====\n")
        // some constants with uncertain value.
        val outrate = -0.8
        val inrate = 0.8
        // the starting state. 5 l are inside, it's filling with inrate.
        var level = 4.5
        var rate = inrate
        // for discrete-time approximation of a time step
        var time = 0.0
        while (time < 25) {
            println("     At time: $time sec. water level is: $level")
            Assertions.assertTrue(level in 0.9 .. 11.1)
            if (level >= 10.0)
                rate = outrate   // If Ge, then set pump to outrate, else keep rate.
            if (level < 2.0)
                rate = inrate    // If Lt, then set pump to inrate, else keep rate.
            level = level + rate // The level has changed in 1 sec. with rate.
            time = time + 1.0
        }
    }
}



