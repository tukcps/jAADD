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
    public enum Type {
        SCALAR, FINITE, INFINITE, NaN
    }

    Type type;

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
        else if (Double.compare(min, max) == 0) this.type = Type.SCALAR;
        else {
            this.type = Type.FINITE;
            this.min = min;
            this.max = max;
        }
    }

    Range(Range other) {
        this.type = other.type;
        this.min  = other.min;
        this.max  = other.max;
    }

    Range(double c) {
        if (Double.isNaN(c) || Double.isInfinite(c))
            setInfinity();
        else {
            this.min = c;
            this.max = c;
            this.type = Type.SCALAR;
        }
    }


    Range(Type kind) {
        if (kind == Type.NaN) setNaN();
        else if (kind == Type.INFINITE) setInfinity();
        else {
            this.type = Type.FINITE;
            min=Double.NEGATIVE_INFINITY;
            max=Double.POSITIVE_INFINITY;
        }
    }


    public void setInfinity() {
        type = Type.INFINITE;
        min = Double.NEGATIVE_INFINITY;
        max = Double.POSITIVE_INFINITY;
    }

    public void setNaN() {
        type = Type.NaN;
        min = Double.POSITIVE_INFINITY;
        max = Double.NEGATIVE_INFINITY;
    }

    public Range handleTrap(Range other) {
        if (this.type == Type.NaN || other.type == Type.NaN) return new Range(Type.NaN);
        if (this.type == Type.INFINITE || other.type == Type.INFINITE) return new Range(Type.INFINITE);
        if ( (this.type == Type.SCALAR) && (other.type == Type.SCALAR)) return new Range(Type.SCALAR);
        return new Range(Type.FINITE);
    }

    public boolean isTrap(Range other) {
        if (this.type == Type.NaN || other.type == Type.NaN
                || this.type == Type.INFINITE || other.type == Type.INFINITE) return true;
        else
            return false;
    }

    public boolean isTrap(double other) {
        if (this.isTrap() || Double.isNaN(other) || Double.isInfinite(other)) return true;
        else return false;
    }

    public boolean isTrap() {
        if (this.type == Type.NaN || this.type == type.INFINITE) return true;
        else return false;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }


    // Some operations.
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
        return other.min == min && other.max == max && other.type == type;
    }

    @Override
    public String toString() {
        if (type == Type.NaN) return "NaN";
        else if (type == Type.INFINITE) return "INF";
        else if (type == Type.FINITE) return "["+ String.format("%.2f", min) + "; " + String.format("%.2f", max) + "]";
        else return ""+getMin();
    }


    public Type getType() {
        return type;
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
