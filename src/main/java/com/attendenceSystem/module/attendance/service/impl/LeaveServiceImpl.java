package com.attendenceSystem.module.attendance.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.attendenceSystem.module.attendance.entity.LeaveRequest;
import com.attendenceSystem.module.attendance.entity.enums.LeaveStatus;
import com.attendenceSystem.module.attendance.repository.LeaveRequestRepository;
import com.attendenceSystem.module.attendance.service.LeaveService;
import com.attendenceSystem.module.user.entity.enums.Role;
import com.attendenceSystem.util.SecurityUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LeaveServiceImpl implements LeaveService {
    private final LeaveRequestRepository leaveRequestRepository;

    @Transactional
    @Override
    public void acceptLeave(Long id) {
        validateManagerAction();
        LeaveRequest leaveRequest = findByIdWithStatusPending(id);
        leaveRequest.setStatus(LeaveStatus.APPROVED);
    }

    @Transactional
    @Override
    public void rejectLeave(Long id) {
        validateManagerAction();
        LeaveRequest leaveRequest = findByIdWithStatusPending(id);
        leaveRequest.setStatus(LeaveStatus.REJECTED);
    }

    private void validateManagerAction() {
        if (SecurityUtil.getCurrentUserRole() != Role.MANAGER) {
            throw new IllegalStateException("Bạn không có quyền thực hiện hành động này");
        }
    }

    private LeaveRequest findByIdWithStatusPending(Long id) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy đơn xin nghỉ phép"));
        if (leaveRequest.getStatus() != LeaveStatus.PENDING) {
            throw new IllegalStateException("Đơn nghỉ phép đã được xử lý");
        }
        return leaveRequest;
    }

}
