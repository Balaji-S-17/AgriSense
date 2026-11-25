package com.agrisense.agrisense.repository;

import com.agrisense.agrisense.model.CropAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CropAnalysisRepository extends JpaRepository<CropAnalysis, Long> {
    List<CropAnalysis> findByUserIdOrderByCreatedAtDesc(Long userId);
}