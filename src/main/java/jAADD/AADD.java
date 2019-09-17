package jAADD;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.apache.commons.math3.optim.linear.*;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import static jAADD.AADDMgr.ONE;
import static jAADD.AADDMgr.ZERO;



/**
 * The class AADD implements an Affine Arithmetic Decision Diagram (AADD).
 * An AADD is, in very brief, a decision diagram (class DD) whose leave nodes
 * take values of type AffineForm.
 * AADD are, like DD, ordered.
 * <p> The AADD is immutable.
 *
 * @author Christoph Grimm, Carna Zivkovic
 */
public class AADD extends DD<AffineForm> implements Cloneable {

    public static double LPCallTh = 0.001; // If the radius is below this value, the LP Solver will not be called to compute a smaller range.
    public static double joinTh   = 0.001;

    /**
      * Creates a new leaf node with a value given as parameter.
      * @param value Center value of Affine Form at leaf node.
      */
     public AADD(AffineForm value) {
         super(value);
         this.leafValue=value.clone();
     }

    /**
     * Creates a new leaf node with an affine form with only center value.
     * @param center center value, no noise symbols.
     */
     public AADD(double center) {
         super(new AffineForm(center));
     }

    /**
     * Creates a new leaf node with an affine form specified by parameters.
     * @param min Lower bound
     * @param max Upper bound
     * @param symbol Noise symbol used
     */
     public AADD(double min, double max, int symbol, String ... docs) {
         super(new AffineForm(min, max, symbol, docs));
     }

     /**
      * Creates an internal node with a given index.
      * index must refer to an existing condition.
      * It must be smaller than maxIndex.
      * @param index is the index of the node level.
      * @param T is the true child.
      * @param F is the false child.
      */
     public AADD(int index, AADD T, AADD F) {
         super(index, T, F);

         if (T.isLeaf() && F.isLeaf()) {
             if ((T.Value().type == Range.Type.NaN) && (F.Value().type == Range.Type.NaN)) {
                 this.leafValue.setNaN();
                 this.index= Integer.MAX_VALUE;
                 this.T=this.F=null;
             }
         }

         if (T.isLeaf() && F.isLeaf() && T.Value().isSimilar(F.Value(), joinTh)) {
             leafValue = T.Value().join(F.Value());
             this.index = Integer.MAX_VALUE;
             this.T = this.F = null;
         }
     }


     /**
      * Creates an internal node and a new condition.
      * @param cond A condition that is an affine form with at least one noise symbol.
      * @param T Child for true, may not be null.
      * @param F Child for false, may not be null.
      */
     // public AADD(AffineForm cond, AADD T, AADD F) {
     //    super(cond, T, F);
     // }


     /**
      *  Clone method. Copies the tree structure, but not conditions.
      *  The leaves are not copied for BDD, where ONE and ZERO are merged.
      */
     @Override
     public AADD clone() {

         if ( isInternal() )
             return new AADD(index, T().clone(), F().clone());
         else
             return new AADD(this.Value());
     }


    /**
     * Applies a unary operator on an AADD and returns its result.
     * @param op operator to be applied on this AADD, returning result. This remains unchanged.
     * @return result of operation.
     */
    protected AADD Apply(Function<AADD, AADD> op) {

        // Recursion stops at leaves.
        if (isLeaf()) return op.apply(this);

        // Otherwise we recurse to T and E nodes.
        return new AADD(index, T().Apply(op), F().Apply(op));
    }


    /**
     * Computes the complementary function of a given BDD.
     * @return complementary of this.
     */
    public AADD negate() {
        Function<AADD, AADD> negate = (a) -> new AADD(a.Value().negate());
        return this.Apply(negate);
    }


    /**
     * Computes the exponentiation of a given BDD.
     * @return exponentiation of this.
     */
    public AADD exp() {
        Function<AADD, AADD> exp = (a) -> new AADD(a.Value().exp());
        return this.Apply(exp);
    }


