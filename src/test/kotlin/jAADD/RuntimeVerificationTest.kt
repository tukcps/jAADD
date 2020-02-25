package jAADD
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import jAADD.NiceApi.ifS as ifS
import jAADD.NiceApi.endS as endS
import jAADD.AADD.Companion.range
import jAADD.AADD.Companion.scalar
import jAADD.BDD.Companion.variable
import jAADD.BDD.Companion.True


class RuntimeVerificationTest {

    @Test
    fun runtimeVerificationTst() {
        println("==== Stupid water level monitor runtime verification test ====")
        // some constants with uncertain value.
        val outrate = range(-1.0 .. -0.6, "outrate")
        val inrate  = range(0.6 .. 1.0, "inrate")
        var level   = range(1.0 .. 11.0, "level")
        var rate    = variable("initial direction").ite(inrate, outrate)
        var drate   = 0.9
        var dlevel  = 4.0
        var inrange  = True
        var rlevel  = range(1.0 .. 11.0, -1)
        var time = 0.0
        while (time < 40) {
            // Sampled over time sec.
            run {
                // Discrete fault:
                // if (time > 22) dlevel = 2.0
                print("  At time: $time sec. ")
                println("physical water level is: " + String.format("%.2f", dlevel))
                if (dlevel >= 10.0) drate = -.8
                if (dlevel < 2.0) drate = 0.9
                // Parametric fault:
                // if (time >= 19 && drate == 0.9) drate = 0.5
                dlevel += drate
            }
            run {
                println("                  LEVEL is: " + level.getRange() + " and has leaves: " + level.numLeaves())
                // Check the discrete state ...
                inrange = (level ge scalar(dlevel)) and (level le scalar(dlevel)) and inrange

                // ... or better: just check with intersect:
                rlevel = rlevel.intersect(dlevel - 0.01 .. dlevel + 0.01)
                Assertions.assertTrue(level in 0.9 .. 11.1)
                ifS(level gt AADD.scalar(10.0))
                    rate = rate.assignS(outrate)
                endS()
                ifS(level le AADD.scalar(2.0))
                    rate = rate.assignS(inrate)
                endS()
                println("                  feasible paths:  " + inrange.numTrue() + " that match physical data.")
                println("                  feasible leaves: " + rlevel.numFeasibleLeaves() + " with Range: " + rlevel.getRange())
                level += rate
                rlevel += rate
            }
            time += 1.0
        }
    }
}
