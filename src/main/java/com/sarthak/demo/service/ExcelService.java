
package com.sarthak.demo.service;

import com.sarthak.demo.model.SalesRecord;
import com.sarthak.demo.repository.SalesRecordRepository;

import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
public class ExcelService {

    @Autowired
    private SalesRecordRepository salesRecordRepository;

    public int parseExcel(InputStream inputStream) {
        List<SalesRecord> newRecords = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            if (workbook.getNumberOfSheets() == 0) {
                throw new IllegalArgumentException("Excel file has no sheets.");
            }

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet.getPhysicalNumberOfRows() == 0) {
                throw new IllegalArgumentException("Excel sheet is empty.");
            }

            DataFormatter formatter = new DataFormatter();
            FormulaEvaluator evaluator =
                    workbook.getCreationHelper().createFormulaEvaluator();

            Row headerRow = sheet.getRow(sheet.getFirstRowNum());
            Map<String, Integer> columns = new HashMap<>();

            if (headerRow == null) {
                throw new IllegalArgumentException("Missing header row.");
            }

            for (Cell cell : headerRow) {
                String header = normalize(
                        formatter.formatCellValue(cell, evaluator));
                columns.put(header, cell.getColumnIndex());
            }

            int productCol = findColumn(columns, "product", "productname", "item");
            int quantityCol = findColumn(columns, "quantity", "qty", "unitssold");
            int priceCol = findColumn(columns, "unitprice", "price", "sellingprice");
            int dateCol = findColumn(columns, "date", "salesdate", "orderdate");
            int regionCol = findColumn(columns, "region", "location", "salesregion");
            int costCol = findColumn(columns, "unitcost", "cost", "costprice");

            if (productCol < 0 || quantityCol < 0 || priceCol < 0) {
                throw new IllegalArgumentException(
                    "Required columns missing. Include Product, Quantity, " +
                    "and UnitPrice in the first row.");
            }

            for (int i = headerRow.getRowNum() + 1;
                    i <= sheet.getLastRowNum(); i++) {

                Row row = sheet.getRow(i);
                if (row == null) continue;

                String product = cellText(row, productCol, formatter, evaluator);
                if (product.isBlank()) continue;

                double quantity = cellNumber(
                        row, quantityCol, formatter, evaluator);
                double unitPrice = cellNumber(
                        row, priceCol, formatter, evaluator);
                double unitCost = costCol < 0 ? 0 :
                        cellNumber(row, costCol, formatter, evaluator);

                if (quantity <= 0 || unitPrice < 0 || unitCost < 0) {
                    continue;
                }

                String date = dateCol < 0 ? "" :
                        cellDate(row.getCell(dateCol), formatter, evaluator);

                String region = regionCol < 0 ? "Unspecified" :
                        cellText(row, regionCol, formatter, evaluator);

                if (region.isBlank()) region = "Unspecified";

                double revenue = quantity * unitPrice;
                double profit = revenue - (quantity * unitCost);

                newRecords.add(new SalesRecord(
                        date, product, quantity, unitPrice,
                        unitCost, region, revenue, profit));
            }

            if (newRecords.isEmpty()) {
                throw new IllegalArgumentException(
                    "No valid sales rows found. Check your spreadsheet data.");
            }