    /**
     * Computes the square root of a given BDD.
     * @return square root of this.
     */
    public AADD sqrt() {
        Function<AADD, AADD> sqrt = (a) -> new AADD(a.Value().sqrt());
        return this.Apply(sqrt);
    }


    /**
     * Computes the natural logarithm of a given BDD.
     * @return natural logarithm of this.
     */
    public AADD log() {
        Function<AADD, AADD> log = (a) -> new AADD(a.Value().log());
        return this.Apply(log);
    }


    /**
     * Computes the reciprocal of a given BDD.
     * @return reciprocal of this.
     */
    public AADD inverse() {
        Function<AADD, AADD> inv = (a) -> new AADD(a.Value().inv());
        return this.Apply(inv);
    }


    /**
     * Applies a binary operator passed as parameter on the AADD
     * passed as first two parameters and returns result.
     * @param op the operation
     * @param g parameter to be applied on this.
     * @return result of binary operation on this and g.
     */
    private AADD Apply(BiFunction<AffineForm, AffineForm, AffineForm> op, AADD g) {
        assert  g != null;

        AADD fT, fF;
        AADD gT, gF;

        // Check for the terminals.
        // It ends iteration and applies operation.
        if (isLeaf() && g.isLeaf() )
            return new AADD( op.apply(Value(), g.Value()) );

        // Otherwise, recursion following the T/F childs with largest index.
        int idx = Math.min(index, g.index);

        // Recursion, with new node
        if (index <= g.index) {
            fT = T(); fF = F();
        } else {
            fT = fF = this;
        }

        if (g.index <= index) {
            gT = g.T(); gF = g.F();
        } else {
            gT = gF = g;
        }

        AADD Tr = fT.Apply(op, gT);
        AADD Fr = fF.Apply(op, gF);

        return new AADD(idx, Tr, Fr);
    }

    /**
     * Applies a multiplication of AADD with BDD
     * passed as a parameter and returns result.
     *
     * The result is an AADD where the 0/1 are replaced with 0/AffineForm of the AADD.
     *
     * @param g parameter to be multiplied with this.
     * @return result of binary operation on this and g.
     */
     AADD AmultB(BDD g) {
        assert  g != null;
        assert  this != null;

        AADD fT, fF;
        BDD  gT, gF;
        int  idx;

        // Check for the terminals of the BDD g.
        // It ends iteration and applies operation.
        if (g == ZERO) return new AADD(0.0);
        if (g == ONE)  return this.clone();

        // Recursion, with new node that has
        // the *largest* indices.
        if (index <= g.index) {
            idx = index;
            fT = T(); fF = F();
        } else {
            idx = g.index;
            fT = fF = this;
        }

        if (g.index <= index) {
            gT = g.T(); gF = g.F();
        } else {
            gT = gF = g;
        }

        AADD Tr = fT.AmultB(gT);
        AADD Fr = fF.AmultB(gF);

        return new AADD(idx, Tr, Fr );
    }

    /**
     * Adds parameter to this and returns result
     * @param other parameter to be added to this.
     * @return result of this + other.
     */
    public AADD add(AADD other) {
        BiFunction<AffineForm, AffineForm, AffineForm> add = AffineForm::add;
        return this.Apply(add, other);
    }

    public AADD sub(AADD other) {
        BiFunction<AffineForm, AffineForm, AffineForm> sub = AffineForm::sub;
        return this.Apply(sub, other);
    }

    public AADD mult(AADD other) {
        BiFunction<AffineForm, AffineForm, AffineForm> mult = AffineForm::mult;
        return this.Apply(mult, other);
    }

    public AADD div(AADD other) {
        BiFunction<AffineForm, AffineForm, AffineForm> div = AffineForm::div;
        return this.Apply(div, other);
    }


    // TODO: Other operations from AffineForm. incl. Test!


    /**********************************************************
         * Comparison operations that make use of LP solver.
         * @ threhold is always 0 // all comparisons can be rewritten in this form (e.g. var1 > var2 <=> var1-var2 > 0)
         * @param op - relational operator used in constraint
         * @return
     ***********************************************************/

