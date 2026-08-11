package com.attendenceSystem.module.attendance.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.attendenceSystem.module.attendance.dto.request.CreateLeaveRequest;
import com.attendenceSystem.module.attendance.dto.response.LeaveDetailResponse;
import com.attendenceSystem.module.attendance.dto.response.LeaveRequestResponse;
import com.attendenceSystem.module.attendance.entity.LeaveRequest;
import com.attendenceSystem.module.attendance.entity.enums.LeaveStatus;
import com.attendenceSystem.module.attendance.mapper.request.CreateLeaveRequestMapper;
import com.attendenceSystem.module.attendance.mapper.response.LeaveDetailResponseMapper;
import com.attendenceSystem.module.attendance.mapper.response.LeaveRequestResponseMapper;
import com.attendenceSystem.module.attendance.repository.LeaveRequestRepository;
import com.attendenceSystem.module.attendance.repository.LeaveRequestSpecification;
import com.attendenceSystem.module.attendance.service.LeaveScheduleService;
import com.attendenceSystem.module.attendance.service.LeaveService;
import com.attendenceSystem.module.user.entity.User;
import com.attendenceSystem.module.user.entity.enums.Role;
import com.attendenceSystem.module.user.provider.UserContextProvider;
import com.attendenceSystem.util.SecurityUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LeaveServiceImpl implements LeaveService {
    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveRequestResponseMapper leaveRequestResponseMapper;
    private final LeaveDetailResponseMapper leaveDetailResponseMapper;
    private final UserContextProvider userContextProvider;
    private final LeaveScheduleService leaveScheduleService;

    @Transactional
    @Override
    public void acceptLeave(final Long id) {
        validateManagerAction();
        LeaveRequest leaveRequest = findByIdWithStatusPending(id);
        leaveRequest.setStatus(LeaveStatus.APPROVED);
    }

    @Transactional
    @Override
    public void rejectLeave(final Long id) {
        validateManagerAction();
        LeaveRequest leaveRequest = findByIdWithStatusPending(id);
        leaveRequest.setStatus(LeaveStatus.REJECTED);
    }

    @Transactional
    @Override
    public LeaveRequestResponse createLeaveRequest(final CreateLeaveRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Yêu cầu không hợp lệ");
        }
        if (request.getStartDate() == null || request.getEndDate() == null) {
            throw new IllegalArgumentException("Ngày bắt đầu và ngày kết thúc không được để trống");
        }
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new IllegalArgumentException("Ngày bắt đầu phải trước ngày kết thúc");
        }

        leaveScheduleService.validateBlackoutPeriod(request.getStartDate());

        User user = userContextProvider.getCurrentUserEntity();
        LeaveRequest leave = CreateLeaveRequestMapper.toEntity(request, user);
        LeaveRequest saved = leaveRequestRepository.save(leave);
        return leaveRequestResponseMapper.fromEntity(saved);
    }

    @Override
    public Page<LeaveRequestResponse> getLeaveRequests(final Pageable pageable) {
        User user = userContextProvider.getCurrentUserEntity();
        return leaveRequestRepository.findByUser(user, pageable)
                .map(leaveRequestResponseMapper::fromEntity);
    }

    @Override
    public Page<LeaveRequestResponse> getLeaveRequests(final LeaveStatus status, final String week,
            final Pageable pageable) {
        User user = userContextProvider.getCurrentUserEntity();
        Specification<LeaveRequest> spec = Specification
                .where(hasUser(user))
                .and(LeaveRequestSpecification.hasStatus(status))
                .and(LeaveRequestSpecification.hasWeek(week));
        return leaveRequestRepository.findAll(spec, pageable).map(leaveRequestResponseMapper::fromEntity);
    }

    @Override
    public Page<LeaveRequestResponse> getAllLeaveRequests(final Pageable pageable) {
        return leaveRequestRepository.findAll(pageable).map(leaveRequestResponseMapper::fromEntity);
    }

    @Override
    public Page<LeaveRequestResponse> getAllLeaveRequests(final String keyword, final LeaveStatus status,
            final String week, final Pageable pageable) {
        Specification<LeaveRequest> spec = Specification
                .where(LeaveRequestSpecification.hasKeyword(keyword))
                .and(LeaveRequestSpecification.hasStatus(status))
                .and(LeaveRequestSpecification.hasWeek(week));
        return leaveRequestRepository.findAll(spec, pageable).map(leaveRequestResponseMapper::fromEntity);
    }

    @Override
    public LeaveDetailResponse getLeaveDetail(final Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID yêu cầu nghỉ phép không hợp lệ");
        }
        LeaveRequest leave = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy yêu cầu nghỉ phép với ID: " + id));
        return leaveDetailResponseMapper.fromEntity(leave);
    }

    private void validateManagerAction() {
        if (SecurityUtil.getCurrentUserRole() != Role.MANAGER) {
            throw new IllegalStateException("Bạn không có quyền thực hiện hành động này");
        }
    }

    private LeaveRequest findByIdWithStatusPending(final Long id) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy đơn xin nghỉ phép"));
        if (leaveRequest.getStatus() != LeaveStatus.PENDING) {
            throw new IllegalStateException("Đơn nghỉ phép đã được xử lý");
        }
        return leaveRequest;
    }

    private Specification<LeaveRequest> hasUser(final User user) {
        return (root, query, cb) -> cb.equal(root.get("user"), user);
    }

}
