package jAADD

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.util.*
import kotlin.collections.HashMap



/**
 * The class implements an IOManager for the AADD package.
 * It allows to add documentation for noise symbols and for the index conditions of AADD and BDD.
 * Furthermore, it allows to write traces, tagged by integer to files.
 *
 * The class AADDstream keeps the documentation of
 * - the noise symbols of Affine Forms. Each noise symbol is documented by some strings, i.e. Name, Unit, ...
 * - the conditions of the AADD. Here, each index refers to a condition of the kind `AAF > 0`.
 * Furthermore, it implements the Json import/export.
 */
@Deprecated("Deprecated - use AADDStream instead.")
object AADDstreams {

    /**
     * The following implements a map of streams of BDD and AADD.
     */
    var BDDStreams  = HashMap<String, TreeMap<Double, BDD>>()
    var AADDStreams = HashMap<String, TreeMap<Double, AADD>>()


    /** Deletes all information from the AADD streams. */
    @JvmStatic
    fun clear() {
        BDDStreams.clear()
        AADDStreams.clear()
    }

    // Saves a sample of an AADD to the AADDStream with the key name, and in the stream with the key key.
    @JvmStatic
    fun toStream(name: String, dd: AADD, key: Double) { // If not there, add the respective Stream.
        if (AADDStreams[name] == null) {
            AADDStreams[name] = TreeMap()
        }
        // Get the respective Stream.
        val innermap = AADDStreams[name]
        // Add a sample.
        innermap!![key] = dd
    }

    // Saves a sample of an AADD to the AADDStream with the key name, and in the stream with the key key.
    fun toStream(name: String, dd: BDD, key: Double) { // If not there, add the respective Stream.
        if (BDDStreams[name] == null) {
            BDDStreams[name] = TreeMap()
        }
        // Get the respective Stream.
        val innermap = BDDStreams[name]
        // Add a sample.
        innermap!![key] = dd
    }


    /**
     * Writes all information into json files:
     * - noise variables -> filename+.noiseVariables.json
     * - conditions      -> filename+.conditions.json
     * - AADD streams    -> filename+.streamname.json
     * - BDD streams     -> filename+.streamname.json
     */
    @JvmStatic
    fun toJson(filename: String) {
        val gson = GsonBuilder().setPrettyPrinting().create()
        NoiseVariables.toJson("$filename.noiseVariables.json")
        Conditions.toJson("$filename.conditions.json")

        for (varname in AADDStreams.keys) {
            val fw = java.io.FileWriter("$filename.$varname.json")
            gson.toJson(AADDStreams[varname], fw)
            fw.close()
        }

        for (varname in BDDStreams.keys) {
            val fw = java.io.FileWriter("$filename.$varname.json")
            gson.toJson(BDDStreams[varname], fw)
            fw.close()
        }
    }


    /**
     * Reads an AADD from a json file.
     * @param path specifies both the path and file name.
     * @param name name of the AADD stream
     */
    @JvmStatic
    fun fromJson(path: String, name: String): TreeMap<Double, AADD> {
        val empMapType = object : TypeToken<HashMap<Double?, AADD?>?>() {}.type
        val rf = java.io.FileReader(path)
        val gson = Gson()
        val stream = gson.fromJson<TreeMap<Double, AADD>>(rf, empMapType)
        AADDStreams[name] = stream
        return stream
    }
}