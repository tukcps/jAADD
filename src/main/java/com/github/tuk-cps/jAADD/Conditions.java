package jAADD;

import java.util.HashMap;
import java.util.Map;

/**
 * The Class Conditions implements the sets of conditions X and Xb.
 * The conditions are global (static) and shared by all BDD and AADD.
 * There are two types of conditions:
 * Unknown Boolean variables and Relational operations.
 *
 * The relational operations are, for each index, each of the form
 *  {@code AffineForm >= 0, with -1 <= ei <= 1}
 * They are saved in the hashmap X and shared among all AADD/BDD.
 *
 * The unknown Boolean variables are saved in a hashmap Xb and shared among all AADD/BDD.
 * The unknown Boolean variables are modeled by the enum BoolX.
 */
class Conditions {

    enum BoolX { ONE, ZERO, X};

    protected static int index = 1;     // last index used for increasing index selection.
    protected static int btmIndex = 0;  // last index used for decreasing index selection.
    protected static HashMap<Integer, AffineForm> X = new HashMap<>();
    protected static HashMap<Integer, BoolX> Xb = new HashMap<>(Map.of(1, BoolX.X));

    /**
     * The method gets the condition x_i from X
     * @param i index of X
     * @return x_i if x_i is an affine form, otherwise null.
     */
    public static AffineForm getX(int i) {
        assert btmIndex <= i;
        assert i <= index : "index out of range accessed: "+i;
        assert i != Integer.MIN_VALUE;
        return X.get(i);
    }


    /**
     * The method gets the condition x_i from X
     * @param i
     * @return x_i if x_i is a boolean variable, otherwise null.
     */
    public static AffineForm getXBool(int i) {
        assert btmIndex <= i;
        assert i <= index : "index out of range accessed: "+i;
        assert i != Integer.MIN_VALUE;
        return X.get(i);
    }

    public static int newIndex(AffineForm c) {
        X.put(++index, c.clone());
        return index;
    }

    public static int newIndex(BoolX b) {
        Xb.put(++index, b);
        return index;
    }

    public static int newBtmIndex(AffineForm c) {
        X.put(--btmIndex, c.clone());
        return btmIndex;
    }

    public static void reset() {
        index = 0;
        btmIndex = 0;
        X = new HashMap<>();
        Xb = new HashMap<>();
    }
}
