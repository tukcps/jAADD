package jAADD;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;



/**
 * The class AffineForm represents an affine form. <p>
 *
 * x := x0 + x1 e1 +x2 e2 + .... + r <p>
 * <ul>
 * <li> x0 is the central value.
 * <li> xi = x1 ... xn, i=1..n are the partial deviations.
 * <li> r holds all nonlinear and rounding effects.
 * </ul> <p>
 * Furthermore, it is a hybrid combination of affine forms that can be overridden by IA
 * computation if IA provides more precise bounds.
 *
 * The class variables (noise variables, r) are assumed to be immutable.
 *
 * @author Cassio Pennachin
 * @author Christoph Grimmm, Carna Zivkovic
 */
public class AffineForm extends Range {

    private double x0 = 0.0;
    public HashMap<Integer, Double> xi = new HashMap<Integer, Double>();
    private double r  = 0.0;
    private static int n = 0;

    // A global constant for convenience
    public static final AffineForm INFINITE = new AffineForm(0.0);
    static { INFINITE.setInfinity(); }

    transient static boolean diagnostics_on = false;

    // true once the IA range is more tight than the diameter of the AA
    transient boolean overriden = false;


    /**
     * Creates a scalar form equivalent to the floating point number c.
     */
    public AffineForm(double c) {
        super(c);
        x0 = c;
        if (Double.isNaN(c)) setNaN();
        if (Double.isInfinite(c)) setInfinity();
    }


    /**
     * Creates an affine form with the given min and max values. Checks for infinite
     * and empty forms. Uses a single noise symbol (use -1 to automatically allocate
     * a new one).
     * It also adds a string as a documentation to the new noise symbol.
     */
    public AffineForm(double min, double max, int symbol, String ... docs ) {
        super(min, max);
        if (trap == Trap.SCALAR) x0 = min;
        else if (trap == Trap.FINITE) {
                x0 = (max + min) / 2.0;
                if (symbol == -1) symbol = ++n;
                xi.put(symbol, Math.max(max - x0, x0 - min));
                AADDMgr.setNoiseSymbolDocs(symbol, docs);
        }
    }


    public AffineForm(Range minmax) {
        super(minmax);
        x0 = (max-min)/2;
        assert isTrap(): "Constructor only for trap handling.";
    }


    /**
     * Creates an affine form from a list of terms and a given central value.
     * r represents previous error from nonlinear operations and roundup and
     * can be zero but must not be negative.
     */
    public AffineForm(double c, HashMap<Integer, Double> ts, double r) {
        super(Trap.FINITE);
        assert r >= 0;

        x0     = c;
        this.r = r;

        if ( Double.isInfinite(c) ||  Double.isInfinite(r)) {
            setInfinity(); return;
        } else if (Double.isNaN(c) || Double.isNaN(r)) {
            setNaN(); return;
        } else if (Double.compare(r, 0.0) < 0)
            throw new RuntimeException("Trying to create affine form with negative noise: " + r);

        xi = new HashMap<Integer, Double>(ts);
        for (Double val : xi.values()) {
            if (Double.isInfinite(val)) {
                setInfinity();
                return;
            }
            if (Double.isNaN(val)) {
                setNaN();
                return;
            }
        }

        double radius = getRadius();
        if (Double.compare(radius, 0.0) > 0 || Double.compare(r, 0.0) > 0) {
            trap = Trap.FINITE;
            min = x0 - r - radius;
            max = x0 + r + radius;
        } else {
            trap = Trap.SCALAR;
            min = max = x0;
        }
    }

    /**
     * Creates an affine form from a list of terms and a given central value.
     * r represents previous error and can be zero but mustn't be negative. Enforces
     * the provided min and max values even if they're different from the radius,
     * allowing hybrid IA/AA model.
     */
    public AffineForm(double c, HashMap<Integer, Double> ts, double r, double min, double max) {
        this(c, ts, r);
        if (trap == Trap.INFINITE || trap == Trap.NaN) return;


        if (Double.compare(min, this.min) > 0) {
            this.min = min;
            overriden = true;
        }
        if (Double.compare(max, this.max) < 0) {
            this.max = max;
            overriden = true;
        }
    }

