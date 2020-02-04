package jAADD
import com.google.gson.GsonBuilder
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.ulp

/**
 * The class AffineForm represents an affine form.
 *
 * x := x0 + x1 e1 +x2 e2 + .... + r
 * where
 *  *  x0 is the central value.
 *  *  xi = x1 ... xn, i=1..n are the partial deviations.
 *  *  r holds all nonlinear and rounding effects.
 *
 * Furthermore, it is a hybrid combination of affine forms that can be overridden by IA
 * computation if IA provides more precise bounds.
 * The code is based on the Java library by Cassio Pennachin
 *
 * The properties (noise variables, r) are immutable.
 *
 * @author Christoph Grimmm, Carna Zivkovic
 *
 */
class AffineForm(iv: Range, var x0: Double, var r: Double = 0.0, coeff: HashMap<Int,Double> = HashMap<Int,Double>()) :
        Range(iv), Comparable<AffineForm> {

    val central: Double get() = x0

    val xi = HashMap(coeff);

    init {
        if ( isReals() && coeff.size == 0 ) { }
        else if (isRanges() ) {
            if (!x0.isFinite() || !r.isFinite()) {
                setRealNaN()
            }
            for (v in xi.values)
                if (!v.isFinite()) {
                    setRealNaN()
                }

            val radius = radius
            if (radius.isNaN()||radius.isInfinite()) setRealNaN()
            else {
                min = max(iv.min, x0 - r - radius)
                max = min(iv.max, x0 + r + radius)
            }
        }
    }

    /* Permits to create special kind of AffineForms */
    internal constructor(kind: Kind) : this(Range(kind), 0.0)

    /** Creates a scalar form equivalent to the floating point number c. */
    internal constructor(c: Double) : this(Range(c), c)

    /**
     * Creates an affine form with the given min and max values.
     * Uses a single noise symbol.
     */
    internal constructor(min: Double, max: Double, symbol: Int) :
            this(Range(min, max),
                    (max+min)/2.0,
                    0.0,
                    hashMapOf( (if(symbol==-1) NoiseVariables.newNoiseVar()
                                else symbol) to (max-min)/2.0) )

    /**
     * Creates an affine form with the given min and max values.
     * Uses a single, new noise symbol.
     */
    internal constructor(min: Double, max: Double, name: String) :
            this(Range(min, max), (max+min)/2.0, 0.0, hashMapOf( NoiseVariables.noiseVar(name) to (max-min)/2.0) )

    /** Creates an affine form that is a range */
    private constructor(r: Range) :
            this(r.min, r.max, -1)


    val noiseVarKeys: Set<Int>
        get() = xi.keys

    /**
     * Doesn't consider r and assumes terms are finite. Also doesn't consider
     * artificial ranges in hybrid forms.
     */
    val radius: Double
        get() {
            if (isEmpty()) return Double.NaN
            var rad = 0.0
            for (v in xi.values) {
                if (v.isInfinite() || v.isNaN()) return Double.POSITIVE_INFINITY
                rad += Math.abs(v); rad += rad.ulp
            }
            return rad // + r
        }

    public override fun clone(): Any {
        if (isEmpty() || isReals()) return this
        if (isScalar()) return AffineForm(x0)
        else return AffineForm(Range(min, max), x0, r, xi)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        if (javaClass != other.javaClass) return false
        val othera = other as AffineForm
        if (isScalar()) return x0 == othera.x0
        else if (isRanges())
            return x0.compareTo(othera.x0) == 0 && r.compareTo(othera.r) == 0
                    && xi == othera.xi && min.compareTo(othera.min) == 0 && max.compareTo(othera.max) == 0
        else return true
    }

    /**
     * Similarity of two affine forms is measured by the amount of uncorrelated deviation
     * that would be caused by merging them both into a single affine form.
     * @param other The affine form that is compared with this.
     * @param tol The tolerance below which we consider the affine forms as similar.
     * @return true, if similar.
     */
    fun isSimilar(other: AffineForm, tol: Double): Boolean {
        if (other === this) return true
        if (isTrap(other)) return false
        var nr = Math.abs(x0 - other.x0)
        nr = (nr + Math.ulp(nr)) / 2
        val indices: MutableSet<Int> = HashSet(xi.keys)
        indices.addAll(other.xi.keys)
        for (i in indices) {
            val xi = xi.getOrDefault(i, 0.0)
            val yi = other.xi.getOrDefault(i, 0.0)
            nr += if (xi * yi > 0) Math.abs(xi - yi) else xi + yi
        }
        return nr < tol
    }

    /**
     * Computes an affine model of the common range while preserving as much correlation
     * information as possible.
     * @param other the second affine form.
     * @return the joined range as affine form.
     */
    fun join(other: AffineForm): AffineForm {
        val nc = (x0 + other.x0) / 2
        var nr = abs(x0 - other.x0)
        nr = (nr + 2 * nr.ulp) / 2
        nr += r
        nr += nr.ulp
        nr += other.r
        nr += nr.ulp
        val nxi = HashMap<Int, Double>()
        val P: MutableSet<Int> = HashSet(xi.keys)
        P.addAll(other.xi.keys)
        for (i in P) {
            val xi = xi.getOrDefault(i, 0.0)
            val yi = other.xi.getOrDefault(i, 0.0)
            if (xi * yi > 0) {
                nxi[i] = Math.min(Math.abs(xi), Math.abs(yi)) * Math.signum(xi)
                nr += Math.abs(xi - yi); nr += Math.ulp(nr)
            } else {
                nr += Math.abs(xi); nr += nr.ulp
                nr += Math.abs(yi); nr += nr.ulp
            }
        }
        return AffineForm(this as Range join other, nc, nr, nxi)
    }

    /** Adds two affine forms   */
    operator fun plus(other: AffineForm): AffineForm {
        if (isEmpty() || other.isEmpty()) return Empty
        val nc = x0 + other.x0
        var err = Math.ulp(nc)
        val nts = HashMap<Int, Double>()
        val idx = HashSet(xi.keys)
        idx.addAll(other.xi.keys)
        for (i in idx) {
            val v1 = xi.getOrDefault(i, 0.0)
            val v2 = other.xi.getOrDefault(i, 0.0)
            var sum = v1 + v2
            err += sum.ulp
            nts[i] = sum
        }
        var nr = r + other.r + err
        nr += nr.ulp
        return AffineForm(this as Range+other, nc, nr, nts)
    }

    /** Subtracts two affine forms   */
    operator fun minus(other: AffineForm): AffineForm {
        if (isEmpty() || other.isEmpty()) return Empty
        val nc = x0 - other.x0
        var err = Math.ulp(nc)
        val nts = HashMap<Int, Double>()
        val idx = HashSet(xi.keys)
        idx.addAll(other.xi.keys)
        for (i in idx) {
            val v1 = xi.getOrDefault(i, 0.0)
            val v2 = other.xi.getOrDefault(i, 0.0)
            var dif = v1 - v2
            err += dif.ulp
            nts[i] = dif
        }
        var nr = r + other.r + err
        nr += nr.ulp
        return AffineForm(this as Range-other, nc, nr, nts)
    }

    /** Adds a (possibly negative) scalar to an affine form. */
    operator fun plus(delta: Double): AffineForm {
        if (isEmpty()) return Empty
        if (isReals()) return Reals
        if (delta == Double.NaN) return Empty
        if (delta == Double.POSITIVE_INFINITY) return AffineForm(Double.POSITIVE_INFINITY)
        if (delta == Double.NEGATIVE_INFINITY) return AffineForm(Double.NEGATIVE_INFINITY)
        val nc = x0 + delta
        val nr = r + 2 * Math.ulp(nc) // noise symbol modeling quantization error.
        val nts = HashMap(xi)
        return AffineForm(this as Range - Range(delta), nc, nr, nts)
    }

    /** Multiplies an affine form by a given scalar.  */
    operator fun times(alpha: Double): AffineForm {
        if (isEmpty()) return Empty
        if (isReals()) return Reals
        if (java.lang.Double.isNaN(alpha)) return Empty
        val nts = HashMap<Int, Double>()
        xi.keys.forEach { nts[it] = xi[it]!! * alpha}
        return AffineForm(this as Range * Range(alpha), x0*alpha, r*Math.abs(alpha), nts)
    }

    /** Negation  */
    override operator fun unaryMinus(): AffineForm {
        if (isEmpty()) return Empty
        if (isReals()) return Reals
        val nc = -x0
        val nr = r
        val nts = HashMap<Int, Double>()
        xi.keys.forEach { nts[it] = -xi[it]!! }
        return AffineForm(-Range(this), nc, nr, nts)
    }

    /**
     * Multiplication. Uses the simpler approximation proposed by Stolfi et al
     * instead of the more precise but costlier version. Computes interval product
     * as well and keeps the intersection of the results, minimizing error propagation.
     */
    operator fun times(other: AffineForm): AffineForm {
        if (isEmpty() || other.isEmpty()) return Empty
        // if (isTrap(other)) return AffineForm(handleTrap(other))
        if (isScalar() && other.isScalar()) return AffineForm(x0 * other.x0)
        val c = x0 * other.x0
        val noise = Math.abs(x0) * other.r + Math.abs(other.x0) * r + (radius + r) * (other.radius + other.r)
        val nts = HashMap<Int, Double>()
        val idx: MutableSet<Int> = HashSet(xi.keys)
        idx.addAll(other.xi.keys)
        idx.forEach {
            val xi = if (xi.containsKey(it)) xi[it]!! else 0.0
            val yi = if (other.xi.containsKey(it)) other.xi[it]!! else 0.0
            nts[it] = xi * other.x0 + yi * x0
        }
        return AffineForm(Range(this)*Range(other), c, noise, nts)
    }

    /** Scalar addition, multiplication and noise increment on a single form */
    fun affine(alpha: Double, delta: Double, noise: Double): AffineForm {
        if (isEmpty()) return Empty
        if (isReals()) return Reals
        val nc = x0 * alpha + delta
        var nr = r * Math.abs(alpha) + noise
        nr += nr.ulp + nc.ulp
        val nts = HashMap(xi)
        for (sym in xi.keys) {
            val nval = xi[sym]!! * alpha
            nr += nval.ulp
            nts[sym] = xi[sym]!! * alpha
        }
        var nMin = min * alpha + delta
        nMin -= nMin.ulp
        var nMax = max * alpha + delta
        nMax += nMax.ulp
        return AffineForm(Range(Math.min(nMin - noise, nMax - noise),
                          Math.max(nMin + noise, nMax + noise)), nc, nr, nts)
    }

    /** Exponentiation */
    fun exp(): AffineForm {
        if (isEmpty()) return Empty
        if (isReals()) return Reals
        val iaMin = Math.exp(min)
        val iaMax = Math.exp(max)
        val delta = (iaMax + iaMin * (1.0 - min - max)) / 2.0
        val noise = (iaMax + iaMin * (min - max - 1.0)) / 2.0
        if (noise < 0 || isScalar()) {
            val nc = Math.max(Math.exp(x0), Double.MIN_VALUE)
            return AffineForm(nc)
        }
        val aux = affine(iaMin, delta, noise)
        if (aux.min.compareTo(iaMin) > 0) {
            val d = aux.min - iaMin
            // NOTE: PLOP uses central + d, but I think that's a typo/bug, as
            // we decrease min, so it doesn' make sense to increase central.
            return AffineForm(Range(iaMin, aux.max), aux.x0 - d, aux.r + d, aux.xi)
        } else if (aux.min.compareTo(0.0) < 0) {
            val d = Double.MIN_VALUE - aux.min
            return AffineForm(Range(Double.MIN_VALUE, aux.max), aux.x0 + d, aux.r + d, aux.xi )
        }
        return aux
    }

    /**
     * Square root. Use the property that sqrt(x) = e ^ (0.5 log x)
     * TODO: Replace with Stolfi's min-range approximation that is more accurate.
     */
    fun sqrt(): AffineForm {
        if (isEmpty()) return Empty
        if (isReals()) return Reals
        val halfLog = log().times(0.5)
        return halfLog.exp()
    }

    /** Natural logarithm */
    fun log(): AffineForm {
        if (isEmpty()) return Empty
        if (isReals()) return Reals
        if (min.compareTo(0.0) < 0) return AffineForm(Double.NEGATIVE_INFINITY) else if (isScalar()) return AffineForm(Math.log(x0))
        val l = Math.log(min)
        val u = Math.log(max)
        val alpha = (u - l) / (max - min)
        val xs = 1 / alpha
        val ys = (xs - min) * alpha + l
        val logxs = Math.log(xs)
        val delta = (logxs + ys) / 2 - alpha * xs
        val noise = Math.abs(logxs - ys) / 2
        return affine(alpha, delta, noise)
    }

    /**
     * We do division by multiplying by inv(other) as suggested by Stolfi.
     * Division by zero returns infinity.
     */
    operator fun div(other: AffineForm): AffineForm = times(other.inv())

    /** Reciprocal, which gives us division. */
    fun inv(): AffineForm {
        if (isEmpty()) return Empty
        if (isReals()) return Reals
        if (isScalar()) {
            return if (x0.compareTo(0.0) == 0) AffineForm(Double.POSITIVE_INFINITY) else AffineForm(1.0 / x0)
        } else if (min.compareTo(0.0) < 0 && max.compareTo(0.0) > 0.0)
            return AffineForm(Range(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY))
        val l = Math.min(abs(min), abs(max))
        val u = Math.max(abs(min), abs(max))
        val alpha = -1.0 / (u * u)
        val auxLow = 2.0 / u
        val auxUpp = 1.0 / l - alpha * l
        val den = if (min.compareTo(0.0) < 0) -2.0 else 2.0
        val delta = (auxUpp + auxLow) / den
        val noise = (auxUpp - auxLow) / 2
        return affine(alpha, delta, Math.max(0.0, noise))
    }


    /** Just use multiplication for the time being. */
    fun sqr(): AffineForm {
        if (isEmpty()) return Empty
        if (isReals()) return Reals
        if (isScalar()) return AffineForm(x0 * x0)
        // Affine forms are supposed to be immutable, so we need to create a
        // new one after messing up with aux's internals.
        val aux = times(this)
        var d = aux.r - aux.x0
        if (java.lang.Double.compare(d, 0.0) > 0) {
            d /= 2.0
            aux.r -= d
            aux.x0 += d
        }
        if (java.lang.Double.compare(aux.max, 0.0) > 0 && java.lang.Double.compare(aux.min, 0.0) < 0) {
            aux.max = Math.max(aux.max, -aux.min)
            aux.min = 0.0
        }
        return AffineForm(Range(aux.min, aux.max),aux.x0, aux.r, aux.xi)
    }

    /** TODO: Port proper least squares approximation */
    fun sin(): AffineForm {
        if (isEmpty()) return Empty
        if (isReals()) return Reals
        return if (isScalar()) AffineForm(Math.sin(x0))
               else AffineForm(-1.0, 1.0, -1)
    }

    // TODO: Port proper least squares approximation
    fun cos(): AffineForm {
        if (isEmpty()) return Empty
        if (isReals()) return Reals
        return if (isScalar()) AffineForm(Math.cos(x0))
               else AffineForm(-1.0, 1.0, -1)
    }

    fun toJson(): String {
        val gson = GsonBuilder().setPrettyPrinting().create()
        return gson.toJson(this)
    }

    /**
     * Creates a human-readable string that represents the affine form.
     * The string is by default only a short range representation.
     * If the field diagnostics_on is set to true, full information is given.
     * @return String
     */
    override fun toString(): String {
        var af = super.toString()
        if (AADD.toStringVerbose) {
            if (isScalar() || isFinite()) {
                af += " \u2286 " + String.format("%.2f", x0)
                xi.keys.forEach { af += " + "+ String.format("%.2f", xi[it]) + "\u03B5" + it }
                af += " \u00B1 " + String.format("%.2f", r)
            }
        }
        return af
    }

    /**
     * Comparison of affine forms:
     * <li>
     *     * we compute the difference, and compare its min/max with 0
     * </li>
     */
    override public operator fun compareTo(other: AffineForm): Int {
        val dif = this-other
        if (dif.min > 0) return 1
        else if (dif.max < 0) return -1
        return 0
    }

    companion object {
        // Global constants, same as for AADD.
        @JvmField
        val Reals  = AffineForm(Range.Reals,0.0, 0.0, HashMap())

        @JvmField
        val RealsNaN = AffineForm(Range.RealsNaN)

        @JvmField
        val Empty = AffineForm(Range.Empty)
    }
}
