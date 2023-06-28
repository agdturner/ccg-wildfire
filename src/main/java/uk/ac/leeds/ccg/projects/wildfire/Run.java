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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import uk.ac.leeds.ccg.generic.core.Generic_Environment;
import uk.ac.leeds.ccg.generic.io.Generic_Defaults;
import uk.ac.leeds.ccg.generic.util.Generic_Time;

/**
 * The main file to run.
 *
 * @author Andy Turner
 */
public class Run {

    public Run() {
    }

    public static void main(String[] args) {

        Path pData = Paths.get("C:", "Users", "agdtu", "work", "research", "Wildfire", "data");
        //Path pIn = Paths.get(pData.toString(), "input", "WRS_Export_All_20230306_Excel", "GENERIC_1", "WRS_Export__.xlsx");
        Path pIn = Paths.get(pData.toString(), "input", "WYFRS Potential Wildfire IRS data.xlsx");
        Path pOut = Paths.get(pData.toString(), "output", "WYFRS Potential Wildfire IRS data.xlsx");
        try {
            Files.createDirectories(pOut.getParent());
        } catch (IOException ex) {
            Logger.getLogger(Run.class.getName()).log(Level.SEVERE, null, ex);
        }

        DataFormatter df = new DataFormatter();

        try {
            Generic_Environment ge = new Generic_Environment(new Generic_Defaults(pData));
            XSSFWorkbook workbook;
            try (FileInputStream file = new FileInputStream(pIn.toFile())) {
                // Create Workbook instance holding reference to .xlsx file.
                workbook = new XSSFWorkbook(file);
                FormulaEvaluator fe = new XSSFFormulaEvaluator(workbook);
                // Get first/desired sheet from the workbook.
                XSSFSheet sheet = workbook.getSheetAt(0);

                // Iterate through each rows one by one.
                int rn = 0;
                for (Row row : sheet) {
                    //System.out.println(rn + " out of " + sheet.getLastRowNum());
                    if (rn < 11298) {
                        // Add three columns
                        int dowc = row.getLastCellNum(); // Day of week
                        row.createCell(dowc);
                        int moyc = row.getLastCellNum(); // Month of year
                        row.createCell(moyc);
                        int yc = row.getLastCellNum(); // Year
                        row.createCell(yc);
                        int d1c = row.getLastCellNum();  // Time difference start to stop
                        row.createCell(d1c);
                        int d2c = row.getLastCellNum();  // Time difference stop to close
                        row.createCell(d2c);
                        // For each row, iterate through all the columns.
                        Iterator<Cell> cellIterator = row.cellIterator();
                        // Print everything to stdout.
                        if (rn == 0) {
                            row.getCell(dowc).setCellValue("Day of week fire start");
                            row.getCell(moyc).setCellValue("Month of year fire start");
                            row.getCell(yc).setCellValue("Year fire start");
                            row.getCell(d1c).setCellValue("Seconds from call to stop");
                            row.getCell(d2c).setCellValue("Seconds from stop to close");
                            while (cellIterator.hasNext()) {
                                Cell cell = cellIterator.next();
                                CellType ct = cell.getCellType();
                                if (ct.equals(CellType.NUMERIC)) {
                                    System.out.print(cell.getNumericCellValue() + " ");
                                } else {
                                    System.out.print(cell.getStringCellValue() + " ");
                                }
                            }
                            System.out.println("");
                        } else {
                            long diff1 = 0;
                            long diff2 = 0;
                            String dow = "";
                            String moy = "";
                            int year = 0;
                            Cell c8 = row.getCell(8);
                            if (c8 != null) {
                                Date d0 = c8.getDateCellValue();
                                ZonedDateTime zdt0 = getZonedDateTime(d0);
                                dow = zdt0.getDayOfWeek().toString();
                                moy = zdt0.getMonth().toString();
                                year = zdt0.getYear();
                                Cell c14 = row.getCell(14);
                                if (c14 != null) {
                                    Date d1 = c14.getDateCellValue();
                                    ZonedDateTime zdt1 = getZonedDateTime(d1);
                                    diff1 = ChronoUnit.SECONDS.between(zdt0, zdt1);
                                    //System.out.println(zdt0.toString() + " " + zdt1.toString() + " " + diff1);
                                    Cell c15 = row.getCell(15);
                                    if (c15 != null) {
                                        Date d2 = c15.getDateCellValue();
                                        ZonedDateTime zdt2 = getZonedDateTime(d2);
                                        diff2 = ChronoUnit.SECONDS.between(zdt1, zdt2);
                                        //System.out.println(zdt2.toString() + " " + diff2);
                                    } else {
                                        diff2 = -1;
                                    }
                                } else {
                                    diff1 = -1;
                                }
                            } else {
                                System.out.println("No fire start date!");
                            }
                            row.getCell(dowc).setCellValue(dow);
                            row.getCell(moyc).setCellValue(moy);
                            row.getCell(yc).setCellValue(year);
                            row.getCell(d1c).setCellValue(diff1);
                            row.getCell(d2c).setCellValue(diff2);
//                        String cellValueStr = df.formatCellValue();
//
//                        //CellValue c8e = fe.evaluate(c8); // This will evaluate the cell, And any type of cell will return string value
//                        //String cellValueStr = df.formatCellValue(c8e, fe);
//                        //String cellValueStr = df.formatCellValue(c8e);
//                        //System.out.print(row.getCell(7).getStringCellValue() + " ");
//                        System.out.print(cellValueStr + " ");
//                        String[] split = cellValueStr.split(" ");
//                        String[] dateSplit = split[0].split("/");
//                        String[] timeSplit = split[1].split(":");
//                        int day = Integer.parseInt(dateSplit[0]);
//                        int month = Integer.parseInt(dateSplit[1]);
//                        int year = Integer.parseInt(dateSplit[2]) + 2000;
//                        int hour = Integer.parseInt(timeSplit[0]);
//                        int minute = Integer.parseInt(timeSplit[1]);
//                        if ()
//                        int seconds = Integer.parseInt(timeSplit[2]);
//                        Generic_Time t = new Generic_Time(ge, year, month, day, hour, minute, seconds);
//                        System.out.print(t.getYYYYMMDDHHMMSS("/", " ", ":", "") + " ");
//                        //System.out.println(row.getCell(9).toString() + " ");
//                        //System.out.print(row.getCell(8).getNumericCellValue() + " ");
                        }
                    }
                    rn++;
                }
            }
            try {
                //Write the workbook in file system
                FileOutputStream out = new FileOutputStream(pOut.toFile());
                workbook.write(out);
                out.close();
                System.out.println("xlsx written successfully on disk.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }

    }

    /**
     * Compile Date as a ZonedDateTime.
     * 
     * @param d The date to be turned into a ZonedDateTime
     * @return ZonedDateTime
     */
    public static ZonedDateTime getZonedDateTime(Date d) {
        String s = d.toString();
        //System.out.print(s);
        String[] split = s.split(" ");
        int month = Generic_Time.getMonthInt(split[1]);
        int day = Integer.parseInt(split[2]);
        String[] timeSplit = split[3].split(":");
        int hour = Integer.parseInt(timeSplit[0]);
        int minute = Integer.parseInt(timeSplit[1]);
        int second = Integer.parseInt(timeSplit[2]);
        //String zoneID = split[4];
        int year = Integer.parseInt(split[5]);
        ZonedDateTime zdt = ZonedDateTime.of(year, month, day, hour, minute, second, 0, ZoneId.of("Europe/London"));
        return zdt;
    }
}
