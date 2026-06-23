package com.attendenceSystem.module.faceid.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.attendenceSystem.module.faceid.entity.FaceProfile;
import com.attendenceSystem.module.user.entity.User;

public interface FaceProfileRepository extends JpaRepository<FaceProfile, Long> {
    Optional<FaceProfile> findByUser(User user);
    Optional<FaceProfile> findByFaceCode(String faceCode);
    Page<FaceProfile> findByFaceCodeContainingIgnoreCase(String faceCode, Pageable pageable);
    Page<FaceProfile> findByUser(User user, Pageable pageable);
}
