package jAADD

import org.apache.commons.math3.optim.linear.LinearConstraint
import java.io.BufferedWriter
import java.io.FileWriter
import java.io.IOException


/*
 * The function prints an overview of all data structures.
 */
internal fun printInfo() {
    println("  --------  Info on AADD  -------")
    println("      Noise symbols: " + NoiseVariables.maxIndex)
    for (symbol in NoiseVariables.names.keys) {
        println("        index $symbol is: ${NoiseVariables.names[symbol]}")
    }
    println("  Conditions: " + Conditions.X.size)
    for (idx in Conditions.X.keys) {
        println("      index: " + idx + ": " + Conditions.X[idx])
    }
    /*
    println("    AADD streams: " + AADDstreams.AADDStreams.size)
    for (name in AADDstreams.AADDStreams.keys) {
        println("        " + name + " with " + AADDstreams.AADDStreams[name]!!.size + " samples of type AADD")
    }
    println("    BDD streams: " + AADDstreams.BDDStreams.size)
    for (name in AADDstreams.BDDStreams.keys) {
        println("        " + name + " with " + AADDstreams.BDDStreams[name]!!.size + " samples of type BDD")
    } */
}


/**
 * This is a debug function, which should prints the inequation system
 * that is passed to the LPsolver.
 * It prints in the inequations of constraints
 * and interprets the array partial_terms as objective function
 * and prints it in a new text-file.
 * @param fileName Name of the new text-file
 * @param constraints contains the linear inequations
 * @param partial_terms should contain the objective function
 */
internal fun AADD.printInequationSystem(fileName: String, constraints: Collection<LinearConstraint>, partial_terms: DoubleArray, value: AffineForm) {
    try {
        FileWriter("/out/$fileName.txt").use { writer ->
            BufferedWriter(writer).use { bw ->
                bw.write("Inequation system")
                bw.newLine()
                var eqCount = 1
                for (con in constraints) {
                    val line = StringBuilder("($eqCount)")
                    val coefficients = con.coefficients.toArray()
                    for (i in coefficients.indices) {
                        val connect = if (i > 0) "+" else ""
                        line.append(connect + " " + coefficients[i] + " * e" + (i + 1))
                    }
                    val sign = con.relationship.toString()
                    line.append(" " + sign + " " + con.value)
                    bw.write(line.toString())
                    bw.newLine()
                    eqCount++
                }
                bw.write("objective function")
                bw.newLine()
                val line = StringBuilder("")
                for (i in partial_terms.indices) {
                    val connect = if (i > 0) "+" else ""
                    line.append(connect + " " + partial_terms[i] + " * e" + (i + 1))
                }
                bw.write("maxizime " + line.toString() + " + " + -value.x0 + " + " + value.r)
                bw.newLine()
                bw.write("minizime " + line.toString() + " + " + -value.x0 + " - " + value.r)
                bw.close()
            }
        }
    } catch (ioE: IOException) {
        System.err.format("IOException: %s%n", ioE)
    }
}



/** Checks  consistency of index structure. Just for testing. */
internal fun DD<*>.sanityCheck() {
    if (isLeaf) {
        assert(value != null)
        assert(T == null && F == null)
        return
    }
    assert(T != null) { "  DD corrupted: T is null." }
    assert(F != null) { "  DD corrupted: F is null." }
    assert((T!!.index > index) or T.isLeaf) { "  DD corrupted: index " + index + " and T.index " + T.index }
    assert((F!!.index > index) or T.isLeaf) { "  DD corrupted: index " + index + " and T.index " + T.index }
    assert(AFConstr() != null) { "  DD condition " + index + " not properly set" }
    T.sanityCheck()
    F.sanityCheck()
}