    /**
     Implements the relational operator less than {@code <}.
     It compares an AADD with AADD passed as a parameter and calls the LP solver to compute min and max.
     @param other - AADD to be compared with this
     @return BDD
     */
    public BDD Lt(AADD other) {
        assert other != null;

        int[] indexes=new int[height()];
        String[] signs=new String[height()];
        AADD temp=this.sub(other);
        return temp.CheckObjective(indexes, signs, 0,"<"); // this-g < 0
    }

    /**
     Implements relational operator less or equal than {@code <=}
     @param other - AADD to be compared with this
     @return BDD
     */
    public BDD Le(AADD other){
        assert other != null;

        int[] indexes=new int[height()];
        String[] signs=new String[height()];
        AADD temp=this.sub(other);
        return temp.CheckObjective(indexes, signs, 0, "<="); // this-g <=0
    }


    /**
     * computes the relational operator greater than {@code >}
     * @param other An AADD that is compared with this.
     * @return A BDD that represents the comparison of the leaves.
     */
    public BDD Gt(AADD other){
        assert other != null;

        int[] indexes=new int[height()];
        String[] signs=new String[height()];
        AADD temp=this.sub(other);
        return temp.CheckObjective(indexes, signs, 0, ">"); // this-other > 0
    }


    /**
    Implements relational operator greater or equal than {@code  >=}
     @param other - AADD to be compared with this
     @return A BDD that represents the comparison of the leaves.
     */
    public BDD Ge(AADD other) {
        assert other != null;

        int[] indexes=new int[height()];
        String[] signs=new String[height()];
        AADD temp=this.sub(other);
        return temp.CheckObjective(indexes, signs, 0, ">="); // this-other >= 0
    }


    /**
     * This method computes the minimum and maximum value of an AADD considering <ul>
     * <li> the conditions as linear constraints.
     * <li> the noise symbol's limitations to -1 to 1.
     * <li> The affine forms at the leaves as objective functions to be min/max. </ul>
     */
    public Range getMinMax() {
        Set<Range> bounds=getAllBounds();
        assert bounds.size()!=0;  // bounds should contain at least one value

        Range res = new Range(Range.Type.NaN);
        res.type = Range.Type.FINITE;
        for (Range b: bounds) {
            if (! b.isTrap()) {
                res.min = Math.min(res.min, b.min);
                res.max = Math.max(res.max, b.max);
            }
        }
        return res;
    }

    /**
     * This method computes minimum and maximum values of affine forms at the leaves
     * - the conditions as linear constraints.
     * - the noise symbol's limitations to -1 to 1.
     * - The affine forms at the leaves as objective functions to be min/max.
     */
    public Set<Range> getAllBounds() {
        Set<Range> bounds=new HashSet<>();
        int[] indexes=new int[height()];
        String[] signs=new String[height()];
        computeBounds(indexes, signs, 0, bounds);
        assert bounds.size() != 0;  // bounds should contain at least one value
        return bounds;
    }


    /**
     * Collects bounds of all leaves.
     * Calls callLPSolver to compute bounds for each leaf.
     *  when node is internal, it collects its condition.
     *  The method is called by getAllBounds and getMinMax.
     */
    private void computeBounds(int[] indexes, String[] operators, int len, Set<Range> bounds) {

        if (isLeaf()) {
            if (Value().getType() != Range.Type.FINITE || indexes.length == 0 || Value().getRadius() < LPCallTh)
                bounds.add( new Range(Value().getMin(), Value().getMax() ) );
            else
                bounds.add( callLPSolver(indexes, operators, len) );
        }
        else {  /* Recursion: collect conditions from root to leaf node */
            indexes[len]=index;
            operators[len]=">=";
            len++;
            T().computeBounds(indexes, operators, len, bounds);
            operators[len-1]="<";
            F().computeBounds(indexes, operators, len, bounds);
        }
    }


