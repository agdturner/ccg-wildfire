/*
 * Copyright 2023 Centre for Computational Geography, University of Leeds.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.leeds.ccg.projects.wildfire;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.leeds.ccg.data.core.Data_Environment;
import uk.ac.leeds.ccg.data.format.Data_ReadCSV;
import uk.ac.leeds.ccg.generic.core.Generic_Environment;
import uk.ac.leeds.ccg.generic.io.Generic_Defaults;
import uk.ac.leeds.ccg.generic.io.Generic_IO;
import uk.ac.leeds.ccg.io.IO_Utilities;

/**
 * The main file to run.
 *
 * @author Andy Turner
 */
public class Run2 {

    HashMap<String, String> fieldLookup;
    // Selected fields
    ArrayList<String> sFields;
    HashMap<String, HashSet<String>> fieldValues;
    // For storing records against recordIDs.
    HashMap<Integer, String> records;
    String header;

    String area = "\"Over 10,000";

    // a
    HashSet<String> vs_5_16;
    int count_5_16 = 0;
    String s_5_16 = "5.16 ";

    HashSet<String> vs_5_16a;
    int count_5_16a = 0;
    String s_5_16a = "5.16a ";

    HashSet<String> vs_8_24;
    int count_8_24 = 0;
    String s_8_24 = "8.24 ";

    HashSet<String> vs_8_25;
    int count_8_25 = 0;
    String s_8_25 = "8.25 ";

    HashSet<String> vs_8_35;
    int count_8_35 = 0;
    String s_8_35 = "8.35 ";

    HashSet<String> vs_8_35a;
    int count_8_35a = 0;
    String s_8_35a = "8.35a ";

    // b
    HashSet<String> vs_3_7;
    String s_3_7 = "3.7 ";
    HashSet<String> vs_6_1;
    String s_6_1 = "6.1 ";
    int count_resourceCommittedGreaterThan3 = 0;

    public Run2() {
    }

    public static void main(String[] args) {
        new Run2().run();
    }

