package com.attendenceSystem.module.attendance.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.attendenceSystem.module.attendance.entity.LeaveRequest;
import com.attendenceSystem.module.user.entity.User;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    Page<LeaveRequest> findByUser(User user, Pageable pageable);
}