    public String[] getDocs(Integer key) { return AADDMgr.getNoiseSymbolDocs(key);}
    public void setDocs(Integer key, String... name) { AADDMgr.setNoiseSymbolDocs(key, name);}

    public double getCentral() {
        return x0;
    }

	public double getR() {
		return r;
	}

    public Set<Integer> getNoiseSymbols() {
        return xi.keySet();
    }

    /**
     * Doesn't consider r and assumes terms are finite. Also doesn't consider
     * artificial ranges in hybrid forms.
     */
    public double getRadius() {
        if (trap == Trap.NaN) return Double.NaN;
        if (trap == Trap.INFINITE) return Double.POSITIVE_INFINITY;

        double radius = 0.0;
        for (double v : xi.values()) {
            radius += Math.abs(v);
        }
        return radius;
    }


    public boolean isOverriden() {
        return overriden;
    }


    @Override
    public AffineForm clone() {
        switch (trap) {
            case SCALAR:
                return new AffineForm(x0);
            case INFINITE:
                return new AffineForm(Double.POSITIVE_INFINITY);
            default:
                return new AffineForm(x0, xi, r, min, max);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        AffineForm other = (AffineForm) obj;
        if (trap != other.trap) return false;
        if (trap == Trap.SCALAR) return x0 == other.x0;
        else if (trap == Trap.FINITE)
            return (Double.compare(x0, other.x0) == 0 && Double.compare(r, other.r) == 0
                    && xi.equals(other.xi))
                    && ((!overriden && !other.overriden)
                    || (Double.compare(min, other.min) == 0 && Double.compare(max, other.max) == 0));
        else
            return true;
    }

    /**
     * Similarity of two affine forms is measured by the amount of uncorrelated deviation
     * that would be caused by merging them both into a single affine form.
     * @param other The affine form that is compared with this.
     * @param tol The tolerance below which we consider the affine forms as similar.
     * @return true, if similar.
     */
    public boolean isSimilar(AffineForm other, double tol) {
        if (other == this) return true;
        if (isTrap(other)) return false;

        double nr = Math.abs(getCentral()-other.getCentral());
        nr = (nr + Math.ulp(nr))/2;

        Set<Integer> indices = new HashSet<Integer>(xi.keySet());
        indices.addAll(other.xi.keySet());
        for (Integer i: indices) {
            double xi = this.xi.getOrDefault(i, 0.0);
            double yi = other.xi.getOrDefault(i, 0.0);
            if (xi*yi > 0) {
                nr += Math.abs(xi-yi);
            } else {
                nr += xi+yi;
            }
        }
        return nr < tol;
    }

    /**
     * Computes an affine model of the common range while preserving as much correlation
     * information as possible.
     * @param other the second affine form.
     * @return the joined range as affine form.
     */
    public AffineForm join(AffineForm other) {
        double nc = (getCentral()+other.getCentral())/2;
        double nr = Math.abs(getCentral()-other.getCentral());
        nr = (nr + 2*Math.ulp(nr))/2;
        nr += getR(); nr += 2*Math.ulp(nr);
        nr += other.getR(); nr += 2*Math.ulp(nr);

        HashMap<Integer, Double> nts = new HashMap<Integer, Double>();
        Set<Integer> P = new HashSet<Integer>(xi.keySet());
        P.addAll(other.xi.keySet());
        for (Integer i: P) {
            double xi = this.xi.getOrDefault(i, 0.0);
            double yi = other.xi.getOrDefault(i, 0.0);
            if (xi*yi > 0) {
                nts.put(i, Math.min(Math.abs(xi), Math.abs(yi))*Math.signum(xi));
                nr += Math.abs(xi-yi); nr += 2*Math.ulp(nr);
            } else {
                nr += Math.abs(xi); nr += 2*Math.ulp(nr);
                nr += Math.abs(yi); nr += 2*Math.ulp(nr);
            }
        }
        if (overriden || other.overriden) {
            double nMin = Math.min(min, other.min); nMin -= 2*Math.ulp(nMin);
            double nMax = Math.max(max, other.max); nMax += 2*Math.ulp(nMax);
            return new AffineForm(nc, nts, nr, Math.min(nMin, nMax), Math.max(nMin, nMax));
        } else {
            return new AffineForm(nc, nts, nr);
        }
    }

    /** Adds two affine forms  */
    public AffineForm add(AffineForm other) {
        if (isTrap(other)) return new AffineForm(handleTrap(other));

        double zc  = x0 + other.x0;
        double err = 2*Math.ulp(x0);

        HashMap<Integer, Double> nts = new HashMap<Integer, Double>();
        Set<Integer> idx = new HashSet<Integer>(xi.keySet());
        idx.addAll(other.xi.keySet());
        for (Integer i : idx) {
            double v1 = xi.getOrDefault(i, 0.0);
            double v2 = other.xi.getOrDefault(i, 0.0);
            double sum = v1+v2;
            sum -= Math.ulp(sum);
            err += 2*Math.ulp(sum);
            nts.put(i, sum);
        }
        double nr = r + other.r + err;
        nr += 2*Math.ulp(nr);

        if (overriden || other.overriden) {
            double nMin = min + other.min; nMin -=  2*Math.ulp(nMin);
            double nMax = max + other.max; nMax +=  2*Math.ulp(nMax);
            return new AffineForm(zc, nts, nr, Math.min(nMin, nMax), Math.max(nMin, nMax));
        } else {
            return new AffineForm(zc, nts, nr);
        }
    }


    /**
     * Adds a (possibly negative) scalar to an affine form.
     * It considers the roundoff error of the central value addition as one ulp and adds it to r.
     */
    public AffineForm add(double delta) {
        if (isTrap()) return this;
        if (delta == Double.NaN) return new AffineForm(Double.NaN);
        if (delta == Double.POSITIVE_INFINITY) return new AffineForm(Double.POSITIVE_INFINITY);
        if (delta == Double.NEGATIVE_INFINITY) return new AffineForm(Double.NEGATIVE_INFINITY);

        double nc = x0 + delta;
        double nr = r + 2*Math.ulp(nc);         // noise symbol modeling quantization error.

        HashMap<Integer, Double> nts = new HashMap<>(xi);

        if (overriden) {
            double nMin = min + delta; nMin -=  Math.ulp(nMin);
            double nMax = max + delta; nMax +=  Math.ulp(nMax);
            return new AffineForm(nc, nts, nr, Math.min(nMin, nMax), Math.max(nMin, nMax));
        } else
            return new AffineForm(nc, nts, nr);
    }


    /**
     * Multiplies an affine form by a given scalar.
     * We consider multiplication as "safe" operation.
     */
    public AffineForm mult(double alpha) {
        if (isTrap(alpha)) return this;

        double nc = x0 * alpha;
        double nr = r * alpha;

        HashMap<Integer, Double> nts = new HashMap<Integer, Double>();
        Set<Integer> idx = new HashSet<Integer>(xi.keySet());
        for (Integer i : idx) {
            nts.put(i, xi.get(i)*alpha);
        }

        if (overriden) {
            double nMin = min * alpha; double nMax = max * alpha;
            return new AffineForm(nc, nts, nr, Math.min(nMin, nMax), Math.max(nMin, nMax));
        } else
            return new AffineForm(nc, nts, nr);
    }

    /** Negation */
    public AffineForm negate() {
        if (isTrap()) return this;

        double nc = -x0;
        double nr = r;

        HashMap<Integer, Double> nts = new HashMap<Integer, Double>();
        Set<Integer> symbols = new HashSet<Integer>(xi.keySet());
        for (Integer sym : symbols) {
            nts.put(sym, -xi.get(sym));
        }

        if (overriden) {
            return new AffineForm(nc, nts, nr, max, min);
        } else
            return new AffineForm(nc, nts, nr);
    }

    /**
     * Subtraction including double roundoff error consideration.
     * The absolute value of the partial sensitivities is reduced by one ulp.
     * r is increased by the sum of the ulps that were reduced.
     **/
    public AffineForm sub(AffineForm other) {
        if (isTrap(other)) return new AffineForm(handleTrap(other));

        double nc  = x0 - other.x0;
        double err = 2*Math.ulp(nc);

        HashMap<Integer, Double> zis = new HashMap<Integer, Double>();
        Set<Integer> idx = new HashSet<Integer>(xi.keySet());
        idx.addAll(other.xi.keySet());
        for (Integer i : idx) {
            double xi = this.xi.getOrDefault(i, 0.0);
            double yi = other.xi.getOrDefault(i, 0.0);
            double zi = xi-yi;
            zi = (Math.abs(zi)-Math.ulp(zi))*Math.signum(zi);
            err += 3*Math.ulp(zi);
            zis.put(i, zi);
        }
        double nr = r + other.r + err;
        nr += 2*Math.ulp(nr);

        if (overriden || other.overriden) {
            double nMin = min + other.min; nMin -=  2*Math.ulp(nMin);
            double nMax = max + other.max; nMax +=  2*Math.ulp(nMax);
            return new AffineForm(nc, zis, nr, Math.min(nMin, nMax), Math.max(nMin, nMax));
        } else {
            return new AffineForm(nc, zis, nr);
        }
    }

    /**
     * Multiplication. Uses the simpler approximation proposed by Stolfi et al
     * instead of the more precise but costlier version. Computes interval product
     * as well and keeps the intersection of the results, minimizing error propagation.
     */
    public AffineForm mult(AffineForm other) {
        if (isTrap(other)) return new AffineForm(handleTrap(other));
        if (trap == Trap.SCALAR && other.trap == Trap.SCALAR)
            return new AffineForm(x0 * other.x0);

        double c = x0 * other.x0;
        double noise = Math.abs(x0) * other.r + Math.abs(other.x0) * r
                + (getRadius() + r) * (other.getRadius() + other.r);

        HashMap<Integer, Double> nts = new HashMap<Integer, Double>();
        Set<Integer> idx = new HashSet<Integer>(xi.keySet());
        idx.addAll(other.xi.keySet());
        for (Integer i : idx) {
            double xi = this.xi.containsKey(i) ? this.xi.get(i) : 0.0;
            double yi = other.xi.containsKey(i) ? other.xi.get(i) : 0.0;
            nts.put(i, xi * other.x0 + yi * x0);
        }

        // IA multiplication for hybrid model
        List<Double> iaMult = Arrays.asList(min * other.min, min * other.max, max * other.min, max * other.max);

        return new AffineForm(c, nts, noise, Collections.min(iaMult), Collections.max(iaMult));
    }


    /**
     * Linear Combination. Returns alpha * this + beta * other + delta.
     * The method is used as kernel to implement all other operations and functions.
     * It handles the computation of r, min, and max.
     */
    public AffineForm linearComb(AffineForm other, double alpha, double beta, double delta, double noise) {
        if (isTrap(other)) return new AffineForm(handleTrap(other));

        double nc = alpha * x0 + beta * other.x0 + delta;

        // TODO: Check with Carna for correctness! I think r here must be multiplication with beta, alpha.
        double nr = alpha*r + beta*other.r + noise;
        // double nr = r+other.r+noise; // Original ...
        HashMap<Integer, Double> nts = new HashMap<Integer, Double>();

        Set<Integer> symbols = new HashSet<Integer>(xi.keySet());
        symbols.addAll(other.xi.keySet());
        for (Integer sym : symbols) {
            double xi = this.xi.containsKey(sym) ? this.xi.get(sym) : 0.0;
            double yi = other.xi.containsKey(sym) ? other.xi.get(sym) : 0.0;
            double zi = alpha * xi + beta * yi;
            zi-=Math.ulp(zi); nr+=2*Math.ulp(zi);
            nts.put(sym, zi);
        }

        if (overriden || other.overriden) {
            double nMin = min * alpha + other.min * beta + delta; nMin -= Math.ulp(nMin);
            double nMax = max * alpha + other.max * beta + delta; nMax += Math.ulp(nMax);
            return new AffineForm(nc, nts, nr, Math.min(nMin - noise, nMax - noise),
                    Math.max(nMin + noise, nMax + noise));
        } else {
            return new AffineForm(nc, nts, nr);
        }
    }

    /**
     * Scalar addition, multiplication and noise increment on a single form
     */
    public AffineForm affine(double alpha, double delta, double noise) {
        if (isTrap()) return this;

        double nc = x0 * alpha + delta;
        double nr = r * Math.abs(alpha) + noise;
        nr += Math.ulp(nr) + Math.ulp(nc);

        HashMap<Integer, Double> nts = new HashMap<Integer, Double>(xi);
        for (Integer sym : xi.keySet()) {
            double nval = xi.get(sym) * alpha;
            nr += Math.ulp(nval);
            nts.put(sym, xi.get(sym) * alpha);
        }

        if (overriden) {
            double nMin = min * alpha + delta; nMin -= Math.ulp(nMin);
            double nMax = max * alpha + delta; nMax += Math.ulp(nMax);
            return new AffineForm(nc, nts, nr, Math.min(nMin - noise, nMax - noise),
                    Math.max(nMin + noise, nMax + noise));
        } else {
            return new AffineForm(nc, nts, nr);
        }
    }

    /**
     * Exponentiation
     */
    public AffineForm exp() {
        if (isTrap()) return this;

        double iaMin = Math.exp(min);
        double iaMax = Math.exp(max);
        double delta = (iaMax + iaMin * (1.0 - min - max)) / 2.0;
        double noise = (iaMax + iaMin * (min - max - 1.0)) / 2.0;

        if (noise < 0 || trap == Trap.SCALAR) {
            double nc = Math.max(Math.exp(x0), Double.MIN_VALUE);
            return new AffineForm(nc);
        }

        AffineForm aux = this.affine(iaMin, delta, noise);

        if (Double.compare(aux.min, iaMin) > 0) {
            double d = aux.min - iaMin;
            // NOTE: PLOP uses central + d, but I think that's a typo/bug, as
            // we decrease min, so it doesn' make sense to increase central.
            return new AffineForm(aux.x0 - d, aux.xi, aux.r + d, iaMin, aux.max);
        } else if (Double.compare(aux.min, 0) < 0) {
            double d = Double.MIN_VALUE - aux.min;
            return new AffineForm(aux.x0 + d, aux.xi, aux.r + d, Double.MIN_VALUE, aux.max);
        }

        return aux;
    }

    /**
     * Square root. Use the property that sqrt(x) = e ^ (0.5 log x)
     * TODO: Replace with Stolfi's min-range approximation that is more accurate.
     */
    public AffineForm sqrt() {
        if (isTrap()) return this;
        AffineForm halfLog = this.log().mult(0.5);
        return halfLog.exp();
    }

    /**
     * Natural logarithm
     */
    public AffineForm log() {
        if (isTrap()) return this;
        else if (Double.compare(min, 0) < 0)
            return new AffineForm(Double.NEGATIVE_INFINITY);
        else if (trap == Trap.SCALAR)
            return new AffineForm(Math.log(x0));

        double l = Math.log(min);
        double u = Math.log(max);
        double alpha = (u - l) / (max - min);
        double xs = 1 / alpha;
        double ys = (xs - min) * alpha + l;
        double logxs = Math.log(xs);
        double delta = (logxs + ys) / 2 - alpha * xs;
        double noise = Math.abs(logxs - ys) / 2;

        return this.affine(alpha, delta, noise);
    }

    /**
     * Reciprocal, which gives us division.
     */
    public AffineForm inv() {

        if (isTrap()) return this;
        else if (trap == Trap.SCALAR) {
            if (Double.compare(x0, 0.0) == 0)
                return new AffineForm(Double.POSITIVE_INFINITY);
            else
                return new AffineForm(1.0 / x0);
        }
        // An interval that straddles zero has ill-defined inv()
        else if (Double.compare(min, 0.0) < 0 && Double.compare(max, 0.0) > 0.0)
            return new AffineForm(Double.POSITIVE_INFINITY);

        double l = Math.min(Math.abs(min), Math.abs(max));
        double u = Math.max(Math.abs(min), Math.abs(max));
        double alpha = -1.0 / (u * u);
        double auxLow = 2.0 / u;
        double auxUpp = 1.0 / l - alpha * l;
        double den = Double.compare(min, 0) < 0 ? -2.0 : 2.0;
        double delta = (auxUpp + auxLow) / den;
        double noise = (auxUpp - auxLow) / 2;

        return this.affine(alpha, delta, Math.max(0.0, noise));
    }

    /**
     * We do division by multiplying by inv(other) as suggested by Stolfi.
     * Division by zero returns infinity.
     */
    public AffineForm div(AffineForm other) {
        if (isTrap()) return this;
        else if (other.trap == Trap.INFINITE) return other;
        return mult(other.inv());
    }

    /**
     * Just use multiplication for the time being.
     */
    public AffineForm sqr() {
        if (isTrap()) return this;
        else if (trap == Trap.SCALAR) return new AffineForm(x0 * x0);

        // Affine forms are supposed to be immutable, so we need to create a
        // new one after messing up with aux's internals.

        AffineForm aux = mult(this);

        double d = aux.r - aux.x0;
        if (Double.compare(d, 0.0) > 0) {
            d /= 2.0;
            aux.r -= d;
            aux.x0 += d;
        }
        if (Double.compare(aux.max, 0.0) > 0 && Double.compare(aux.min, 0.0) < 0) {

            aux.max = Math.max(aux.max, -aux.min);
            aux.min = 0.0;
        }

        return new AffineForm(aux.x0, aux.xi, aux.r, aux.min, aux.max);
    }

    // TODO: Port proper least squares approximation
    public AffineForm sin() {
        if (isTrap()) return this;
        else if (trap == Trap.SCALAR) return new AffineForm(Math.sin(x0));
        else return new AffineForm(-1.0, 1.0, -1);
    }

    // TODO: Port proper least squares approximation
    public AffineForm cos() {
        if (trap == Trap.INFINITE)
            return this;
        else if (trap == Trap.SCALAR)
            return new AffineForm(Math.cos(x0));
        else
            return new AffineForm(-1.0, 1.0, -1);
    }

    /**
     * Creates a human-readable string that represents the affine form.
     * The string is by default only a short range representation.
     * If the field diagnostics_on is set to true, full information is given.
     * @return String
     */
    @Override
    public String toString() {
        String af = super.toString();
        if (diagnostics_on ) {
            if (!isTrap()) {
                af += " = ";
                af += String.format("%.2f", getCentral()) + " + ";
                for (Integer i : xi.keySet()) {
                    af += String.format("%.2f", xi.get(i)) + "\u03B5" + i + " + ";
                }
                af += "[+/-" +String.format("%.3f",getR()) +"]";
            }
        }
        return af;
    }
}
