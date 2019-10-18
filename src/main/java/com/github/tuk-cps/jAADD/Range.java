package jAADD;

/**
 * The package jAADD implements an Affine Arithmetic Decision Diagram.
 *
 * @author Christoph Grimm, Carna Zivkovic
 */

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A class that is a range from a lower to an upper bound of the Reals.
 */
public class Range implements Cloneable {

    /**
     * min and max are the overall interval bounds.
     */
    protected double min;
    protected double max;

    /**
     * Different types of special cases: <ul>
     *   <li>SCALAR is a floating point number.
     *   <li>FINITE is an affine form.
     *   <li>INFINITE is an overflow.
     *   <li>NaN is Not-A-Number.</ul>
     */
    public enum Trap {
        SCALAR, FINITE, INFINITE, NaN
    }

    Trap trap;

    /**
     * Creates a range from min to max and checks whether the ranges is a NaN, Infinite, Scalar, or
     * finite range.
     * @param min lower bound of the range.
     * @param max upper bound of the range, bus be larger than min.
     */
    Range(double min, double max) {
        if (Double.isNaN(max) || Double.isNaN(min)) setNaN();
        else if (Double.compare(min, max) > 0) setNaN();
        else if (Double.isInfinite(min) || Double.isInfinite(max)) setInfinity();
        else if (Double.compare(min, max) == 0) this.trap = Trap.SCALAR;
        else {
            this.trap = Trap.FINITE;
            this.min = min;
            this.max = max;
        }
    }

    Range(Range other) {
        this.trap = other.trap;
        this.min  = other.min;
        this.max  = other.max;
    }

    Range(double c) {
        if (Double.isNaN(c) || Double.isInfinite(c))
            setInfinity();
        else {
            this.min = c;
            this.max = c;
            this.trap = Trap.SCALAR;
        }
    }


    Range(Trap kind) {
        if (kind == Trap.NaN) setNaN();
        else if (kind == Trap.INFINITE) setInfinity();
        else {
            this.trap = Trap.FINITE;
            min=Double.NEGATIVE_INFINITY;
            max=Double.POSITIVE_INFINITY;
        }
    }


    public void setInfinity() {
        trap = Trap.INFINITE;
        min = Double.NEGATIVE_INFINITY;
        max = Double.POSITIVE_INFINITY;
    }

    public void setNaN() {
        trap = Trap.NaN;
        min = Double.POSITIVE_INFINITY;
        max = Double.NEGATIVE_INFINITY;
    }

    /**
     * The method returns a new range of the type defined by the trap-type of this and other.
     * It is used to check traps in binary operations.
     * The calling method must correctly set the range's values.
     * @param other The other parameter of a binary operation.
     * @return A range with the type set appropriately.
     */
    Range handleTrap(Range other) {
        if (this.trap == Trap.NaN || other.trap == Trap.NaN) return new Range(Trap.NaN);
        if (this.trap == Trap.INFINITE || other.trap == Trap.INFINITE) return new Range(Trap.INFINITE);
        if ( (this.trap == Trap.SCALAR) && (other.trap == Trap.SCALAR)) return new Range(Trap.SCALAR);
        return new Range(Trap.FINITE);
    }


    /**
     * The method checks if one of the parameters of a binary operations is a trap.
     * If so, it returns true.
     * @param other The 2nd parameter of a binary operation.
     * @return True, if one of the parameters is NaN or Infinite.
     */
    public boolean isTrap(Range other) {
        return this.trap == Trap.NaN || other.trap == Trap.NaN
                || this.trap == Trap.INFINITE || other.trap == Trap.INFINITE;
    }

    public boolean isTrap(double other) {
        return this.isTrap() || Double.isNaN(other) || Double.isInfinite(other);
    }

    public boolean isTrap() {
        return this.trap == Trap.NaN || this.trap == Trap.INFINITE;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    // Some operations on ranges.
    Range add(Range other) { return new Range(min+other.min, max+other.max); }
    Range sub(Range other) { return new Range(min-other.min, max-other.max); }
    Range mul(Range other) {
        List<Double> iaMult = Arrays.asList(min * other.min, min * other.max, max * other.min, max * other.max);
        return new Range(Collections.min(iaMult), Collections.max(iaMult));
    }
    Range join(Range r) { return new Range(Math.min(r.min, min), Math.max(r.max, max)); }
    Range intersect(Range r) { return new Range(Math.max(r.min, min), Math.min(r.max, max)); }
    Boolean isEmptyRange() { return min > max; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Range other = (Range) obj;
        return other.min == min && other.max == max && other.trap == trap;
    }

    @Override
    public String toString() {
        if (trap == Trap.NaN) return "NaN";
        else if (trap == Trap.INFINITE) return "INF";
        else if (trap == Trap.FINITE) return "["+ String.format("%.2f", min) + "; " + String.format("%.2f", max) + "]";
        else return ""+getMin();
    }


    public Trap getTrap() {
        return trap;
    }

    public boolean isStrictlyPositive() {
        return Double.compare(min, 0.0) > 0;
    }
    public boolean isStrictlyNegative() {
        return Double.compare(max, 0.0) < 0;
    }
    public boolean isWeaklyPositive() {
        return Double.compare(min, 0.0) >= 0;
    }
    public boolean isWeaklyNegative() {
        return Double.compare(max, 0.0) <= 0;
    }
}
