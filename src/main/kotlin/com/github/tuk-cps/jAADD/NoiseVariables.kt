package jAADD

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.FileReader
import java.io.FileWriter

/**
 * This object manages the noise variables.
 * - provides unique indexes
 * - maintains information on kind and documentation
 */
object NoiseVariables {
    /**
     * The maximum index.
     * We use index numbers from 1, each new index increases maxIndex.
     */
    internal var maxIndex = 0

    /** A set of names for each noise variable index. */
    internal val names = HashMap<Int, String>()

    /** Returns a new index of a noise variable. */
    fun newNoiseVar(): Int = ++maxIndex

    /** Returns a new noise variable with name. */
    fun noiseVar(n: String): Int {
        for ((index, name) in NoiseVariables.names)
            if (n == name) return index
        names[++maxIndex] = n
        return maxIndex
    }

    override fun toString(): String {
        var s = "\nNoise variables in use:\n"
            s+= "-----------------------\n"
        for( (key, doc) in names) {
            s+=("Index: $key = $doc \n")
        }
        return s
    }

    fun toJson(): String = Conditions.gson.toJson(this)
    fun toJson( filename: String ) {
        val fw = FileWriter(filename)
        Conditions.gson.toJson(this, fw)
        fw.close()
    }

    /**
     * Read noise symbols documentation in json format using the gson library.
     */
    fun fromJson(filename: String) {
        var file = FileReader(filename)

        var empMapType = object : TypeToken<Map<Int, String>>() {}.type
        val gson = Gson()
        maxIndex = 0
        names.clear()
        names.putAll(gson.fromJson(file, empMapType))
        maxIndex = names.size
    }

    fun clean() {
        maxIndex = 0
        names.clear()
    }

}