    /**
     * Computes bounds of a leaf node using LP solver that consider the linear constraints of the internal nodes.
     * Called by computeBounds that collects bounds of all leaves of AADD.
     * It calls LP solver from Math3 package to solve the underlying LP optimization problem.
     * The model of the problem is defined by:
     * - Objective function, defined by the AffineForm of leaf node Value()
     * - Constraints, defined by the conditions in the internal nodes on the path from root to leaf.
     *
     * @param indexes the indexes from the path from root to the respective leave; set of conditions
     * @param operators the operators.
     * @param len the sizes of the arrays.
     * @return
     */
    private Range callLPSolver(int[] indexes, String[] operators, int len) {
        assert len > 0;

        Collection<LinearConstraint> constraints = new ArrayList<>();

        // set union of noise symbols in Value() and conditions
        // creates dense set of variables for LP problem
        Set<Integer> symbols = new TreeSet<Integer>(Value().xi.keySet());

        for (int i=0; i<len; i++) {
            symbols.addAll(AADDMgr.getCond(indexes[i]).xi.keySet());
        }

        // stores partial deviations xi*ei in affine forms of conditions and value of leaf node
        // the last term is for r.
        double[] partial_terms=new double[symbols.size()];

        // add constraints on noise symbols Ei and r:
        // -1 <= Ei <= 1, -1
        for (int i = 0; i < symbols.size(); i++) {
            partial_terms[i] = 1.0; // stores partial term Ei*1.0;
            constraints.add(new LinearConstraint(partial_terms, Relationship.GEQ, -1.0));
            constraints.add(new LinearConstraint(partial_terms, Relationship.LEQ, 1.0));
            partial_terms[i] = 0.0;
        }

        // constraints from conditions, incl. r.
        for (int i=0; i < len; i++) {
            AffineForm condition = AADDMgr.getCond(indexes[i]);

            int k=0;
            for (Integer symb : symbols) {
                partial_terms[k] = condition.xi.getOrDefault(symb, 0.0);
                k++;
            }

            if (operators[i]==">=")
                constraints.add(new LinearConstraint(partial_terms, Relationship.GEQ, -condition.getCentral()+condition.getR()));
            else
                constraints.add(new LinearConstraint(partial_terms, Relationship.LEQ, -condition.getCentral()-condition.getR()));
        }

        // creates objective function
        int k=0;
        for (Integer symb : symbols) {
           partial_terms[k] = Value().xi.getOrDefault(symb, 0.0);
           k++;
        }
        // partial_terms[k] = Value().getR();
        // LinearObjectiveFunction f = new LinearObjectiveFunction(partial_terms, Value().getCentral());

        // approximation error must be treated as interval [-getR(), getR()]
        /* to guarantee safe inclusion we need to:
             - add getR() to objective function when computing max
             - subtract getR() from objective function when computing min */

        // System.out.print  ("       Condition's indexes: " );
        // for(int i=0; i<len; i++) System.out.print(""+indexes[i]+", ");System.out.println("");

        Range result = new Range(Range.Type.FINITE);
        PointValuePair solution;
        SimplexSolver solver = new SimplexSolver(1.0e-3, 100, 1.0e-30);
        try {
            LinearConstraintSet constraintSet = new LinearConstraintSet(constraints);
            solution = solver.optimize(new LinearObjectiveFunction(partial_terms, Value().getCentral()+Value().getR()), constraintSet, GoalType.MAXIMIZE, new NonNegativeConstraint(false));
            result.max = solution.getValue();
            solution = solver.optimize(new LinearObjectiveFunction(partial_terms, Value().getCentral()-Value().getR()), constraintSet, GoalType.MINIMIZE, new NonNegativeConstraint(false));
            result.min = solution.getValue();
        } catch (NoFeasibleSolutionException e) {
/*
            Value().diagnostics_on = true;
            System.out.println("");
            System.out.println("ERROR: No feasible solution found of LP problem. ");

            System.out.println("       Iterations: "+solver.getIterations());
            System.out.println("       Parameters of LP problem:");
            System.out.print  ("       Condition's indexes: " + len + ", indices: " );
            for(int i=0; i<len; i++) { System.out.print(""+indexes[i]+", ");  }
            System.out.println("");
            System.out.println("");
            System.out.println("Details:");
            for(int i=0; i<len; i++) System.out.println("       Condition "+indexes[i]+": "+AADDMgr.getCond(indexes[i])+" "+ operators[i]+" 0");
            System.out.println("       Value:         "+Value());
            System.out.println("");

            sanityCheck();*/
            this.leafValue.setNaN();
            result.setNaN();
            // throw new RuntimeException(("AADD-Error: No feasible solution."));
        }
        catch (UnboundedSolutionException e) {
            Value().diagnostics_on = true;
            System.out.println("");
            System.out.println("ERROR: Unbounded solution of LP problem. ");
            System.out.println("       Iterations: "+solver.getIterations());
            System.out.println("       Parameters of LP problem:");
            System.out.print  ("       Condition's indexes: " + len + ", indices: " );
            for(int i=0; i<len; i++) { System.out.print(""+indexes[i]+", ");  }
            System.out.println("");
            System.out.println("");
            System.out.println("Details:");
            for(int i=0; i<len; i++) System.out.println("       Condition "+indexes[i]+": "+AADDMgr.getCond(indexes[i])+" "+ operators[i]+" 0");
            System.out.println("       Value:         "+Value());
            System.out.println("");
            sanityCheck();
            throw new RuntimeException(("AADD-Error: unbounded solution."));

        }
        return result;
    }


