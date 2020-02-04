package jAADD

/**
 * The package jAADD implements an Affine Arithmetic Decision Diagram.
 *
 * @author Christoph Grimm, Carna Zivkovic
 */
/**
 * A class that is a range from a lower to an upper bound of the Integers.
 */
internal class iRange internal constructor(protected var min: Int, protected var max: Int) {

    // Some operations.
    operator fun plus(r: iRange) {
        min = r.min + min
        max = r.max + max
    }

    infix fun join(r: iRange) {
        min = Math.min(r.min, min)
        max = Math.max(r.max, max)
    }

    infix fun intersect(r: iRange) {
        min = Math.max(r.min, min)
        max = Math.min(r.max, max)
    }

    val isEmptySet: Boolean
        get() = min > max

}