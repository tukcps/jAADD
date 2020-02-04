package exprParser

import jAADD.AADD
import jAADD.BDD
import jAADD.DD

class ExprTreeConstrNet(l: ExprTree, r: ExprTree):
        ExprTree(l.value,"ConstNet", null) {

    var exprTree = r
    var leftSide = l

    override fun evalUp() {
        exprTree.evalUp()
    }

    override fun evalUpRec() {
        exprTree.evalUpRec()
    }

    override fun evalDown() {
        exprTree.evalDown()
    }

    override fun toString() = "EQN " + leftSide.toString() + "==" + exprTree.toString()

    fun solve(iterations: Int) {
        for (i in 1 .. iterations) {
            value = leftSide.value
            exprTree.evalUpRec()
            exprTree.evalDownRec()
            value = exprTree.value
            leftSide.value = value
        }
        println("lval="+leftSide.value+" "+leftSide)
    }



}