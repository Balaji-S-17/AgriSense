package com.agrisense.agrisense.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "crop_analysis")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CropAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"password", "createdAt", "phoneNumber"})
    private User user;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "crop_type")
    private String cropType;

    @Column(name = "disease_detected")
    private String diseaseDetected;

    @Column(name = "confidence_score")
    private Double confidenceScore;

    @Column(name = "remedy_tamil", columnDefinition = "TEXT")
    private String remedyTamil;

    @Column(name = "remedy_english", columnDefinition = "TEXT")
    private String remedyEnglish;

    @Enumerated(EnumType.STRING)
    private AnalysisStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum AnalysisStatus {
        PENDING, PROCESSING, COMPLETED, FAILED
    }
}