package com.attendenceSystem.module.faceid.repository;

import com.attendenceSystem.module.faceid.entity.FaceIdRecognition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FaceIdRecognitionRepository extends JpaRepository<FaceIdRecognition, Long> {
    boolean existsByTrackingId(String trackingId);
}