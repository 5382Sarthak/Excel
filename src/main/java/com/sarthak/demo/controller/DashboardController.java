package com.sarthak.demo.controller;

import com.sarthak.demo.model.ApiUsage;
import com.sarthak.demo.service.ExcelService;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@CrossOrigin(
    origins = "http://localhost:5173",
    allowCredentials = "true"
)
public class DashboardController {

    @Autowired
    private ExcelService excelService;

    // UPLOAD EXCEL FILE
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(
            @RequestParam("file") MultipartFile file,
            HttpSession session) {

        // Check login
        if (session.getAttribute("user") == null) {
            return ResponseEntity
                    .status(401)
                    .body("Unauthorized");
        }

        if (file.isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body("Please select an Excel file.");
        }

        try {

            excelService.parseExcel(file.getInputStream());

            return ResponseEntity.ok(
                    "File uploaded successfully!"
            );

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(500)
                    .body("Upload failed!");
        }
    }

    // VIEW DATA
    @GetMapping("/view")
    public ResponseEntity<?> viewData(
            @RequestParam(defaultValue = "mostUsed") String sortBy,
            HttpSession session) {

        // Check login
        if (session.getAttribute("user") == null) {
            return ResponseEntity
                    .status(401)
                    .body("Unauthorized");
        }

        List<ApiUsage> result =
                excelService.getSortedData(sortBy);

        return ResponseEntity.ok(result);
    }
}