package com.attendenceSystem.module.attendance.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.attendenceSystem.module.attendance.entity.LeaveRequest;
import com.attendenceSystem.module.user.entity.User;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long>, JpaSpecificationExecutor<LeaveRequest> {
    Page<LeaveRequest> findByUser(User user, Pageable pageable);

    Optional<LeaveRequest> findById(long id);
}