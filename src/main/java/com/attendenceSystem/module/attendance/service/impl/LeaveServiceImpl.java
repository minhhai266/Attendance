package com.attendenceSystem.module.attendance.service.impl;

import java.time.LocalDate;
import java.time.temporal.IsoFields;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.attendenceSystem.module.attendance.dto.request.CreateLeaveRequest;
import com.attendenceSystem.module.attendance.dto.response.EmployeeLeaveListResponse;
import com.attendenceSystem.module.attendance.dto.response.LeaveDetailResponse;
import com.attendenceSystem.module.attendance.dto.response.LeaveRequestResponse;
import com.attendenceSystem.module.attendance.dto.response.ManagerLeaveListResponse;
import com.attendenceSystem.module.attendance.entity.LeaveRequest;
import com.attendenceSystem.module.attendance.entity.enums.LeaveStatus;
import com.attendenceSystem.module.attendance.mapper.request.CreateLeaveRequestMapper;
import com.attendenceSystem.module.attendance.mapper.response.EmployeeLeaveListResponseMapper;
import com.attendenceSystem.module.attendance.mapper.response.LeaveDetailResponseMapper;
import com.attendenceSystem.module.attendance.mapper.response.LeaveRequestResponseMapper;
import com.attendenceSystem.module.attendance.mapper.response.ManagerLeaveListResponseMapper;
import com.attendenceSystem.module.attendance.model.DateRange;
import com.attendenceSystem.module.attendance.repository.LeaveRequestRepository;

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
    private final EmployeeLeaveListResponseMapper employeeLeaveListResponseMapper;
    private final ManagerLeaveListResponseMapper managerLeaveListResponseMapper;
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
    public Page<EmployeeLeaveListResponse> getLeaveRequests(final Pageable pageable) {
        User user = userContextProvider.getCurrentUserEntity();
        return leaveRequestRepository.findByUser(user, pageable)
                .map(employeeLeaveListResponseMapper::fromEntity);
    }

    @Override
    public Page<EmployeeLeaveListResponse> getLeaveRequests(final LeaveStatus status, final String week,
            final Pageable pageable) {
        User user = userContextProvider.getCurrentUserEntity();

        DateRange range = parseWeekRange(week);

        return leaveRequestRepository.findLeaveRequestsWithFilters(user, status, range.startDate(), range.endDate(), pageable)
                .map(employeeLeaveListResponseMapper::fromEntity);
    }

    @Override
    public Page<ManagerLeaveListResponse> getAllLeaveRequests(final Pageable pageable) {
        return getAllLeaveRequests(null, null, null, pageable);
    }

    @Override
    public Page<ManagerLeaveListResponse> getAllLeaveRequests(final String keyword, final LeaveStatus status,
            final String week, final Pageable pageable) {
        DateRange range = parseWeekRange(week);
        String searchKeyword = null;
        if (keyword != null && !keyword.isBlank()) {
            searchKeyword = "%" + keyword.trim().toLowerCase() + "%";
        }
        return leaveRequestRepository.findAllManagerLeaveRequestsWithFilters(
                searchKeyword,
                status,
                range.startDate(),
                range.endDate(),
                pageable).map(managerLeaveListResponseMapper::fromEntity);
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

    private DateRange parseWeekRange(String week) {
    if (week == null || week.isBlank()) {
        return new DateRange(null, null);
    }
    try {
        int year = Integer.parseInt(week.substring(0, 4));
        int weekNumber = Integer.parseInt(week.substring(6));

        LocalDate weekStart = LocalDate.of(year, 1, 1)
                .with(IsoFields.WEEK_OF_WEEK_BASED_YEAR, weekNumber)
                .with(java.time.DayOfWeek.MONDAY);
        LocalDate weekEnd = weekStart.plusDays(6);
        
        return new DateRange(weekStart, weekEnd);
    } catch (Exception e) {
        return new DateRange(null, null);
    }
}
}