            // Append records. Existing records are never deleted.
            salesRecordRepository.saveAll(newRecords);
            return newRecords.size();

        } catch (Exception e) {
            throw new RuntimeException(
                    "Could not import sales Excel file: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> getAnalytics() {
        List<SalesRecord> records = salesRecordRepository.findAll();

        double totalRevenue = 0;
        double totalProfit = 0;
        double totalCost = 0;
        double totalUnits = 0;

        // Sorted by month for the line chart.
        Map<String, double[]> monthly = new TreeMap<>();

        // Aggregate revenue by product.
        Map<String, Double> products = new HashMap<>();

        // Aggregate revenue and profit by region.
        Map<String, double[]> regions = new TreeMap<>();

        for (SalesRecord record : records) {
            double revenue = record.getRevenue();
            double profit = record.getProfit();
            double cost = revenue - profit;

            totalRevenue += revenue;
            totalProfit += profit;
            totalCost += cost;
            totalUnits += record.getQuantity();

            String month = "Undated";
            if (record.getDate() != null && !record.getDate().isBlank()) {
                try {
                    month = YearMonth.from(
                            LocalDate.parse(record.getDate())).toString();
                } catch (Exception ignored) {
                    month = "Undated";
                }
            }

            double[] monthValues = monthly.computeIfAbsent(
                    month, key -> new double[2]);
            monthValues[0] += revenue;
            monthValues[1] += profit;

            products.merge(record.getProduct(), revenue, Double::sum);

            String region = record.getRegion() == null ||
                    record.getRegion().isBlank()
                    ? "Unspecified" : record.getRegion();

            double[] regionValues = regions.computeIfAbsent(
                    region, key -> new double[2]);
            regionValues[0] += revenue;
            regionValues[1] += profit;
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalRevenue", totalRevenue);
        summary.put("totalProfit", totalProfit);
        summary.put("totalCost", totalCost);
        summary.put("totalUnitsSold", totalUnits);
        summary.put("totalSalesRecords", records.size());
        summary.put("averageRevenuePerRecord",
                records.isEmpty() ? 0 : totalRevenue / records.size());

        List<Map<String, Object>> monthlyData = new ArrayList<>();
        monthly.forEach((month, values) -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("month", month);
            item.put("revenue", values[0]);
            item.put("profit", values[1]);
            monthlyData.add(item);
        });

        List<Map<String, Object>> productData = new ArrayList<>();
        products.forEach((product, revenue) -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("product", product);
            item.put("revenue", revenue);
            productData.add(item);
        });
        productData.sort((a, b) -> Double.compare(
                (Double) b.get("revenue"), (Double) a.get("revenue")));

        List<Map<String, Object>> regionData = new ArrayList<>();
        regions.forEach((region, values) -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("region", region);
            item.put("revenue", values[0]);
            item.put("profit", values[1]);
            regionData.add(item);
        });

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("summary", summary);
        result.put("monthlyRevenue", monthlyData);
        result.put("productRevenue", productData);
        result.put("regionComparison", regionData);

        return result;
    }

    private int findColumn(Map<String, Integer> columns, String... names) {
        for (String name : names) {
            Integer index = columns.get(normalize(name));
            if (index != null) return index;
        }
        return -1;
    }

    private String normalize(String value) {
        return value == null ? "" :
                value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    private String cellText(Row row, int column,
                            DataFormatter formatter, FormulaEvaluator evaluator) {
        Cell cell = row.getCell(column);
        return cell == null ? "" :
                formatter.formatCellValue(cell, evaluator).trim();
    }

    private double cellNumber(Row row, int column,
                              DataFormatter formatter, FormulaEvaluator evaluator) {
        Cell cell = row.getCell(column);
        if (cell == null) return 0;

        if (cell.getCellType() == CellType.NUMERIC ||
                cell.getCellType() == CellType.FORMULA) {
            try {
                return evaluator.evaluate(cell).getNumberValue();
            } catch (Exception ignored) {
                // Try parsing formatted text below.
            }
        }

        String value = formatter.formatCellValue(cell, evaluator)
                .replace(",", "")
                .replace("₹", "")
                .replace("$", "")
                .trim();

        try {
            return value.isEmpty() ? 0 : Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String cellDate(Cell cell, DataFormatter formatter,
                            FormulaEvaluator evaluator) {
        if (cell == null) return "";

        if (cell.getCellType() == CellType.NUMERIC &&
                DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue()
                    .toLocalDate().toString();
        }

        String value = formatter.formatCellValue(cell, evaluator).trim();
        if (value.isBlank()) return "";

        try {
            return LocalDate.parse(value).toString();
        } catch (DateTimeParseException ignored) {
            // Try common spreadsheet date formats.
        }

        for (DateTimeFormatter format : List.of(
                DateTimeFormatter.ofPattern("d/M/uuuu"),
                DateTimeFormatter.ofPattern("d-M-uuuu"),
                DateTimeFormatter.ofPattern("M/d/uuuu"),
                DateTimeFormatter.ofPattern("d MMM uuuu"))) {
            try {
                return LocalDate.parse(value, format).toString();
            } catch (DateTimeParseException ignored) {
                // Try next format.
            }
        }

        return "";
    }
}
