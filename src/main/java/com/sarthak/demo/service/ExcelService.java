package com.sarthak.demo.service;

import com.sarthak.demo.model.ApiUsage;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;

@Service
public class ExcelService {

    private final List<ApiUsage> data = new ArrayList<>();

    public void parseExcel(InputStream inputStream) {
        try (Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            data.clear();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);

                if (row == null) continue;

                String apiKey = row.getCell(0).getStringCellValue();
                int total = (int) row.getCell(1).getNumericCellValue();
                int success = (int) row.getCell(2).getNumericCellValue();
                int failed = (int) row.getCell(3).getNumericCellValue();

                data.add(new ApiUsage(apiKey, total, success, failed));
            }

            System.out.println("Excel loaded: " + data.size());

        } catch (Exception e) {
            throw new RuntimeException("Error reading Excel file", e);
        }
    }

    public List<ApiUsage> getSortedData(String sortBy) {

        List<ApiUsage> sortedList = new ArrayList<>(data);

        switch (sortBy) {

            case "leastUsed":
                sortedList.sort(Comparator.comparing(ApiUsage::getTotalCalls));
                break;

            case "mostFailed":
                sortedList.sort(Comparator.comparing(ApiUsage::getFailedCalls).reversed());
                break;

            case "leastFailed":
                sortedList.sort(Comparator.comparing(ApiUsage::getFailedCalls));
                break;

            case "successRateHigh":
                sortedList.sort(Comparator.comparing(ApiUsage::getSuccessPercent).reversed());
                break;

            case "successRateLow":
                sortedList.sort(Comparator.comparing(ApiUsage::getSuccessPercent));
                break;

            case "failureRateHigh":
                sortedList.sort(Comparator.comparing(ApiUsage::getFailedPercent).reversed());
                break;

            case "failureRateLow":
                sortedList.sort(Comparator.comparing(ApiUsage::getFailedPercent));
                break;

            case "mostUsed":
            default:
                sortedList.sort(Comparator.comparing(ApiUsage::getTotalCalls).reversed());
        }

        return sortedList;
    }
}