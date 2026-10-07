package com.sarthak.demo.service;

import com.sarthak.demo.model.ApiUsage;
import com.sarthak.demo.repository.ApiUsageRepository;

import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class ExcelService {

    @Autowired
    private ApiUsageRepository apiUsageRepository;

    // =========================================================
    // READ EXCEL FILE AND SAVE DATA TO MONGODB
    // =========================================================
    public void parseExcel(InputStream inputStream) {

        try (Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);

            // Delete previous uploaded API usage data
            apiUsageRepository.deleteAll();

            // Start from row 1 because row 0 contains column headers
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {

                Row row = sheet.getRow(i);

                if (row == null) {
                    continue;
                }

                // Ignore completely empty rows
                if (row.getCell(0) == null) {
                    continue;
                }

                String apiKey = getStringValue(row.getCell(0));

                int total = getIntValue(row.getCell(1));
                int success = getIntValue(row.getCell(2));
                int failed = getIntValue(row.getCell(3));

                ApiUsage apiUsage = new ApiUsage(
                        apiKey,
                        total,
                        success,
                        failed
                );

                // Save record to MongoDB
                apiUsageRepository.save(apiUsage);
            }

            System.out.println(
                    "Excel loaded into MongoDB: "
                    + apiUsageRepository.count()
                    + " records"
            );

        } catch (Exception e) {

            e.printStackTrace();

            throw new RuntimeException(
                    "Error reading Excel file",
                    e
            );
        }
    }

    // =========================================================
    // GET DATA AND SORT IT
    // =========================================================
    public List<ApiUsage> getSortedData(String sortBy) {

        List<ApiUsage> sortedList =
                new ArrayList<>(apiUsageRepository.findAll());

        switch (sortBy) {

            // Least total API calls
            case "leastUsed":

                sortedList.sort(
                        Comparator.comparing(
                                ApiUsage::getTotalCalls
                        )
                );

                break;

            // Most failed calls
            case "mostFailed":

                sortedList.sort(
                        Comparator.comparing(
                                ApiUsage::getFailedCalls
                        ).reversed()
                );

                break;

            // Least failed calls
            case "leastFailed":

                sortedList.sort(
                        Comparator.comparing(
                                ApiUsage::getFailedCalls
                        )
                );

                break;

            // Highest success percentage
            case "successRateHigh":

                sortedList.sort(
                        Comparator.comparing(
                                ApiUsage::getSuccessPercent
                        ).reversed()
                );

                break;

            // Lowest success percentage
            case "successRateLow":

                sortedList.sort(
                        Comparator.comparing(
                                ApiUsage::getSuccessPercent
                        )
                );

                break;

            // Highest failure percentage
            case "failureRateHigh":

                sortedList.sort(
                        Comparator.comparing(
                                ApiUsage::getFailedPercent
                        ).reversed()
                );

                break;

            // Lowest failure percentage
            case "failureRateLow":

                sortedList.sort(
                        Comparator.comparing(
                                ApiUsage::getFailedPercent
                        )
                );

                break;

            // Most total API calls
            case "mostUsed":
            default:

                sortedList.sort(
                        Comparator.comparing(
                                ApiUsage::getTotalCalls
                        ).reversed()
                );

                break;
        }

        return sortedList;
    }

    // =========================================================
    // SAFELY READ STRING FROM EXCEL
    // =========================================================
    private String getStringValue(Cell cell) {

        if (cell == null) {
            return "";
        }

        DataFormatter formatter = new DataFormatter();

        return formatter.formatCellValue(cell).trim();
    }

    // =========================================================
    // SAFELY READ INTEGER FROM EXCEL
    // =========================================================
    private int getIntValue(Cell cell) {

        if (cell == null) {
            return 0;
        }

        if (cell.getCellType() == CellType.NUMERIC) {

            return (int) cell.getNumericCellValue();
        }

        if (cell.getCellType() == CellType.STRING) {

            try {
                return Integer.parseInt(
                        cell.getStringCellValue().trim()
                );

            } catch (NumberFormatException e) {
                return 0;
            }
        }

        return 0;
    }
}