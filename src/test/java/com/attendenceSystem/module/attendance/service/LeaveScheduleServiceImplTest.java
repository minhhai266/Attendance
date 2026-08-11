package com.attendenceSystem.module.attendance.service;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.attendenceSystem.module.attendance.entity.LeaveRequest;
import com.attendenceSystem.module.attendance.entity.enums.LeaveStatus;
import com.attendenceSystem.module.attendance.repository.LeaveRequestRepository;
import com.attendenceSystem.module.attendance.service.impl.LeaveScheduleServiceImpl;
import com.attendenceSystem.module.user.entity.User;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeaveScheduleServiceImplTest {

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    @InjectMocks
    private LeaveScheduleServiceImpl leaveScheduleService;

    @Test
    void testAutoRejectExpiredLeaveRequests() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        User user = User.builder()
                .id(1L)
                .build();

        LeaveRequest leave = LeaveRequest.builder()
                .id(1L)
                .user(user)
                .startDate(tomorrow)
                .status(LeaveStatus.PENDING)
                .build();

        when(leaveRequestRepository.findByStatusAndStartDateLessThanEqual(LeaveStatus.PENDING, tomorrow))
                .thenReturn(List.of(leave));

        leaveScheduleService.autoRejectExpiredLeaveRequests();

        verify(leaveRequestRepository, times(1)).save(leave);
        assert leave.getStatus() == LeaveStatus.REJECTED;
    }

    @Test
    void testAutoRejectWithNoExpiredLeaves() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        when(leaveRequestRepository.findByStatusAndStartDateLessThanEqual(LeaveStatus.PENDING, tomorrow))
                .thenReturn(List.of());

        leaveScheduleService.autoRejectExpiredLeaveRequests();

        verify(leaveRequestRepository, times(0)).save(any());
    }
}