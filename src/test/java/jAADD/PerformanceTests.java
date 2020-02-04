package jAADD;

import org.junit.jupiter.api.Test;

import static jAADD.NiceApi4Java.assignS;
import static jAADD.NiceApi.ifS;
import static jAADD.NiceApi.endS;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PerformanceTests {
    /**
     * Stress test: Discrete-time water level monitor.
     *
     * - level is the water level of a water container.
     * - It shall stay in the bounds of 0.5 (never empty) to 11.5 (never overflow).
     * - Sensors check for lower bound 2.0 and upper bound 10.0 of level.
     * - If level <= 2.0, pump fills in water with rate in_rate.
     * - If level >= 10.0, pump removes water with rate out_rate.
     */
    @Test
    void waterlevelStressTest() {
        System.out.println("==== Stupid water level monitor as stress test ====");

        // some constants with uncertain value.
        AADD outrate = AADD.range(-1, -0.6, "Uncertainty of outrate");
        AADD inrate  = AADD.range( 0.6, 1, "Uncertainty of inrage");
        AADD level   = AADD.range(4, 5, "Uncertainty in initial water level");

        AADD rate = inrate;

        for (Double time = 0.0; time < 50; time = time + 1.0) {     // Sampled over 50 time steps

            Range wl = level.getRange();
            System.out.println("  At time: " + time + " sec. water level is: "+wl);
            assertTrue(wl.getMin() > 0.9 && wl.getMax() < 11.1);

            ifS(level.gt(AADD.scalar(10.0)));
                rate = assignS(rate, outrate);
            endS();
            ifS(level.lt(AADD.scalar(2.0)));
                rate = assignS(rate, inrate);
            endS();

            // To be correct, we would have to compute change = rate * time step (numerical integration).
            // We should also consider the inaccuracy of discrete integration fault.
            // change = rate.mult(new AADD(1.0) );
            level = level.plus(rate);                         // The level has changed in 1 sec. with rate.
        }
    }
}
