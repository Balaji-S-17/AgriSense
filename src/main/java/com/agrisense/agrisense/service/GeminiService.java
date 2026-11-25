package com.agrisense.agrisense.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
@Slf4j
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String analyzeCropImage(byte[] imageBytes, String mimeType) {
        try {
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);

            String requestBody = "{"
                    + "\"contents\": [{"
                    + "\"parts\": ["
                    + "{"
                    + "\"inline_data\": {"
                    + "\"mime_type\": \"" + mimeType + "\","
                    + "\"data\": \"" + base64Image + "\""
                    + "}"
                    + "},"
                    + "{"
                    + "\"text\": \"You are an expert agricultural disease detection AI. Analyze this crop image and respond ONLY with a valid JSON object (no markdown, no code blocks, just pure JSON) in this exact format: {\\\"cropType\\\": \\\"crop name\\\", \\\"diseaseDetected\\\": \\\"disease name or Healthy\\\", \\\"confidenceScore\\\": 85, \\\"remedyTamil\\\": \\\"தமிழில் விரிவான தீர்வு\\\", \\\"remedyEnglish\\\": \\\"detailed remedy in English\\\"}\""
                    + "}"
                    + "]"
                    + "}]"
                    + "}";

            Request request = new Request.Builder()
                    .url(apiUrl + "?key=" + apiKey)
                    .post(RequestBody.create(
                            requestBody,
                            MediaType.parse("application/json")))
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                String responseBody = response.body().string();
                log.debug("Gemini raw response: {}", responseBody);
                JsonNode root = objectMapper.readTree(responseBody);
                String text = root.path("candidates")
                        .path(0)
                        .path("content")
                        .path("parts")
                        .path(0)
                        .path("text")
                        .asText();
                log.debug("Gemini text: {}", text);
                return text;
            }

            //return "{\"cropType\": \"Tomato\", \"diseaseDetected\": \"Late Blight\", \"confidenceScore\": 92, \"remedyTamil\": \"தக்காளி தாமதமான கருகல் நோய் கண்டறியப்பட்டது. நீலக்கல் கலவை (Bordeaux mixture) தெளிக்கவும். பாதிக்கப்பட்ட இலைகளை உடனே அகற்றவும். 7 நாட்களுக்கு ஒருமுறை மஞ்சள் நிற பூஞ்சாணக்கொல்லி தெளிக்கவும்.\", \"remedyEnglish\": \"Late Blight detected on Tomato. Apply Bordeaux mixture spray. Remove infected leaves immediately. Spray fungicide every 7 days.\"}";
        }
    catch (Exception e) {
            log.error("Gemini API error: {}", e.getMessage());
            throw new RuntimeException("Image analysis failed: " + e.getMessage());
        }
    }
}