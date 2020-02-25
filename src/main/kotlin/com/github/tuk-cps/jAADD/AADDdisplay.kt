package jAADD

import javax.swing.tree.DefaultMutableTreeNode

internal fun displayTree(v: DD<*>, title: String): DefaultMutableTreeNode {
    if (v.isLeaf) {
        return DefaultMutableTreeNode("$title: $v")
    } else {
        var treeNode = DefaultMutableTreeNode("$title: is "+Conditions.X[v.index])
        treeNode.add(displayTree( v.T!!, "T" ))
        treeNode.add(displayTree(v.F!!, "F" ))
        return treeNode
    }
}

/**
 * Opens a window and shows the AADD.
 * One can combine several DDs in one window.
 */
fun display(vararg dds: DefaultMutableTreeNode, title: String="jAADD AADD/BDD Tree view") {
    AADD.toStringVerbose = true
    val frame = javax.swing.JFrame(title)
    for (dd in dds) {
        val tree = javax.swing.JTree(dd)
        frame.add(tree)
    }
    frame.setSize(550, 400)
    frame.isVisible = true
}


/**
 * Opens a window and shows the AADD.
 * Simple version, just writes a single DD with name into a single window.
 */
internal fun display(dd: DD<*>, name:  String) {
    val frame = javax.swing.JFrame(name)
    val dt = displayTree(dd, name)
    val tree = javax.swing.JTree(dt)
    frame.add(tree)
    frame.setSize(550, 400)
    frame.isVisible = true
}

fun AADD.display(name: String) = display(this, name)
fun BDD.display(name: String) = display(this, name)
