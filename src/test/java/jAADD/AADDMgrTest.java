package jAADD;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.junit.jupiter.api.Assertions.*;

class AADDMgrTest {

    AffineForm a, b, c;
    AADD aa, bb, cc, dd;
    BDD cond;
    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @BeforeEach
    void setUp() {
        // System.out.println("=== IOManagerTests: SetUp ===");
        a = new AffineForm(1, 3, 1);
        b = new AffineForm(2, 4, 2);
        c = new AffineForm(3, 5, 3, "Third Symbols name", "3rd Unit", "3rd Comment");

        aa = new AADD(a);
        bb = new AADD(b);
        cc = new AADD(c);

        cond = aa.Gt(new AADD(2.0)); // a > 0.0
        dd = cond.ITE(aa, bb);

        // System.out.println("condheight =" + cond.toString());
        // System.out.println("condheight =" + dd.toString());

        assertEquals(cond.height(), 1);
        assertEquals(dd.height(), 1);
    }

    @Test
    void checkAffineFormDocs() {
        System.out.println("=== IOManagerTests: checkAffineFormDocs ===");
        a.setDocs(1, "First symbol name", "1st Unit", "1st Comment");
        b.setDocs(2, "Second Symbol name", "2nd Unit");
        // c.setDoc(3, "Third Symbols name"); Is done via constructor!

        c = c.add(a.add(b)); // c = a+b+c, should have all three symbols now.
        AADDMgr.writeToJson();     // See Json File for results.
    }

    @Test
    void checkIndexDocs() {

    }

    @Test
    void checkDocsStream() {

    }

    @Test
    void checkDocFromJson() {
        System.out.println("=== IOManagerTests: checkDocFromJson ===");

        String path = "src/test/json/checkDocFromJson/";
        System.out.println("  Reading Documentation from files in "+path);
        AADDMgr.readDocFromJson(path);

        AADDMgr.PrintInfo();
    }

    @Test
    void checkReadAADDStream() {
        System.out.println("=== IOManagerTests: checkReadAADDStream ===");
        AADDMgr.clear();
        System.out.println("  Reading AADD level from JSON ... ");
        AADDMgr.readAADDSampleFromJson("src/test/json/checkReadAADDStream/AADD.level.json", "level");

        AADDMgr.PrintInfo();
    }
}