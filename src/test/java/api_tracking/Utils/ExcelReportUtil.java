package api_tracking.Utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ExcelReportUtil {

    private static Map<String, String> buildInfo = new HashMap<>();

    // Set build info from test class
    public static void setBuildInfo(Map<String, String> info) {
        buildInfo = info;
    }

    /**
     * Generate an Excel report:
     * 1. BUILD_INFO
     * 2. SUMMARY
     * 3. Section Sheets
     */
    public static void generateReport(String outputPath, Map<String, List<Map<String, Object>>> sectionWiseData) throws IOException {

        try (Workbook workbook = new XSSFWorkbook()) {

            // COMMON STYLES
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle greenStyle = createRowStyle(workbook, IndexedColors.LIGHT_GREEN);
            CellStyle redStyle = createRowStyle(workbook, IndexedColors.ROSE);
            DataFormat df = workbook.createDataFormat();

            // BUILD_INFO SHEET
            Sheet buildSheet = workbook.createSheet("BUILD_INFO");
            int br = 0;

            // Title Row
            Row bt = buildSheet.createRow(br++);
            Cell btCell = bt.createCell(0);
            btCell.setCellValue("BUILD INFORMATION");

            // Title Style
            CellStyle buildTitleStyle = workbook.createCellStyle();
            Font buildTitleFont = workbook.createFont();
            buildTitleFont.setBold(true);
            buildTitleFont.setFontHeightInPoints((short) 14);
            buildTitleStyle.setFont(buildTitleFont);
            buildTitleStyle.setAlignment(HorizontalAlignment.CENTER);
            buildTitleStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
            buildTitleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            btCell.setCellStyle(buildTitleStyle);

            buildSheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 1));

            // Column Headers
            Row headerRow = buildSheet.createRow(br++);
            Cell fieldHeader = headerRow.createCell(0);
            fieldHeader.setCellValue("Property");
            Cell valueHeader = headerRow.createCell(1);
            valueHeader.setCellValue("Details");

            CellStyle buildHeaderStyle = workbook.createCellStyle();
            Font buildHeaderFont = workbook.createFont();
            buildHeaderFont.setBold(true);
            buildHeaderStyle.setFont(buildHeaderFont);
            buildHeaderStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            buildHeaderStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            buildHeaderStyle.setAlignment(HorizontalAlignment.CENTER);

            fieldHeader.setCellStyle(buildHeaderStyle);
            valueHeader.setCellStyle(buildHeaderStyle);

            // Build Info rows
            for (Map.Entry<String, String> entry : buildInfo.entrySet()) {
                Row row = buildSheet.createRow(br++);
                row.createCell(0).setCellValue(entry.getKey());
                row.createCell(1).setCellValue(entry.getValue());
            }

            buildSheet.autoSizeColumn(0);
            buildSheet.autoSizeColumn(1);

            // ---------- SUMMARY SHEET ----------
            List<Map<String, Object>> allEntries = new ArrayList<>();
            for (List<Map<String, Object>> list : sectionWiseData.values()) {
                allEntries.addAll(list);
            }

            int totalApis = allEntries.size();
            long passedCount = allEntries.stream()
                    .filter(e -> e.get("Status") != null && e.get("Status").toString().equals("200"))
                    .count();
            long failedCount = totalApis - passedCount;

            List<Long> responseTimes = allEntries.stream()
                    .map(e -> e.get("ResponseTimeMs"))
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .map(s -> {
                        try { return Long.parseLong(s); }
                        catch (Exception ignored) { return null; }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            double avgResponse = responseTimes.isEmpty() ? 0 :
                    responseTimes.stream().mapToLong(Long::longValue).average().orElse(0);

            Optional<Map<String, Object>> fastest = allEntries.stream()
                    .filter(e -> e.get("ResponseTimeMs") != null)
                    .min(Comparator.comparingLong(e -> Long.parseLong(e.get("ResponseTimeMs").toString())));

            Optional<Map<String, Object>> slowest = allEntries.stream()
                    .filter(e -> e.get("ResponseTimeMs") != null)
                    .max(Comparator.comparingLong(e -> Long.parseLong(e.get("ResponseTimeMs").toString())));

            List<Map<String, Object>> top10Slowest = allEntries.stream()
                    .filter(e -> e.get("ResponseTimeMs") != null)
                    .sorted((a, b) -> Long.compare(
                            Long.parseLong(b.get("ResponseTimeMs").toString()),
                            Long.parseLong(a.get("ResponseTimeMs").toString())
                    ))
                    .limit(10)
                    .collect(Collectors.toList());

            Sheet summary = workbook.createSheet("SUMMARY");
            int r = 0;

            // SUMMARY TITLE
            Row titleRow = summary.createRow(r++);
            Cell title = titleRow.createCell(0);
            title.setCellValue("API EXECUTION SUMMARY");

            CellStyle summaryTitleStyle = workbook.createCellStyle();
            Font summaryTitleFont = workbook.createFont();
            summaryTitleFont.setBold(true);
            summaryTitleFont.setFontHeightInPoints((short) 14);
            summaryTitleStyle.setFont(summaryTitleFont);
            summaryTitleStyle.setAlignment(HorizontalAlignment.CENTER);
            summaryTitleStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
            summaryTitleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            title.setCellStyle(summaryTitleStyle);

            summary.addMergedRegion(new CellRangeAddress(0, 0, 0, 1));
            r++;

            // Metrics table
            String[][] metrics = new String[][]{
                    {"Total APIs", String.valueOf(totalApis)},
                    {"Passed (200)", String.valueOf(passedCount)},
                    {"Failed (Non-200)", String.valueOf(failedCount)},
                    {"Pass %", String.format("%.2f%%",
                            totalApis == 0 ? 0.0 : (passedCount * 100.0 / totalApis))},
                    {"Avg Response Time (ms)", String.format("%.2f", avgResponse)},
                    {"Fastest API", fastest.map(e -> e.get("URL") + " (" + e.get("ResponseTimeMs") + " ms)").orElse("N/A")},
                    {"Slowest API", slowest.map(e -> e.get("URL") + " (" + e.get("ResponseTimeMs") + " ms)").orElse("N/A")}
            };

            for (String[] m : metrics) {
                Row row = summary.createRow(r++);
                row.createCell(0).setCellValue(m[0]);
                row.createCell(1).setCellValue(m[1]);
            }
            r++;

            // Top 10 slowest APIs
            // Create row
            Row slowHeader = summary.createRow(r++);

            // Merge between 0 and 1 columns
            summary.addMergedRegion(new CellRangeAddress(
                    slowHeader.getRowNum(), 
                    slowHeader.getRowNum(), 
                    0, 
                    1
            ));

            // Create cell only once
            Cell sh1 = slowHeader.createCell(0);
            sh1.setCellValue("Top 10 Slowest APIs");

            // Reuse existing headerStyle but apply yellow fill
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            // Apply style
            sh1.setCellStyle(headerStyle);

            // Also create 2nd merged cell for correct style
            Cell sh2 = slowHeader.createCell(1);
            sh2.setCellStyle(headerStyle);


            Row slowCols = summary.createRow(r++);
            slowCols.createCell(0).setCellValue("API URL");
            slowCols.createCell(1).setCellValue("Response Time (ms)");
            slowCols.getCell(0).setCellStyle(headerStyle);
            slowCols.getCell(1).setCellStyle(headerStyle);

            for (Map<String, Object> e : top10Slowest) {
                Row row = summary.createRow(r++);
                row.createCell(0).setCellValue(String.valueOf(e.get("URL")));
                row.createCell(1).setCellValue(Long.parseLong(e.get("ResponseTimeMs").toString()));
            }

            summary.autoSizeColumn(0);
            summary.autoSizeColumn(1);

            // SECTION SHEETS
            String[] headers = new String[]{"API URL", "Method", "Status Code", "Response Time (ms)"};

            for (Map.Entry<String, List<Map<String, Object>>> sectionEntry : sectionWiseData.entrySet()) {
                String sectionName = sanitizeSheetName(sectionEntry.getKey());
                Sheet sheet = workbook.createSheet(sectionName);

                Row headerRow2 = sheet.createRow(0);
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = headerRow2.createCell(i);
                    cell.setCellValue(headers[i]);
                    cell.setCellStyle(headerStyle);
                }

                int rowIndex = 1;
                for (Map<String, Object> rowMap : sectionEntry.getValue()) {
                    Row row = sheet.createRow(rowIndex++);

                    String url = Objects.toString(rowMap.getOrDefault("URL", ""), "");
                    String method = Objects.toString(rowMap.getOrDefault("Method", ""), "");
                    String status = Objects.toString(rowMap.getOrDefault("Status", ""), "");
                    String respTime = Objects.toString(rowMap.getOrDefault("ResponseTimeMs", ""), "");

                    row.createCell(0).setCellValue(url);
                    row.createCell(1).setCellValue(method);

                    try { row.createCell(2).setCellValue(Integer.parseInt(status)); }
                    catch (Exception ex) { row.createCell(2).setCellValue(status); }

                    try { row.createCell(3).setCellValue(Long.parseLong(respTime)); }
                    catch (Exception ex) { row.createCell(3).setCellValue(respTime); }

                    // row coloring
                    CellStyle rowStyle =
                            status.equals("200") ? greenStyle : redStyle;

                    for (int i = 0; i < headers.length; i++) {
                        Cell cc = row.getCell(i);
                        if (cc == null) cc = row.createCell(i);
                        CellStyle combined = workbook.createCellStyle();
                        combined.cloneStyleFrom(rowStyle);
                        if (i == 3) combined.setDataFormat(df.getFormat("0"));
                        cc.setCellStyle(combined);
                    }
                }

                if (rowIndex > 1) {
                    sheet.setAutoFilter(new CellRangeAddress(0, rowIndex - 1, 0, headers.length - 1));
                }

                for (int i = 0; i < headers.length; i++) {
                    sheet.autoSizeColumn(i);
                    sheet.setColumnWidth(i, Math.min(sheet.getColumnWidth(i) + 1500, 10000));
                }
            }

            // WRITE FILE
            try (FileOutputStream fos = new FileOutputStream(outputPath)) {
                workbook.write(fos);
            }
        }
    }

    private static CellStyle createHeaderStyle(Workbook wb) {
        CellStyle header = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        header.setFont(font);
        header.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        header.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        header.setAlignment(HorizontalAlignment.CENTER);
        return header;
    }

    private static CellStyle createRowStyle(Workbook wb, IndexedColors bgColor) {
        CellStyle style = wb.createCellStyle();
        style.setFillForegroundColor(bgColor.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private static String sanitizeSheetName(String raw) {
        if (raw == null || raw.trim().isEmpty()) return "SECTION";
        String s = raw.trim();
        if (s.length() > 31) s = s.substring(0, 31);
        return s.replaceAll("[:\\\\/?*\\[\\]]", "_");
    }
    
 // Path Generator for One Drive shared Folder
    public static String generateOneDriveReportPath() {

        // 1. BASE/Root Folder
        String basePath = "C:\\Users\\prateek.hullatti\\OneDrive - SLK Software Pvt Ltd\\API Automation Report";

        // 2. Create date folder (Sub Folder)
        String today = new java.text.SimpleDateFormat("dd-MM-yyyy").format(new Date());
        String dateFolderPath = basePath + "/" + today;

        java.io.File dateFolder = new java.io.File(dateFolderPath);
        if (!dateFolder.exists()) {
            dateFolder.mkdirs(); // Creates folder if not exists
        }

        // 3. Create time stamped folder (Sub-sub folder) Location of excel generated
        String timestamp = new java.text.SimpleDateFormat("HH-mm-ss").format(new Date());
        String fileName = "API_Report_" + timestamp + ".xlsx";

        // 4. Return full file path
        return dateFolderPath + "/" + fileName;
    }
}
