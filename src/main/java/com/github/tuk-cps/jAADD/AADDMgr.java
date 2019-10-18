package jAADD;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;

import java.util.HashMap;
import java.util.Map;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.math3.optim.linear.LinearConstraint;


import static java.lang.System.out;


/**
 * The class implements an IOManager for the AADD package.
 * It allows to add documentation for noise symbols and for the index conditions of AADD and BDD.
 * Furthermore, it allows to write traces, tagged by integer to files.
 *
 * The class AADDmanager keeps the documentation of
 * - the noise symbols of Affine Forms. Each noise symbol is documented by some strings, i.e. Name, Unit, ...
 * - the conditions of the AADD. Here, each index refers to a condition of the kind {@code AAF > 0}.
 * Furthermore, it implements the Json import/export.
 */
public class AADDMgr {

    // flag that enables an print of inequation for the LPsolver
    public static boolean debugFlagLPsolvePrint;


    // A stream of BDD, tagged with a double that models time.
    public static class BDDStream extends HashMap<Double, BDD> {}

    // A stream of AADD, tagged with a double that models time.
    // public class AADDStream extends HashMap<Double, AADD> {};

    // A stream of Affine Forms, time-tagged with an integer.
    public static class AffineFormStream extends HashMap<Double, AffineForm> {}

    // The documentation of the index of the BDD and AADD.
    public class IndexDocs extends HashMap<Integer, String> {}

    // The documentation of the noise symbols of Affine Forms.
    public class NoiseSymbolDocs extends HashMap<Integer, String[]> {}


    /**
     * The following holds documentation strings for the conditions of the AADD's conditions.
     * Each condition is uniquely identified by it's index of type int.
     */
    protected static HashMap<Integer, String>  iDocs = new HashMap<Integer, String> ();
    public  final void setIndexDoc(Integer key, String name) { iDocs.put(key, name); }
    public  static String getIndexDoc(Integer key) { return iDocs.get(key); }


    /**
     * The following holds documentation for the Affine form's noise symbols.
     * Each noise symbol is documented by an array of strings.
     */
    static protected HashMap<Integer, String[]> nsDocs = new HashMap<Integer, String[]>();
    static void setNoiseSymbolDocs(Integer key, String... name) {
        assert(name.length <= 3);
        if (name.length >0) {
            nsDocs.put(key, name);
            // System.out.println(" NS doc added for: "+ name[0]);
        }
    }

    /**
     * The method returns  Noise Symbol documentation.
     *
     * @param key is the index of the noise symbol Ei.
     * @return An array of variable lenght, containing name, unit, and comment iff specified in constructor.
     */
    static String[] getNoiseSymbolDocs(Integer key) { return nsDocs.get(key); }


    /**
     * The following implements a map of streams of BDD and AADD.
     */
    static protected HashMap<String, BDDStream >  BDDStreams = new HashMap<String, BDDStream>();
    static protected HashMap<String, HashMap<Double, AADD>  > AADDStreams = new HashMap<String, HashMap<Double, AADD>  >();
    static protected HashMap<String, AffineFormStream > AffineFormStreams = new HashMap<String, AffineFormStream>();


    // deletes all information.
    public static void clear() {
        iDocs.clear();
        nsDocs.clear();
        BDDStreams.clear();
        AADDStreams.clear();
        AffineFormStreams.clear();
    }

    // Saves a sample of an AADD to the AADDStream with the key name, and in the stream with the key key.
    public static void addAADDSample(String name, AADD dd, Double key) {

        // If not there, add the respective Stream.
        if (AADDStreams.get(name)==null) {
            AADDStreams.put(name, new HashMap<Double, AADD> () );
        }

        // Get the respective Stream.
        HashMap<Double, AADD>  innermap = AADDStreams.get(name);

        // Add a sample.
        innermap.put(key, dd);
    }


    // Saves a sample of an AADD to the AADDStream with the key name, and in the stream with the key key.
    public static void addBDDSample(String name, BDD dd, Double key) {

        // If not there, add the respective Stream.
        if (BDDStreams.get(name)==null) {
            BDDStreams.put(name, new BDDStream() );
        }

        // Get the respective Stream.
        BDDStream innermap = BDDStreams.get(name);

        // Add a sample.
        innermap.put(key, dd);
    }


    // Saves a sample of an AADD to the AADDStream with the key name, and in the stream with the key key.
    public static void addAffineFormSample(String name, AffineForm aaf, Double key) {

        // If not there, add the respective Stream.
        if (AffineFormStreams.get(name)==null) {
            AffineFormStreams.put(name, new AffineFormStream() );
        }

        // Get the respective Stream.
        AffineFormStream innermap = AffineFormStreams.get(name);

        // Add a sample.
        innermap.put(key, aaf);
    }

