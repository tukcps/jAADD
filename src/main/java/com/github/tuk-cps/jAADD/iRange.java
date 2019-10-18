package jAADD;
/**
 * The package jAADD implements an Affine Arithmetic Decision Diagram.
 *
 * @author Christoph Grimm, Carna Zivkovic
 */

/**
 * A class that is a range from a lower to an upper bound of the Integers.
 */

public class iRange {
    protected Integer lb, ub;

    // Constructors
    iRange (Integer l, Integer u) {lb = u; ub = u; }

    // Some operations.
    void add(iRange r) {
        lb= r.lb + lb;
        ub= r.ub + ub;
    }

    void join(iRange r) {
        lb = Math.min(r.lb, lb);
        ub = Math.max(r.ub, ub);
    }

    void intersect(iRange r) {
        lb = Math.max(r.lb, lb);
        ub = Math.min(r.ub, ub);
    }

    Boolean isEmptySet() { return lb > ub; }
}
