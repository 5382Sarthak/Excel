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
@CrossOrigin
public class DashboardController {

    @Autowired
    private ExcelService excelService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file,
                                             HttpSession session) {

        if (session.getAttribute("user") == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        try {
            excelService.parseExcel(file.getInputStream());
            return ResponseEntity.ok("File uploaded successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Upload failed!");
        }
    }
    @CrossOrigin(origins = "http://localhost:8080", allowCredentials = "true")
    @GetMapping("/view")
    public ResponseEntity<?> viewData(
            @RequestParam(defaultValue = "mostUsed") String sortBy,
            HttpSession session) {

        if (session.getAttribute("user") == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        List<ApiUsage> result = excelService.getSortedData(sortBy);
        return ResponseEntity.ok(result);
    }
}