package jAADD;

import java.util.function.BiFunction;
import java.util.function.Function;


/**
 *  The class BDD implements an ROBDD.
 *  It is derived from the super class DD.
 *  It has exactly two leaves that are either ONE or ZERO.
 *  @author Christoph Grimm, Carna Zivkovic
 */
public class BDD extends DD<Boolean> implements Cloneable {

    /**
     * ONE and ZERO are the only leaf nodes that may be used.
     * BOOL models an unknown value that is ONE or ZERO and remains unknown.
     */
    public static BDD ONE  = new BDD(true);      // Leave of value ONE
    public static BDD ZERO = new BDD(false);     // Leave of value ZERO
    public static BDD BOOL = new BDD(1, ONE, ZERO);


    /**
     * Creates a new leaf node.
     * The class is not needed for users as in BDD there exist only two leaf nodes,
     * ONE and ZERO. It is used only once.
     * Therefore it is private. Use ONE and ZERO to get a leaf.
     * @param value Value of leaf ... DONT USE THIS CONSTRUCTOR unless you know what you do.
     */
    BDD(Boolean value) {
       super(value);
    }


    /**
     * Use this to get a leaf node of a given boolean value.
     */
    public static BDD newLeaf(boolean value) {
        if (value) return ONE;
        else return ZERO;
    }


    /**
     * Creates an internal node with a given index.
     * index must refer to an existing condition.
     * It must be smaller than maxIndex.
     * @param index is the index of the node level.
     * @param T is the true child; no copy is made.
     * @param F is the false child; no copy is made.
     */
    public BDD(int index, BDD T, BDD F) {
        super(index, T, F);
    }


    /**
     *  Clone method. Copies the tree structure, but not conditions.
     *  The leaves are not copied for BDD, where ONE and ZERO are merged.
     */
    @Override
    public BDD clone() {
        if ( isInternal() ) {
            BDD klon = new BDD(index, (BDD) T, (BDD) F);
            klon.T = ((BDD) T).clone(); // deep copy
            klon.F = ((BDD) F).clone(); // deep copy
            return klon;
        }
        else // for leaves
        {
            return this; // shallow copy for leaves (ONE, ZERO) of BDD.
        }
    }

    protected BDD T() { return (BDD) T; }
    protected BDD F() { return (BDD) F; }


    /**
     * Applies a unary operator on a BDD and returns its result.
     * It works recursively.
     * @param op a function on the BDD with a parameter.
     * @return a new BDD that is the result of the applied function.
     */
    private BDD Apply(Function<BDD, BDD> op) {

        // Recursion stops at leaves.
        if (isLeaf()) return op.apply(this);

        // Otherwise we recurse to T and E nodes.
        return new BDD(index, T().Apply(op), F().Apply(op));

    }


    /**
     * Computes the complementary function of a given BDD.
     * @return complementary of this.
     */
    public BDD negate() {

        // For leaves stop recursion
        Function<BDD, BDD> negate = (a) -> {
            if (a == ONE) return ZERO;
            else return ONE;
        };

        // Otherwise recursion to leaves and apply negate there.
        return this.Apply(negate);
    }


    /**
     * Applies a binary operator passed as las parameter on the BDD
     * passed as first two parameters and returns result.
     * @param op the operation
     * @param g parameter 2
     * @return result of binary operation on the parameters
     */
    public BDD Apply(BiFunction<Boolean, Boolean, Boolean> op, BDD g) {
        assert ( g != null );

        BDD fT, fF, gT, gF;  // T, F of this and/or g
        int idx;

        // Iteration to leaves.
        // It ends iteration and applies operation.
        if (isLeaf() && g.isLeaf() )
            return newLeaf(op.apply(Value(), g.Value()));

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

        // do the recursion
        BDD Tr = fT.Apply(op, gT);
        BDD Fr = fF.Apply(op, gF);

        // now, the operation is finished, but
        // we check for redundancies that we can reduce before
        // setting up and returning the result.
        if (Tr == Fr) return Tr;
        else return new BDD(idx, Tr, Fr);
    }


    public BDD and(BDD other) {
        BiFunction<Boolean, Boolean, Boolean> and = (a, b) -> a && b;
        return this.Apply(and, other);
    }

    public BDD or(BDD other) {
        BiFunction<Boolean, Boolean, Boolean> or = (a, b) -> a || b;
        return this.Apply(or, other);
    }

    public BDD xor(BDD other) {
        BiFunction<Boolean, Boolean, Boolean> xor = (a, b) -> a != b;
        return this.Apply(xor, other);
    }

    public BDD nand(BDD other) {
        BiFunction<Boolean, Boolean, Boolean> nand = (a, b) -> !(a && b);
        return this.Apply(nand, other);
    }

    public BDD nor(BDD other) {
        BiFunction<Boolean, Boolean, Boolean> or = (a, b) -> !(a || b);
        return this.Apply(or, other);
    }

    public BDD xnor(BDD other) {
        BiFunction<Boolean, Boolean, Boolean> xnor = (a, b) -> (a == b);
        return this.Apply(xnor, other);
    }


    /**
     * Compares this BDD with other BDD for exact equality.
     * @param other
     * @return
     */
    public boolean equals(BDD other) {
        if (this == other ) return true;
        if (other == null || this.getClass() != other.getClass()) return false;
        if (isLeaf()) {
            return other == this;
        } else
            return (T().equals(other.T()) && F().equals(other.F()));
    }


    /**
     *  The ITE function merges  BDD in an if-then-else-function.
     *  Note, that the condition itself that is this BDD, is also a BDD.
     *  The parameters are not changed.
     */
    public BDD ITE(BDD t, BDD e) // throws CloneNotSupportedException
    {
        if (this == ONE) return t.clone();
        else if (this == ZERO) return e.clone();

        BDD ct  = this.and(t);
        BDD cne = this.negate().and(e);
        return ct.or(cne);
    }

    public AADD ITE(AADD t, AADD e) {
        assert( t != null);
        assert( e != null);

        if (this == ONE) return t.clone();
        else if (this == ZERO) return e.clone();

        AADD Temp1 = t.AmultB(this);
        AADD Temp2 = e.AmultB(this.negate());
        return Temp1.add(Temp2);
    }
}



