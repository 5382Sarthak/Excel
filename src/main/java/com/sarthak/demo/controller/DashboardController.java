
package com.sarthak.demo.controller;

import com.sarthak.demo.service.ExcelService;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@CrossOrigin(
    origins = "http://localhost:5173",
    allowCredentials = "true"
)
public class DashboardController {

    @Autowired
    private ExcelService excelService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(
            @RequestParam("file") MultipartFile file,
            HttpSession session) {

        if (session.getAttribute("user") == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("Please select a sales Excel file.");
        }

        String filename = file.getOriginalFilename();
        if (filename == null ||
                !(filename.toLowerCase().endsWith(".xlsx") ||
                  filename.toLowerCase().endsWith(".xls"))) {
            return ResponseEntity.badRequest()
                    .body("Please upload an .xlsx or .xls file.");
        }

        try {
            int imported = excelService.parseExcel(file.getInputStream());

            return ResponseEntity.ok(
                    "Sales data imported successfully. Added " +
                    imported + " records. Previous data was retained."
            );

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(
                    "Import failed: " + e.getMessage()
            );
        }
    }

    @GetMapping("/view")
    public ResponseEntity<?> viewData(HttpSession session) {

        if (session.getAttribute("user") == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        Map<String, Object> analytics = excelService.getAnalytics();
        return ResponseEntity.ok(analytics);
    }
}
