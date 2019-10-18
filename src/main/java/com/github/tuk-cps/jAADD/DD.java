package jAADD;


/**
 * The class DD implements a decision diagram as base class template leaf types.
 * It is the base class from which different kind of DD are inherited.
 * It provides the basic framework, but not the type of the leaves.
 * This involves in particular the management of the conditions and
 * index that are common for AADD and BDD.
 *
 * @author Christoph Grimm, Carna Zivkovic
 */
public class DD <ValT>  {

    /**
     * The DD is ordered by the index. The index of leaves is Integer.MAXVALUE.
     * The index of other nodes grows from 0 (root) with increasing height of the graph.
     */
    protected int index;

    /** The true and false childs. */
    protected DD<ValT> T, F;   // true and false nodes.

    /** Value of a leaf node, otherwise null. */
    protected ValT leafValue;

    /**
     * Creates a leaf node with value val.
     * @param val Value of leaf. NOT TO BE USED WITH BDD.
     */
    protected DD(ValT val)  {
        index = Integer.MAX_VALUE;
        leafValue = val;
    }


    /**
     * Creates an internal node with index i that is already in use and the two child.
     * @param i of condition. Index must be already assigned a condition.
     * @param T DD that will be used for T.
     * @param F DD that will be used for F.
     */
    protected DD(int i,  DD<ValT> T, DD<ValT> F) {
        assert T != null;
        assert F != null;
        assert (i == 1) || (Conditions.getX(i) != null) || (Conditions.getXBool(i) != null);
        assert i < T.index : "  DD insane: index "+i+" but T "+T.index;
        assert i < F.index : "  DD insane: index "+i+" but F "+F.index;

        this.index = i;
        this.T = T;
        this.F = F;
        leafValue = null;
    }


    /**
     * Returns true if the node is a leaf.
     */
    public boolean isLeaf() { return index == Integer.MAX_VALUE; }
    public boolean isInternal() { return index != Integer.MAX_VALUE; }

    // Returns the number of leaves.
    public int numLeaves() {
        if (isLeaf()) return 1;
        else return T.numLeaves() + F.numLeaves();
    }

    // Returns the height of the tree.
    public int height() {
        if (isLeaf()) return 0;
        else return 1+Math.max(T.height(), F.height() );
    }


    public ValT Value() {
        assert isLeaf(): "  DD: Value of internal node requested. ";
        return leafValue;
    }


    /**
     * Gets the condition to which the index refers.
     * @return Affine form that models a linear constraint {@code cond > 0}.
     */
    public AffineForm Cond()   {
        return Conditions.getX(index);
    }

    /**
     * Returns true is the condition is a simple boolean variable.
     * @return
     */
    public Boolean BoolCond() {
        return Conditions.getX(index) == null;
    }


    @Override
    public String toString() {
        String str = "";
        if (isLeaf()) {
            str += Value().toString();
        } else {
            str = "( I: " + index;
            str += ", T: " + T + ", E: " + F +")";
        }
        return str;
    }


    /**
     * Allows checking of consistency of index structure.
     * Just for testing.
     */
    public void sanityCheck() {
        if (isLeaf()) return;
        assert T != null: "  DD corrupted: T is null.";
        assert F != null: "  DD corrupted: F is null.";
        assert T.index > index | T.isLeaf(): "  DD corrupted: index "+index+" and T.index "+T.index;
        assert F.index > index | T.isLeaf(): "  DD corrupted: index "+index+" and T.index "+T.index;
        assert Cond() != null: "  DD condition "+this.index+" not properly set";
        T.sanityCheck();
        F.sanityCheck();
    }
}


