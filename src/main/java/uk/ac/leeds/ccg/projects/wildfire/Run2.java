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
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.leeds.ccg.data.core.Data_Environment;
import uk.ac.leeds.ccg.data.format.Data_ReadCSV;
import uk.ac.leeds.ccg.generic.core.Generic_Environment;
import uk.ac.leeds.ccg.generic.io.Generic_Defaults;
import uk.ac.leeds.ccg.generic.io.Generic_IO;
import uk.ac.leeds.ccg.generic.util.Generic_Collections;
import uk.ac.leeds.ccg.io.IO_Utilities;

/**
 * The main file to run for the second set of provided data. These are two files
 * in a CSV format as exported somehow from the IRS database.
 *
 * @author Andy Turner
 */
public class Run2 {

    // Lookup from a short code to the full field name.
    HashMap<String, String> fieldLookup;

    HashMap<String, HashSet<String>> fieldValues;

    // For storing records against recordIDs.
    HashMap<Integer, String> records;

    // For storing the input file header.
    String header;

    // General variables for Part 1
    // ----------------------------
    // Selected fields
    ArrayList<String> sFields;

    // a
    // Identifies those records that meet a criteria.
    HashSet<Integer> a;
    // A class of area for which selections are made.
    String area = "\"Over 10,000";
    int areaHa = 1;

    // 5_16
    HashSet<String> vs_5_16;
    int count_5_16 = 0;
    String s_5_16 = "5.16 ";
    // 5_16a
    HashSet<String> vs_5_16a;
    int count_5_16a = 0;
    String s_5_16a = "5.16a ";
    // 8_24
    HashSet<String> vs_8_24;
    int count_8_24 = 0;
    String s_8_24 = "8.24 ";
    // 8_25
    HashSet<String> vs_8_25;
    int count_8_25 = 0;
    String s_8_25 = "8.25 ";
    // 8_35
    HashSet<String> vs_8_35;
    int count_8_35 = 0;
    String s_8_35 = "8.35 ";
    // 8_35a
    HashSet<String> vs_8_35a;
    int count_8_35a = 0;
    String s_8_35a = "8.35a ";

    // b
    // Identifies those records that meet b criteria.
    int resources = 4;
    HashSet<Integer> b;
    HashSet<String> vs_3_7;
    String s_3_7 = "3.7 ";
    HashSet<String> vs_6_1;
    String s_6_1 = "6.1 ";
    int count_resourceCommittedGE_resources = 0;

    // c
    // Identifies those records that meet c criteria.
    int minutes = 360;
    int hours = minutes / 60;
    HashSet<Integer> c;
    String s_2_1 = "2.1 ";
    String s_2_5 = "2.5 ";
    String s_2_6 = "2.6 ";
    HashMap<Integer, Long> callsToStops;
    HashMap<Integer, Long> stopsToCloses;
    HashMap<Integer, Long> callsToCloses;
    HashMap<Integer, String> dates;
    HashMap<Integer, String> times;
    int count_durationGE_hours = 0;

    // General variables for Part 2
    // ----------------------------
    HashSet<String> vs_3_2_Property_Type;
    TreeMap<String, Integer> counts_3_2_Property_Type;
    TreeMap<Month, TreeMap<Integer, Integer>> countsOutDoorFireByYearAndMonth;
    TreeMap<Integer, TreeMap<Integer, Integer>> countsOutDoorFireByYearAndWeek;
    TreeMap<Integer, TreeMap<Integer, Integer>> countsOutDoorFireByYearAndDay;
    String s_3_2_Property_Type = "3.2 Property Type";

    /**
     * Create a new instance.
     */
    public Run2() {
    }

    /**
     * Main method.
     *
     * @param args Ignored.
     */
    public static void main(String[] args) {
        new Run2().run();
    }

