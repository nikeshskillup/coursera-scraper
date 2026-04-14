package utils;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pages.AdminCoursesPage.CourseVersion;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;

public class ExcelExporter {
    private static final Logger logger = LoggerFactory.getLogger(ExcelExporter.class);
    
    public void writeToBatchExcel(List<CourseVersion> courses, String filePath) {
        long startTime = System.currentTimeMillis();
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Course Ratings");
            
            // Create header row
            createHeaderRow(sheet);
            
            // Set column widths (done once, not per row)
            sheet.setColumnWidth(0, 4000);
            sheet.setColumnWidth(1, 2000);
            sheet.setColumnWidth(2, 1500);
            sheet.setColumnWidth(3, 5000);
            sheet.setColumnWidth(4, 3000);
            
            // Create cell styles
            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.setDataFormat(workbook.createDataFormat()
                .getFormat("yyyy-mm-dd hh:mm:ss"));
            
            // Bulk write all data
            int rowNum = 1;
            for (CourseVersion course : courses) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(course.getCourseName());
                row.createCell(1).setCellValue(course.getVersion());
                
                try {
                    double rating = Double.parseDouble(course.getRating());
                    row.createCell(2).setCellValue(rating);
                } catch (NumberFormatException e) {
                    row.createCell(2).setCellValue(course.getRating());
                }
                
                row.createCell(3).setCellValue(course.getLink());
                
                // Add timestamp
                var dateCell = row.createCell(4);
                dateCell.setCellValue(new Date());
                dateCell.setCellStyle(dateStyle);
            }
            
            // Single write operation (critical for performance)
            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
                fileOut.flush();
            }
            
            long elapsed = System.currentTimeMillis() - startTime;
            logger.info("Excel file written: {} rows in {}ms to {}", 
                courses.size(), elapsed, filePath);
            
        } catch (IOException e) {
            logger.error("Failed to export to Excel: {}", e.getMessage());
            throw new RuntimeException("Excel export failed", e);
        }
    }
    
    private void createHeaderRow(Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Course Name");
        headerRow.createCell(1).setCellValue("Version");
        headerRow.createCell(2).setCellValue("Rating");
        headerRow.createCell(3).setCellValue("URL");
        headerRow.createCell(4).setCellValue("Scraped At");
        
        logger.debug("Header row created");
    }
}