    /**
     * Creates a new BDD level, depending on the satisfiability of a comparison.
     * @param indexes and operations of conditions from root to a leaf node
     * @param op
     * @return
     */
    private BDD CheckObjective(int[] indexes, String[] operators, int len, String op)
    {
        if (isLeaf()) {
            double min, max;

            /* no need to call solver if there are no partial deviations or no conditions. */
            if (Value().getType() == jAADD.Range.Type.SCALAR || indexes.length == 0) {
                min=Value().getMin();
                max=Value().getMax();
            } else {
                // Call LP solver.
                Range res = callLPSolver(indexes, operators, len);
                min = res.min;
                max = res.max;
            }

            if (op== ">=") {
                if (Double.compare(min, 0) > 0 || Math.abs(min)<2*Double.MIN_VALUE)
                    return ONE;
                if (Double.compare(max, 0) < 0)
                    return ZERO;
            }
            else if (op== ">") {

                if (Double.compare(min, 0) > 0)
                    return ONE;
                if (Double.compare(max, 0) < 0 || Math.abs(max)<2*Double.MIN_VALUE)
                    return ZERO;

            }
            else if (op == "<="){

                if (Double.compare(min, 0) > 0)
                    return ZERO;
                if (Double.compare(max, 0) < 0 || Math.abs(max)<2*Double.MIN_VALUE)
                    return ONE;

            }
            else if (op == "<"){

                if (Double.compare(min, 0) >0 || Math.abs(min)<2*Double.MIN_VALUE)
                    return ZERO;
                if (Double.compare(max, 0) < 0)
                    return ONE;

            }

            if (op==">=" || op==">")
                return new BDD(AADDMgr.newTopIndex(Value()), ONE, ZERO);
            else
                return new BDD(AADDMgr.newTopIndex(Value()), ZERO, ONE);
        }

        assert isInternal();

        /* Recursion step.*/
        indexes[len]=index;
        operators[len]=">=";
        len++;

        BDD Tr = T().CheckObjective(indexes, operators, len, op);
        operators[len-1]="<";
        BDD Fr = F().CheckObjective(indexes, operators, len, op);

        // before building and returning the new node,
        // we check for redundancies that we can reduce before
        // setting up and returning the result.
        // internal nodes whose both child are the same
        if (Tr == Fr)
            return Tr;
        else
            return new BDD(index, Tr, Fr);
    }


    /**
     * Method that casts T to AADD
     * @return T with type AADD.
     */
    protected AADD T() { return (AADD) T; }

    /**
     * Method that casts F to AADD
     * @return F with type AADD.
     */
    protected AADD F() { return (AADD) F; }

}