    public void run() {
        try {
            initialise();
            Path pData = Paths.get("C:", "Users", "geoagdt", "work", "research", "Wildfire", "data");
            Path pIn = Paths.get(pData.toString(), "input", "Richard Hawley 2.csv");
            Path pIn2 = Paths.get(pData.toString(), "input", "Richard Hawley 3.csv");
            Path pOut = Paths.get(pData.toString(), "output", "ab.csv");

            //DataFormatter df = new DataFormatter();
            Generic_Environment ge = new Generic_Environment(new Generic_Defaults(pData));
            Data_Environment de = new Data_Environment(ge);

            int rowid = process(de, pIn);
            System.out.println("");
            System.out.println("" + rowid + " records in \"" + pIn.getFileName().toString() + "\"");

            rowid = process(de, pIn2);
            System.out.println("");
            System.out.println("" + rowid + " records in \"" + pIn2.getFileName().toString() + "\"");

            output(pOut);
        } catch (Exception ex) {
            Logger.getLogger(Run2.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Initialise sFields and field values
     */
    private void initialise() {
        fieldLookup = new HashMap<>();
        fieldValues = new HashMap<>();
        records = new HashMap<>();
        sFields = new ArrayList<>();
        // 5_16
        sFields.add(s_5_16);
        vs_5_16 = new HashSet<>();
        fieldValues.put(s_5_16, vs_5_16);
        // 5_16a
        sFields.add(s_5_16a);
        vs_5_16a = new HashSet<>();
        fieldValues.put(s_5_16a, vs_5_16a);
        // 8_24
        sFields.add(s_8_24);
        vs_8_24 = new HashSet<>();
        fieldValues.put(s_8_24, vs_8_24);
        // 8_25
        sFields.add(s_8_25);
        vs_8_25 = new HashSet<>();
        fieldValues.put(s_8_25, vs_8_25);
        // 8_35
        sFields.add(s_8_35);
        vs_8_35 = new HashSet<>();
        fieldValues.put(s_8_35, vs_8_35);
        // 8_35a
        sFields.add(s_8_35a);
        vs_8_35a = new HashSet<>();
        fieldValues.put(s_8_35a, vs_8_35a);
        // 3_7
        sFields.add(s_3_7);
        vs_3_7 = new HashSet<>();
        fieldValues.put(s_3_7, vs_3_7);
        // 6_1
        sFields.add(s_6_1);
        vs_6_1 = new HashSet<>();
        fieldValues.put(s_6_1, vs_6_1);
    }

    public int process(Data_Environment de, Path pIn) {
        int rowid = 1;
        try {
            BufferedReader br = IO_Utilities.getBufferedReader(pIn);
            Data_ReadCSV r = new Data_ReadCSV(de);
            r.setStreamTokenizer(br, 9);
            String line = r.readLine();
            header = line;
            int hf = r.countFields(line);
            System.out.println("Number of fields " + hf);
            ArrayList<String> fields = r.parseLine(header);
            // Initialise field indexes.
            int i_5_16 = 0;
            int i_5_16a = 0;
            int i_8_24 = 0;
            int i_8_25 = 0;
            int i_8_35 = 0;
            int i_8_35a = 0;
            int i_3_7 = 0;
            int i_6_1 = 0;
            int i = 0;
            for (var field : fields) {
                System.out.println(field);
                //System.out.println(field + " " + (fieldLookup.size() - 1));
                if (field.contains(" ")) {
                    fieldLookup.put(field.split(" ")[0], field);
                } else {
                    fieldLookup.put(field, field);
                }
                // a
                if (field.startsWith(s_5_16)) {
                    i_5_16 = i;
                    System.out.println(field + " " + i_5_16);
                }
                if (field.startsWith(s_5_16a)) {
                    i_5_16a = i;
                    System.out.println(field + " " + i_5_16a);
                }
                if (field.startsWith(s_8_24)) {
                    i_8_24 = i;
                    System.out.println(field + " " + i_8_24);
                }
                if (field.startsWith(s_8_25)) {
                    i_8_25 = i;
                    System.out.println(field + " " + i_8_25);
                }
                if (field.startsWith(s_8_35)) {
                    i_8_35 = i;
                    System.out.println(field + " " + i_8_35);
                }
                if (field.startsWith(s_8_35a)) {
                    i_8_35a = i;
                    System.out.println(field + " " + i_8_35a);
                }
                // b
                if (field.startsWith(s_3_7)) {
                    i_3_7 = i;
                    System.out.println(field + " " + i_3_7);
                }
                if (field.startsWith(s_6_1)) {
                    i_6_1 = i;
                    System.out.println(field + " " + i_6_1);
                }
                
                i++;
            }
            line = r.readRow(hf);
            while (line != null) {
                ArrayList<String> row = r.parseLine(line);
                // a
                // 5.16
                String v_5_16 = row.get(i_5_16);
                vs_5_16.add(v_5_16);
                if (v_5_16.equalsIgnoreCase(area)) {
                    //System.out.println(area);
                    count_5_16++;
                    //records.put(rowid, row);
                    records.put(rowid, line);
                }
                // 5.16a
                String v_5_16a = row.get(i_5_16a);
                vs_5_16a.add(v_5_16a);
                if (!v_5_16a.isBlank()) {
                    if (Integer.parseInt(v_5_16a) >= 100) {
                        //System.out.println(v_5_16a);
                        count_5_16a++;
                        //records.put(rowid, row);
                        records.put(rowid, line);
                    }
                }
                // 8.24
                String v_8_24 = row.get(i_8_24);
                vs_8_24.add(v_8_24);
                if (v_8_24.equalsIgnoreCase(area)) {
                    //System.out.println(area);
                    count_8_24++;
                    //records.put(rowid, row);
                    records.put(rowid, line);
                }
                // 8.25
                String v_8_25 = row.get(i_8_25);
                vs_8_25.add(v_8_25);
                if (v_8_25.equalsIgnoreCase(area)) {
                    //System.out.println(area);
                    count_8_25++;
                    //records.put(rowid, row);
                    records.put(rowid, line);
                }
                // 8.35
                String v_8_35 = row.get(i_8_35);
                vs_8_35.add(v_8_35);
                if (v_8_35.equalsIgnoreCase(area)) {
                    //System.out.println(area);
                    count_8_35++;
                    //records.put(rowid, row);
                    records.put(rowid, line);
                }
                // 8.35a
                String v_8_35a = row.get(i_8_35a);
                vs_8_35a.add(v_8_35a);
                if (!v_8_35a.isBlank()) {
                    if (Integer.parseInt(v_8_35a) >= 100) {
                        //System.out.println(area);
                        count_8_35a++;
                        //records.put(rowid, row);
                        records.put(rowid, line);
                    }
                }
                // b
                int resourceCommitted = 0;
                String v_3_7 = row.get(i_3_7);
                vs_3_7.add(v_3_7);
                if (!v_3_7.isBlank()) {
                    if (Integer.parseInt(v_3_7) > 0) {
                        resourceCommitted ++;
                    }
                }
                String v_6_1 = row.get(i_6_1);
                vs_6_1.add(v_6_1);
                if (!v_6_1.isBlank()) {
                        resourceCommitted += Integer.parseInt(v_6_1);                    
                    if (resourceCommitted >= 4) {
                        count_resourceCommittedGreaterThan3 ++;
                        records.put(rowid, line);
                    }
                }
                
                line = r.readRow(hf);
                rowid++;
            }
        } catch (IOException ex) {
            Logger.getLogger(Run2.class.getName()).log(Level.SEVERE, null, ex);
        }
        return rowid;
    }

    public void output(Path pOut) {
        // Summary of fieldValues
        for (var field : fieldValues.keySet()) {
            System.out.println("");
            System.out.println("Field " + field);
            HashSet<String> values = fieldValues.get(field);
            for (var v : values) {
                System.out.println(v);
            }
        }

        // Record selection summary
        System.out.println("" + count_5_16 + " records with \"" + fieldLookup.get(s_5_16.trim()) + "\" = " + area + "\"");
        System.out.println("" + count_5_16a + " records with \"" + fieldLookup.get(s_5_16a.trim()) + "\" > 100");
        System.out.println("" + count_8_24 + " records with \"" + fieldLookup.get(s_8_24.trim()) + "\" = " + area + "\"");
        System.out.println("" + count_8_25 + " records with \"" + fieldLookup.get(s_8_25.trim()) + "\" = " + area + "\"");
        System.out.println("" + count_8_35 + " records with \"" + fieldLookup.get(s_8_35.trim()) + "\" = " + area + "\"");
        System.out.println("" + count_8_35a + " records with \"" + fieldLookup.get(s_8_35a.trim()) + "\" > 100");
        System.out.println("" + count_resourceCommittedGreaterThan3 + " records with resources committed >= 4");
        System.out.println("In total there are " + records.size() + " records:");

        // Output records
        try {
            Path outdir = pOut.getParent();
            if (!Files.exists(outdir)) {
                Files.createDirectories(outdir);
            }
            try (BufferedWriter bw = Generic_IO.getBufferedWriter(pOut, false)) {
                Files.createDirectories(pOut.getParent());
                bw.write(header);
                bw.write("\n");
                for (var id : records.keySet()) {
                    System.out.println(records.get(id));
                    bw.write(records.get(id) + "\n");
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Run2.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