    /**
     * This is a debug function, which should prints the inequation system
     * that is passed to the LPsolver.
     * It prints in the inequations of constrains
     * and interprets the array partial_terms as objective function
     * and prints it in a new text-file.
     * @param fileName Name of the new text-file
     * @param constraints contains the linear inequations
     * @param partial_terms should contain the objective function
     */
    public static void printInequationSystem(String fileName, Collection<LinearConstraint> constraints, double[] partial_terms, AffineForm value){
        try (FileWriter writer = new FileWriter(fileName+".txt");
             BufferedWriter bw = new BufferedWriter(writer)) {

            bw.write("Unequation system");
            bw.newLine();

            int eqCount = 1;
            for (LinearConstraint con:constraints){
                StringBuilder line=new StringBuilder("("+eqCount+")");
                double [] coefficients =con.getCoefficients().toArray();
                for (int i=0; i<coefficients.length;i++){
                    String connect =i>0?"+":"";
                    line.append(connect+" "+coefficients[i]+" * e"+(i+1));
                }
                String sign =con.getRelationship().toString();
                line.append(" "+sign+" "+con.getValue());
                bw.write(line.toString());
                bw.newLine();
                eqCount++;
            }
            bw.write("objective function");
            bw.newLine();
            StringBuilder line=new StringBuilder("");
            for (int i=0;i<partial_terms.length;i++){
                String connect =i>0?"+":"";
                line.append(connect+" "+partial_terms[i]+" * e"+(i+1));
            }
            bw.write("maxizime "+line.toString()+" + "+(- value.getCentral()) +" + "+value.getR());
            bw.newLine();
            bw.write("minizime "+line.toString()+" + "+(-value.getCentral()) +" - "+value.getR());
            bw.close();
        } catch (IOException ioE) {
            System.err.format("IOException: %s%n", ioE);
        }
    }

    static public void writeToJson()
    {
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            FileWriter fw = new FileWriter("out/json/NoiseSymbolDocs.json");
            gson.toJson(nsDocs, fw);
            fw.close();

            fw = new FileWriter("out/json/IndexDocs.json");
            gson.toJson(iDocs, fw);
            fw.close();

            for(String varname: AADDStreams.keySet()) {
                fw = new FileWriter("out/json/AADD."+varname+".json");
                gson.toJson(AADDStreams.get(varname), fw);
                fw.close();
            }

            for(String varname: BDDStreams.keySet()) {
                fw = new FileWriter("out/json/BDD."+varname+".json");
                gson.toJson(BDDStreams.get(varname), fw);
                fw.close();
            }

            for(String varname: AffineFormStreams.keySet()) {
                fw = new FileWriter("out/json/AffineForm."+varname+".json");
                gson.toJson(AffineFormStreams.get(varname), fw);
                fw.close();
            }

        } catch (Exception e) {
            out.println("AADD: writing to JSON file failed.");
            out.println(e);

        }
    }


    /**
     * Reads the documentation in the the IOManager data structures.
     * @param path  speciefies the directory in which the files NoiseSymbolsDocs.json and
     *              IndexSymbolDocs.json are located.
     */
    public static void readDocFromJson(String path)
    {
        try {
            // get the file with noise symbols documentation
            if (path != "") path.concat("/");
            FileReader file = new FileReader(path + "NoiseSymbolDocs.json");

            // read noise symbols documentation in json format using the gson library.
            java.lang.reflect.Type empMapType = new TypeToken<Map<Integer, String[]>>() {}.getType();
            // java.lang.reflect.Type empMapType = new TypeToken<NoiseSymbolDocs>() {}.getType();

            Gson gson = new Gson();
            nsDocs.clear();
            nsDocs = gson.fromJson(file, empMapType);

            // read index documentation in json format
            empMapType = new TypeToken<HashMap<Integer, String> >() {}.getType();
            file = new FileReader(path + "IndexDocs.json");
            iDocs.clear();
            iDocs = gson.fromJson(file, empMapType);

        } catch (Exception e) {
            out.println("AADD: reading from JSON files failed.");
            out.println(e);
        }
    }

    /**
     * Reads an AADD from a json file.
     * @param path specifies both the path and file name.
     * @param name name of the AADD
     */
    public static void readAADDSampleFromJson(String path, String name){
        try {
            java.lang.reflect.Type empMapType = new TypeToken<HashMap<Double, AADD> >() {}.getType();
            FileReader rf = new FileReader(path);
            Gson gson = new Gson();
            HashMap<Double, AADD>  stream = gson.fromJson(rf, empMapType);
            AADDStreams.put(name, stream);
        } catch (Exception e) {
            out.println("AADD: reading from JSON files in path " + path + " failed.");
            out.println(e);
        }
    }


    // Just for debug.
    public static void PrintInfo()
    {
        out.println("  --- Infos of IOManager: ---");
        out.println("  Noise symbol documentations: "+nsDocs.size());
        for(Integer symbol: nsDocs.keySet()) {
            out.print("        symbol no. "+ symbol + ": ");
            for(String doc: nsDocs.get(symbol)) out.print(doc+" ");
            out.println();
        }
        out.println("  Index docs: "+iDocs.size());
        for (Integer idx: iDocs.keySet()) {
            out.println("        index: " + idx + ": " + iDocs.get(idx));
        }

        out.println("  AADD streams: " + AADDStreams.size());
        for (String name: AADDStreams.keySet()) {
            out.println("        "+name+ " with " + AADDStreams.get(name).size() + " samples of type AADD");
        }

        out.println("  BDD streams: " + BDDStreams.size());
        for (String name: BDDStreams.keySet()) {
            out.println("        "+name+ " with " + BDDStreams.get(name).size() + " samples of type BDD");
        }

        out.println("  AffineForm streams: " + AffineFormStreams.size());
        for (String name: AffineFormStreams.keySet()) {
            out.println("        "+name+ " with " + AffineFormStreams.get(name).size() + " samples of type AffineForm");
        }
    }


}

