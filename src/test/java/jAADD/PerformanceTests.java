package jAADD;

import org.junit.jupiter.api.Test;

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
        AADD outrate = new AADD(-1, -0.6, 1, "Uoutrate", "l/sec", "Symbol models uncertain amount of water leaving the tank if pump removes water");
        AADD inrate  = new AADD( 0.6, 1, 2, "Uinrate", "l/sec", "Symbol models uncertain amount of water entering the tank if pump fills it with water");

        // the starting state. 5 l are inside, it's filling with inrate.
        AADD level = new AADD(1, 11, 3,  "Ulevel", "l", "Uncertainty in  amount of water in the tank.");
        AADD rate = BDD.BOOL.ITE(inrate, outrate);

        for (Double time = 0.0; time < 60; time = time + 1.0) {     // Sampled over 25 sec.

            Range wl = level.getRange();                // Solves LP to compute accurate bounds of AADD.
            AADDMgr.addAADDSample("level", level, time); // Writes level into stream of samples of AADD.

            // Some tracing.
            System.out.print("  At time: " + time + " sec. ");
            double lb = wl.getMin();
            double ub = wl.getMax();
            System.out.println("water level is: "+wl);

            // Testing some invariants of the model.
            assertTrue(lb > 0.1); // Invariant: no empty tank.
            assertTrue(ub < 11.9);// Invariant: no overflow of tank.

            BDD too_high = level.Gt(new AADD(10.0));      // Checks for upper threshold.
            BDD too_low = level.Lt(new AADD(2.0));       // Checks for lower threshold.

            rate = too_high.ITE(outrate, rate);            // If Ge, then set pump to outrate, else keep rate.
            rate = too_low.ITE(inrate, rate);             // If Lt, then set pump to inrate, else keep rate.

            // To be correct, we would have to compute change = rate * time step (numerical integration).
            // We should also consider the inaccuracy of discrete integration fault.
            // change = rate.mult(new AADD(1.0) );
            level = level.add(rate);                         // The level has changed in 1 sec. with rate.
        }

        // DD.io.writeToJson(); // Writes results of tracing into file level.json.
        // System.out.println();
        // DD.symbolMgr.PrintInfo();   // Prints summary to screen.

        // For Debug uncomment the following to print Json to screen:
        // Gson gson = new Gson();
        // System.out.println("JSON string of level: " + gson.toJson(level););
    }

    @Test
    void waterlevelDoubleTest() {
        System.out.println("==== Stupid water level monitor with double numbers ====\n");

        // some constants with uncertain value.
        double outrate = -0.8;
        double inrate  = 0.8;

        // the starting state. 5 l are inside, it's filling with inrate.
        double level = 4.5;
        double rate = inrate;

        // for discrete-time approximation of a time step
        double change;

        for (Double time = 0.0; time < 25; time = time + 1.0) {     // Sampled over 25 sec.

            // Some tracing.
            System.out.print("  At time: " + time + " sec. ");
            System.out.println("water level is: "+level);

            // The obvious invariants that must hold.
            assertTrue(level > 0.5);
            assertTrue( level < 11.5);

            // Testing some invariants of the model, could be from AADD-Simulation !
            // assertTrue(level >= level.bounds.getLb() ); // Invariant: no empty tank.
            // assertTrue(level <= level.bounds.getUb()  );// Invariant: no overflow of tank.

            // level.io.addAADDSample("level", level, time); // Writes level into stream of samples of AADD.

            Boolean too_high = ( level >= 10.0) ; // Checks for upper threshold.
            if ( too_high ) rate = outrate;       // If Ge, then set pump to outrate, else keep rate.
            else rate = rate;

            Boolean too_low = ( level < 2.0) ;   // Checks for lower threshold.
            if (too_low) rate = inrate;          // If Lt, then set pump to inrate, else keep rate.
            else rate = rate;

            // to be correct, we would have to compute change = rate * time step (numerical integration).
            // to be correct, we should also consider the inaccuracy of discrete integration fault.
            change = rate * 1.0;
            level = level + change;                         // The level has changed in 1 sec. with rate.
        }
    }
}
