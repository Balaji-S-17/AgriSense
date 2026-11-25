package com.agrisense.agrisense.service;

import com.agrisense.agrisense.model.CropAnalysis;
import com.agrisense.agrisense.model.User;
import com.agrisense.agrisense.repository.CropAnalysisRepository;
import com.agrisense.agrisense.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CropAnalysisService {

    private final CropAnalysisRepository cropAnalysisRepository;
    private final UserRepository userRepository;
    private final GeminiService geminiService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CropAnalysis analyzeCrop(MultipartFile image, String userEmail) {
        try {
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found!"));

            CropAnalysis analysis = CropAnalysis.builder()
                    .user(user)
                    .status(CropAnalysis.AnalysisStatus.PROCESSING)
                    .build();
            cropAnalysisRepository.save(analysis);

            String mimeType = image.getContentType() != null
                    ? image.getContentType() : "image/jpeg";

            String geminiResponse = geminiService.analyzeCropImage(
                    image.getBytes(), mimeType);

            String cleanJson = geminiResponse
                    .replace("```json", "")
                    .replace("```", "")
                    .trim();

            JsonNode result = objectMapper.readTree(cleanJson);

            analysis.setCropType(result.path("cropType").asText());
            analysis.setDiseaseDetected(result.path("diseaseDetected").asText());
            analysis.setConfidenceScore(result.path("confidenceScore").asDouble());
            analysis.setRemedyTamil(result.path("remedyTamil").asText());
            analysis.setRemedyEnglish(result.path("remedyEnglish").asText());
            analysis.setStatus(CropAnalysis.AnalysisStatus.COMPLETED);

            return cropAnalysisRepository.save(analysis);

        } catch (Exception e) {
            log.error("Analysis error: {}", e.getMessage());
            throw new RuntimeException("Crop analysis failed: " + e.getMessage());
        }
    }

    public List<CropAnalysis> getUserHistory(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found!"));
        return cropAnalysisRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
    }
}