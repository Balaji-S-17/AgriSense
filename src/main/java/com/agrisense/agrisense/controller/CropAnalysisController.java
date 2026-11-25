package com.agrisense.agrisense.controller;

import com.agrisense.agrisense.model.CropAnalysis;
import com.agrisense.agrisense.service.CropAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/crop")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CropAnalysisController {

    private final CropAnalysisService cropAnalysisService;

    @PostMapping("/analyze")
    public ResponseEntity<CropAnalysis> analyzeCrop(
            @RequestParam("image") MultipartFile image,
            Authentication authentication) {

        String userEmail = authentication.getName();
        CropAnalysis result = cropAnalysisService.analyzeCrop(image, userEmail);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/history")
    public ResponseEntity<List<CropAnalysis>> getHistory(
            Authentication authentication) {

        String userEmail = authentication.getName();
        List<CropAnalysis> history = cropAnalysisService.getUserHistory(userEmail);
        return ResponseEntity.ok(history);
    }
}