    /**
     * Main method run.
     */
    public void run() {
        try {
            Path pData = Paths.get("C:", "Users", "geoagdt", "work", "research",
                    "Wildfire", "data");
            Path pInput = Paths.get(pData.toString(), "input");
            Path pOutput = Paths.get(pData.toString(), "output");
            Path pIn = Paths.get(pInput.toString(), "Richard Hawley 2.csv");
            Path pIn2 = Paths.get(pInput.toString(), "Richard Hawley 3.csv");

            //DataFormatter df = new DataFormatter();
            Generic_Environment ge = new Generic_Environment(new Generic_Defaults(pData));
            Data_Environment de = new Data_Environment(ge);

            // General initialisation.
            initialise();

            // Part 1
            //part1(de, pIn, pIn2, pOutput);
            // Part 2
            part2(de, pIn, pIn2, pOutput);

        } catch (Exception ex) {
            Logger.getLogger(Run2.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // Part 2
    protected void part2(Data_Environment de, Path pIn, Path pIn2, Path pOutput) {
        initialisePart2();
        processPart2(de, pIn);
        processPart2(de, pIn2);

        //outputPart2(pOutput);
        System.out.println(s_3_2_Property_Type);
        System.out.println("Value, Count");
        for (var v : counts_3_2_Property_Type.keySet()) {
            System.out.println(v + ", " + counts_3_2_Property_Type.get(v));
        }

        // Monthly counts
        System.out.println("Count of outdoor fires by month:");
        String outputheader = "Month";
        boolean first = true;
        TreeMap<Integer, Integer> totalPerYear = new TreeMap<>();
        String row;
        for (Month month : countsOutDoorFireByYearAndMonth.keySet()) {
            TreeMap<Integer, Integer> countsOutDoorFireByMonth = countsOutDoorFireByYearAndMonth.get(month);
            row = month.name();
            for (Integer year : countsOutDoorFireByMonth.keySet()) {
                if (first) {
                    outputheader += "," + year;
                }
                int count = countsOutDoorFireByMonth.get(year);
                Generic_Collections.addToCount(totalPerYear, year, count);
                row += "," + count;
            }
            if (first) {
                System.out.println(outputheader);
                first = false;
            }
            System.out.println(row);
        }
        row = "Any";
        for (Integer year : totalPerYear.keySet()) {
            row += "," + totalPerYear.get(year);
        }
        System.out.println(row);
        
        // Week of year
        System.out.println("Count of outdoor fires by week of year:");
        outputheader = "WeekOfYear";
        first = true;
        totalPerYear = new TreeMap<>();
        for (Integer week : countsOutDoorFireByYearAndWeek.keySet()) {
            TreeMap<Integer, Integer> countsOutDoorFireByWeek = countsOutDoorFireByYearAndWeek.get(week);
            row = week.toString();
            for (Integer year : countsOutDoorFireByWeek.keySet()) {
                if (first) {
                    outputheader += "," + year;
                }
                int count = countsOutDoorFireByWeek.get(year);
                Generic_Collections.addToCount(totalPerYear, year, count);
                row += "," + count;
            }
            if (first) {
                System.out.println(outputheader);
                first = false;
            }
            System.out.println(row);
        }
        row = "Any";
        for (Integer year : totalPerYear.keySet()) {
            row += "," + totalPerYear.get(year);
        }
        System.out.println(row);
        
        // Day of Year
        System.out.println("Count of outdoor fires by day of year:");
        outputheader = "DayOfYear";
        first = true;
        totalPerYear = new TreeMap<>();
        for (Integer day : countsOutDoorFireByYearAndDay.keySet()) {
            TreeMap<Integer, Integer> countsOutDoorFireByDay = countsOutDoorFireByYearAndDay.get(day);
            row = day.toString();
            for (Integer year : countsOutDoorFireByDay.keySet()) {
                if (first) {
                    outputheader += "," + year;
                }
                int count = countsOutDoorFireByDay.get(year);
                Generic_Collections.addToCount(totalPerYear, year, count);
                row += "," + count;
            }
            if (first) {
                System.out.println(outputheader);
                first = false;
            }
            System.out.println(row);
        }
        row = "Any";
        for (Integer year : totalPerYear.keySet()) {
            row += "," + totalPerYear.get(year);
        }
        System.out.println(row);
        
    }

    /**
     * Initialise Part 2 variables.
     */
    protected void initialisePart2() {
        // 3_2
        vs_3_2_Property_Type = new HashSet<>();
        counts_3_2_Property_Type = new TreeMap<>();
        countsOutDoorFireByYearAndMonth = new TreeMap<>();
        countsOutDoorFireByYearAndWeek = new TreeMap<>();
        for (int week = 1; week <= 53; week ++) {
            TreeMap<Integer, Integer> countsOutDoorFireByWeek = new TreeMap<>();
            countsOutDoorFireByYearAndWeek.put(week, countsOutDoorFireByWeek);
            for (int year = 2010; year < 2024; year ++) {
                countsOutDoorFireByWeek.put(year, 0);
            }
        }
        countsOutDoorFireByYearAndDay = new TreeMap<>();
        for (int day = 1; day < 367; day ++) {
            TreeMap<Integer, Integer> countsOutDoorFireByDay = new TreeMap<>();
            countsOutDoorFireByYearAndDay.put(day, countsOutDoorFireByDay);
            for (int year = 2010; year < 2024; year ++) {
                countsOutDoorFireByDay.put(year, 0);
            }
        }
    }

    /**
     * Process Part 2.
     *
     * @param de The data environment.
     * @param pIn The input file path.
     */
    protected void processPart2(Data_Environment de, Path pIn) {
        try {
            String outdoorType = "Property/Outdoor/Grassland, woodland and crops/";
            BufferedReader br = IO_Utilities.getBufferedReader(pIn);
            Data_ReadCSV r = new Data_ReadCSV(de);
            r.setStreamTokenizer(br, 9);
            String line = r.readLine();
            header = line;
            int hf = r.countFields(line);
            System.out.println("Number of fields " + hf);
            ArrayList<String> fields = r.parseLine(header);
            // Initialise field indexes.
            int i_2_1 = 0;
            int i_3_2 = 0;
            int i = 0;
            for (var field : fields) {
                //System.out.println(field);
                //System.out.println(field + " " + (fieldLookup.size() - 1));
                if (field.contains(" ")) {
                    fieldLookup.put(field.split(" ")[0], field);
                } else {
                    fieldLookup.put(field, field);
                }
                if (field.startsWith(s_2_1)) {
                    i_2_1 = i;
                }
                // a
                if (field.equals(s_3_2_Property_Type)) {
                    i_3_2 = i;
                }
                i++;
            }
            line = r.readRow(hf);
            while (line != null) {
                ArrayList<String> row = r.parseLine(line);
                // 2.1
                String v_2_1 = row.get(i_2_1);
                if (!v_2_1.isBlank()) {
                    ZonedDateTime start = getZonedDateTime(v_2_1);
                    TreeMap<Integer, Integer> countsOutDoorFireByMonth;
                    Integer year = start.getYear();
                    Month month = start.getMonth();
                    if (countsOutDoorFireByYearAndMonth.containsKey(month)) {
                        countsOutDoorFireByMonth = countsOutDoorFireByYearAndMonth.get(month);
                    } else {
                        countsOutDoorFireByMonth = new TreeMap<>();
                        countsOutDoorFireByYearAndMonth.put(month, countsOutDoorFireByMonth);
                    }
                    TreeMap<Integer, Integer> countsOutDoorFireByWeek;
                    Integer day = start.getDayOfYear();
                    Integer week = (day + 7) / 7;
                    if (countsOutDoorFireByYearAndWeek.containsKey(week)) {
                        countsOutDoorFireByWeek = countsOutDoorFireByYearAndWeek.get(week);
                    } else {
                        countsOutDoorFireByWeek = new TreeMap<>();
                        countsOutDoorFireByYearAndWeek.put(week, countsOutDoorFireByWeek);
                    }
                    TreeMap<Integer, Integer> countsOutDoorFireByDay;
                    if (countsOutDoorFireByYearAndDay.containsKey(day)) {
                        countsOutDoorFireByDay = countsOutDoorFireByYearAndDay.get(day);
                    } else {
                        countsOutDoorFireByDay = new TreeMap<>();
                        countsOutDoorFireByYearAndDay.put(day, countsOutDoorFireByDay);
                    }
                    
                    // 3.2
                    String v_3_2 = row.get(i_3_2);
                    v_3_2 = v_3_2.replaceAll("\"", "");
                    vs_3_2_Property_Type.add(v_3_2);
                    Generic_Collections.addToCount(counts_3_2_Property_Type, v_3_2, 1);
                    if (v_3_2.startsWith(outdoorType)) {
                        Generic_Collections.addToCount(countsOutDoorFireByMonth, year, 1);
                        Generic_Collections.addToCount(countsOutDoorFireByWeek, year, 1);
                        Generic_Collections.addToCount(countsOutDoorFireByDay, year, 1);
                    }
                }
                line = r.readRow(hf);
            }
        } catch (IOException ex) {
            Logger.getLogger(Run2.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Part 1.
     *
     * @param de The data environment.
     * @param pIn The first input file path.
     * @param pIn2 The second input file path.
     * @param pOutput The output file path.
     */
    protected void part1(Data_Environment de, Path pIn, Path pIn2, Path pOutput) {
        Path pOut = Paths.get(pOutput.toString(), "abc.csv");
        initialisePart1();
        int id = 0;
        int id1 = processPart1(de, pIn, id);
        System.out.println("");
        System.out.println("" + id1 + " records in \"" + pIn.getFileName().toString() + "\"");

        int id2 = processPart1(de, pIn2, id1);
        System.out.println("");
        System.out.println("" + (id2 - id1) + " records in \"" + pIn2.getFileName().toString() + "\"");

        outputPart1(pOut);
    }

    /**
     * Initialise sFields and field values
     */
    protected void initialise() {
        fieldLookup = new HashMap<>();
        fieldValues = new HashMap<>();
        records = new HashMap<>();
    }

    /**
     * Initialise sFields and field values
     */
    protected void initialisePart1() {
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

    /**
     * Process Part 2.
     *
     * @param de The data environment.
     * @param pIn The input file path.
     * @param id The row id for the first record read and processed.
     * @return The row id for the next record to be processed.
     */
    public int processPart1(Data_Environment de, Path pIn, int id) {
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
                if (!line.replaceAll(",", "").trim().isBlank()) {
                    boolean selected = false;
                    ArrayList<String> row = r.parseLine(line);

//                    if (Long.parseLong(row.get(0)) == 2047020822L) {
//                        int debug = 1;
//                    }
                    // a
                    // 5.16
                    String v_5_16 = row.get(i_5_16);
                    vs_5_16.add(v_5_16);
                    if (v_5_16.equalsIgnoreCase(area)) {
                        //System.out.println(area);
                        count_5_16++;
                        //records.put(id, row);
                        records.put(id, line);
                        a.add(id);
                        selected = true;
                    }
                    // 5.16a
                    String v_5_16a = row.get(i_5_16a);
                    vs_5_16a.add(v_5_16a);
                    if (!v_5_16a.isBlank()) {
                        if (Integer.parseInt(v_5_16a) >= 1) {
                            //System.out.println(v_5_16a);
                            count_5_16a++;
                            //records.put(rowid, row);
                            records.put(id, line);
                            a.add(id);
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
                        records.put(id, line);
                        a.add(id);
                        selected = true;
                    }
                    // 8.25
                    String v_8_25 = row.get(i_8_25);
                    vs_8_25.add(v_8_25);
                    if (v_8_25.equalsIgnoreCase(area)) {
                        //System.out.println(area);
                        count_8_25++;
                        //records.put(rowid, row);
                        records.put(id, line);
                        a.add(id);
                        selected = true;
                    }
                    // 8.35
                    String v_8_35 = row.get(i_8_35);
                    vs_8_35.add(v_8_35);
                    if (v_8_35.equalsIgnoreCase(area)) {
                        //System.out.println(area);
                        count_8_35++;
                        //records.put(rowid, row);
                        records.put(id, line);
                        a.add(id);
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
                            records.put(id, line);
                            a.add(id);
                            selected = true;
                        }
                    }
                    // b
                    int resourceCommitted = 0;
                    String v_6_1 = row.get(i_6_1);
                    vs_6_1.add(v_6_1);
                    if (!v_6_1.isBlank()) {
                        if (Integer.parseInt(v_6_1) > 0) {
                            resourceCommitted++;
                        }
                    }
                    String v_3_7 = row.get(i_3_7);
                    vs_3_7.add(v_3_7);
                    if (!v_3_7.isBlank()) {
                        resourceCommitted += Integer.parseInt(v_3_7);
                        if (resourceCommitted >= resources) {
                            count_resourceCommittedGE_resources++;
                            records.put(id, line);
                            b.add(id);
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
                        if (duration >= minutes) {
                            count_durationGE_hours++;
                            records.put(id, line);
                            c.add(id);
                            selected = true;
                        }
                        if (selected) {
                            String[] dateTime = v_2_1.split(" ");
                            callsToStops.put(id, duration);
                            stopsToCloses.put(id, ChronoUnit.MINUTES.between(stop, close));
                            callsToCloses.put(id, ChronoUnit.MINUTES.between(start, close));
                            dates.put(id, dateTime[0]);
                            times.put(id, dateTime[1]);
                        }
                    }
                    id++;
                }
                line = r.readRow(hf);
            }
        } catch (IOException ex) {
            Logger.getLogger(Run2.class.getName()).log(Level.SEVERE, null, ex);
        }
        return id;
    }

    /**
     * For generating Part 1 outputs.
     *
     * @param pOut The path of the file to write to.
     */
    public void outputPart1(Path pOut) {
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
        System.out.println("" + count_5_16a + " records with \"" + fieldLookup.get(s_5_16a.trim()) + "\" >= " + areaHa);
        System.out.println("" + count_8_24 + " records with \"" + fieldLookup.get(s_8_24.trim()) + "\" = " + area + "\"");
        System.out.println("" + count_8_25 + " records with \"" + fieldLookup.get(s_8_25.trim()) + "\" = " + area + "\"");
        System.out.println("" + count_8_35 + " records with \"" + fieldLookup.get(s_8_35.trim()) + "\" = " + area + "\"");
        System.out.println("" + count_8_35a + " records with \"" + fieldLookup.get(s_8_35a.trim()) + "\" >= " + areaHa);
        System.out.println("" + count_resourceCommittedGE_resources + " records with resources committed >= " + resources);
        System.out.println("" + count_durationGE_hours + " records with duration >= " + hours + " hours");

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
