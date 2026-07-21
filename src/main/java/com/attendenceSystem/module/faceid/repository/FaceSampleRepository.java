package com.attendenceSystem.module.faceid.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.attendenceSystem.module.faceid.entity.FaceSample;
import com.attendenceSystem.module.faceid.entity.FaceProfile;

public interface FaceSampleRepository extends JpaRepository<FaceSample, Long> {
    List<FaceSample> findByFaceProfile(FaceProfile faceProfile);
    List<FaceSample> findByFaceProfileOrderBySampleOrderAsc(FaceProfile faceProfile);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM FaceSample fs WHERE fs.faceProfile = :faceProfile")
    void deleteByFaceProfile(@Param("faceProfile") FaceProfile faceProfile);
}