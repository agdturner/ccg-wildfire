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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
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

    // Identifies those records that meet a criteria.
    HashSet<Integer> a;

    // Identifies those records that meet b criteria.
    HashSet<Integer> b;

    // Identifies those records that meet c criteria.
    HashSet<Integer> c;

    // A class of area for which selections are made.
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

    // c
    String s_2_1 = "2.1 ";
    String s_2_5 = "2.5 ";
    String s_2_6 = "2.6 ";
    HashMap<Integer, Long> callsToStops;
    HashMap<Integer, Long> stopsToCloses;
    HashMap<Integer, Long> callsToCloses;
    HashMap<Integer, String> dates;
    HashMap<Integer, String> times;

    int count_resourceCommittedGreaterThan3 = 0;
    int count_durationGreaterThan6Hours = 0;

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
            Path pOut = Paths.get(pData.toString(), "output", "abc.csv");

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

        // a
        a = new HashSet<>();
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

        // b
        b = new HashSet<>();
        // 3_7
        sFields.add(s_3_7);
        vs_3_7 = new HashSet<>();
        fieldValues.put(s_3_7, vs_3_7);
        // 6_1
        sFields.add(s_6_1);
        vs_6_1 = new HashSet<>();
        fieldValues.put(s_6_1, vs_6_1);

        // c
        c = new HashSet<>();
        callsToStops = new HashMap<>();
        stopsToCloses = new HashMap<>();
        callsToCloses = new HashMap<>();
        dates = new HashMap<>();
        times = new HashMap<>();
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
            int i_2_1 = 0;
            int i_2_5 = 0;
            int i_2_6 = 0;
            int i = 0;
            for (var field : fields) {
                //System.out.println(field);
                //System.out.println(field + " " + (fieldLookup.size() - 1));
                if (field.contains(" ")) {
                    fieldLookup.put(field.split(" ")[0], field);
                } else {
                    fieldLookup.put(field, field);
                }
                // a
                if (field.startsWith(s_5_16)) {
                    i_5_16 = i;
                    //System.out.println(field + " " + i_5_16);
                }
                if (field.startsWith(s_5_16a)) {
                    i_5_16a = i;
                    //System.out.println(field + " " + i_5_16a);
                }
                if (field.startsWith(s_8_24)) {
                    i_8_24 = i;
                    //System.out.println(field + " " + i_8_24);
                }
                if (field.startsWith(s_8_25)) {
                    i_8_25 = i;
                    //System.out.println(field + " " + i_8_25);
                }
                if (field.startsWith(s_8_35)) {
                    i_8_35 = i;
                    //System.out.println(field + " " + i_8_35);
                }
                if (field.startsWith(s_8_35a)) {
                    i_8_35a = i;
                    //System.out.println(field + " " + i_8_35a);
                }
                // b
                if (field.startsWith(s_3_7)) {
                    i_3_7 = i;
                    //System.out.println(field + " " + i_3_7);
                }
                if (field.startsWith(s_6_1)) {
                    i_6_1 = i;
                    //System.out.println(field + " " + i_6_1);
                }
                // c
                if (field.startsWith(s_2_1)) {
                    i_2_1 = i;
                }
                if (field.startsWith(s_2_5)) {
                    i_2_5 = i;
                }
                if (field.startsWith(s_2_6)) {
                    i_2_6 = i;
                }
                i++;
            }
            line = r.readRow(hf);
            while (line != null) {
                boolean selected = false;
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
                    a.add(rowid);
                    selected = true;
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
                        a.add(rowid);
                        selected = true;
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
                    a.add(rowid);
                    selected = true;
                }
                // 8.25
                String v_8_25 = row.get(i_8_25);
                vs_8_25.add(v_8_25);
                if (v_8_25.equalsIgnoreCase(area)) {
                    //System.out.println(area);
                    count_8_25++;
                    //records.put(rowid, row);
                    records.put(rowid, line);
                    a.add(rowid);
                    selected = true;
                }
                // 8.35
                String v_8_35 = row.get(i_8_35);
                vs_8_35.add(v_8_35);
                if (v_8_35.equalsIgnoreCase(area)) {
                    //System.out.println(area);
                    count_8_35++;
                    //records.put(rowid, row);
                    records.put(rowid, line);
                    a.add(rowid);
                    selected = true;
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
                        a.add(rowid);
                        selected = true;
                    }
                }
                // b
                int resourceCommitted = 0;
                String v_3_7 = row.get(i_3_7);
                vs_3_7.add(v_3_7);
                if (!v_3_7.isBlank()) {
                    if (Integer.parseInt(v_3_7) > 0) {
                        resourceCommitted++;
                    }
                }
                String v_6_1 = row.get(i_6_1);
                vs_6_1.add(v_6_1);
                if (!v_6_1.isBlank()) {
                    resourceCommitted += Integer.parseInt(v_6_1);
                    if (resourceCommitted >= 4) {
                        count_resourceCommittedGreaterThan3++;
                        records.put(rowid, line);
                        b.add(rowid);
                        selected = true;
                    }
                }
                // c
                String v_2_1 = row.get(i_2_1);
                if (!v_2_1.isBlank()) {
                    String v_2_5 = row.get(i_2_5);
                    String v_2_6 = row.get(i_2_6);
                    ZonedDateTime start = getZonedDateTime(v_2_1);
                    ZonedDateTime stop = getZonedDateTime(v_2_5);
                    ZonedDateTime close = getZonedDateTime(v_2_6);
                    long duration = ChronoUnit.MINUTES.between(start, stop);
                    if (duration >= 360) {
                        count_durationGreaterThan6Hours++;
                        records.put(rowid, line);
                        c.add(rowid);
                        selected = true;
                    }
                    if (selected) {
                        String[] dateTime = v_2_1.split(" ");
                        callsToStops.put(rowid, duration);
                        stopsToCloses.put(rowid, ChronoUnit.MINUTES.between(stop, close));
                        callsToCloses.put(rowid, ChronoUnit.MINUTES.between(start, close));
                        dates.put(rowid, dateTime[0]);
                        times.put(rowid, dateTime[1]);
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
        System.out.println("" + count_durationGreaterThan6Hours + " records with duration >= 6 hours");

        System.out.println("In total there are " + records.size() + " records.");

        // Output records
        try {
            Path outdir = pOut.getParent();
            if (!Files.exists(outdir)) {
                Files.createDirectories(outdir);
            }
            try (BufferedWriter bw = Generic_IO.getBufferedWriter(pOut, false)) {
                Files.createDirectories(pOut.getParent());
                bw.write(header
                        + ",Time from call to stop (in minutes)"
                        + ",Time from stop to close (in minutes)"
                        + ",Time from call to close (in minutes)"
                        + ",date,time"
                        + ",a,b,c,a+b+c");
                bw.write("\n");

                for (var id : records.keySet()) {
                    //System.out.println(records.get(id));                    
                    bw.write(records.get(id));
                    // Times
                    bw.write("," + Long.toString(callsToStops.get(id)));
                    bw.write("," + Long.toString(stopsToCloses.get(id)));
                    bw.write("," + Long.toString(callsToCloses.get(id)));
                    bw.write(",\"" + dates.get(id) + "\"");
                    bw.write(",\"" + times.get(id) + "\"");
                    int abc = 0;
                    // a, b, c
                    if (a.contains(id)) {
                        bw.write(",1");
                        abc++;
                    } else {
                        bw.write(",0");
                    }
                    if (b.contains(id)) {
                        bw.write(",1");
                        abc++;
                    } else {
                        bw.write(",0");
                    }
                    if (c.contains(id)) {
                        bw.write(",1");
                        abc++;
                    } else {
                        bw.write(",0");
                    }
                    bw.write("," + Integer.toString(abc));
                    bw.write("\n");
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Run2.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Compile Date as a ZonedDateTime.
     *
     * @param s The date as a String to be turned into a ZonedDateTime
     * @return ZonedDateTime
     */
    public static ZonedDateTime getZonedDateTime(String s) {
        //System.out.print(s);
        String[] split = s.split(" ");
        String[] DDMMYYYY = split[0].split("/");
        int day = Integer.parseInt(DDMMYYYY[0]);
        int month = Integer.parseInt(DDMMYYYY[1]);
        int year = Integer.parseInt(DDMMYYYY[2]);
        String[] timeSplit = split[1].split(":");
        int hour = Integer.parseInt(timeSplit[0]);
        int minute = Integer.parseInt(timeSplit[1]);
        int second;
        if (timeSplit.length == 3) {
            second = Integer.parseInt(timeSplit[2]);
        } else {
            second = 0;
        }
        ZonedDateTime zdt = ZonedDateTime.of(year, month, day, hour, minute, second, 0, ZoneId.of("Europe/London"));
        return zdt;
    }
}
