package jAADD

import java.util.*

/**
 * The package jAADD implements an Affine Arithmetic Decision Diagram.
 *
 * @author Christoph Grimm, Carna Zivkovic, Jack D. Martin
 */

/**
 * @class Range
 * A class that is a range from a lower to an upper bound of the Reals, i.e. an interval data type.
 */
open class Range(var min: Double, var max: Double) :
        ClosedFloatingPointRange<Double>,
        Cloneable {

    override val start: Double
        get() = min

    override val endInclusive: Double
        get() = max

    override fun lessThanOrEquals(a: Double, b: Double): Boolean {
        TODO("Not yet implemented")
    }

    /**
     * Different kind of special cases:
     *  * EMPTY is an empty range, i.e. (+infinity, -infinity)
     *  * SCALAR is a single value.
     *  * RANGE is a finite range.
     *  * REAL is a  range that includes all Reals representable as Double
     */
    enum class Kind { EMPTY, SCALAR, RANGE, REALS, REALNaN, NaN }

    /** Copy constructor  */
    internal constructor(other: Range) : this(other.min, other.max)

    /** Constructor that creates a range of kind scalar  */
    internal constructor(c: Double) : this(c, c)
    internal constructor(r: ClosedFloatingPointRange<Double>) : this(r.start, r.endInclusive)

    /** Constructor that creates a range of a specific kind  */
    internal constructor(kind: Kind) : this(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY) {
        when(kind) {
            Kind.EMPTY  -> setEmpty()
            Kind.SCALAR -> { min = 0.0; max = 0.0 }
            Kind.RANGE  -> setReal()
            Kind.REALS   -> setReal()
            Kind.REALNaN-> setRealNaN()
            Kind.NaN    -> setRealNaN()
        }
    }

    /** The range has no restriction. The value can be any number from the reals.*/
    fun setReal() {
        min = -Double.MAX_VALUE
        max = Double.MAX_VALUE
    }

    /** The range has no restriction. The value can be any number from the reals or NaN. */
    fun setRealNaN() {
        min = Double.NEGATIVE_INFINITY
        max = Double.POSITIVE_INFINITY
    }

    /** The range includes no numbers from the Reals; empty set.  */
    fun setEmpty() {
        min = Double.MAX_VALUE
        max = -Double.MAX_VALUE
    }

    /** Checks if the range is of finite kind */
    fun isFinite() = (min != Double.NEGATIVE_INFINITY) && (max != Double.POSITIVE_INFINITY)
    override fun isEmpty() =  (min > max)
    fun isReals() = (min == -Double.MAX_VALUE) && (max == Double.MAX_VALUE)
                        //   || min.isNaN() || max.isNaN() || min.isInfinite() || max.isInfinite()
    fun isRealsNaN() = min.isNaN() || max.isNaN() || min.isInfinite() || max.isInfinite()
    fun isRanges()  = min.isFinite() && max.isFinite() && !isEmpty() && !isScalar()
    fun isScalar() = (min == max)


    /**
     * The method checks if one of the parameters of a binary operations requires special treatment.
     * If so, it returns true.
     * @param other The 2nd parameter of a binary operation.
     * @return True, if one of the parameters is NaN or Infinite.
     */
    fun isTrap() = isEmpty() || isRealsNaN()
    fun isTrap(other: Range)   = isTrap() || other.isTrap()
    fun isTrap(other: Double)  = isEmpty() || other.isInfinite()

    operator fun plus(other: Range)  =
            Range(min + other.min, max + other.max)

    operator fun minus(other: Range)  =
            Range(this.min-other.max, this.max-other.min)
            // Range(Math.min(this.min-other.max, this.max-other.min), Math.max(this.min-other.max, this.max-other.min))

    open operator fun unaryMinus() =
            Range(Math.min(-max, -min), Math.max(-max, -min))

    operator fun times(other: Range): Range {
        val iaMult = Arrays.asList(min * other.min, min * other.max, max * other.min, max * other.max)
        return Range(Collections.min(iaMult), Collections.max(iaMult))
    }

    infix fun join(other: Range): Range {
        if (isFinite() && other.isFinite())
            return Range(Math.min(other.min, min), Math.max(other.max, max))
        return Range.RealsNaN
    }

    infix fun intersect(r: Range) = Range(Math.max(r.min, min), Math.min(r.max, max))

    val isStrictlyPositive: Boolean get() = min.compareTo(0.0) > 0
    val isStrictlyNegative: Boolean get() = max.compareTo(0.0) < 0
    val isWeaklyPositive: Boolean get() = min.compareTo(0.0) >= 0
    val isWeaklyNegative: Boolean get() = max.compareTo(0.0) <= 0

    /** Some constants */ 
    companion object {
        /** Some constants that simplify work ...  */
        @JvmField val Empty = Range(Kind.EMPTY)
        @JvmField val Reals  = Range(Kind.REALS)
        @JvmField val RealsNaN = Range(Kind.REALNaN)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        if (javaClass != other.javaClass) return false
        val otherr = other as Range
        return otherr.min == min && otherr.max == max
    }

    override fun toString(): String {
        if (isEmpty())  return "∅"
        else if (isScalar()) return ""+min
        else if (isReals())   return "(-" + '\u221e' + ";+" + '\u221e' + ")"
        else if (isRealsNaN()) return "[" + '\u221e' + ";+" + '\u221e' + "]"
        else
            return "[" + String.format("%.2f", min) + "; " + String.format("%.2f", max) + "]"
    }
}
