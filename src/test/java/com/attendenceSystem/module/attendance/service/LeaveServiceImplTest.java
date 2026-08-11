package com.attendenceSystem.module.attendance.service;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.attendenceSystem.module.attendance.dto.request.CreateLeaveRequest;
import com.attendenceSystem.module.attendance.dto.response.LeaveRequestResponse;
import com.attendenceSystem.module.attendance.mapper.response.LeaveRequestResponseMapper;
import com.attendenceSystem.module.attendance.repository.LeaveRequestRepository;
import com.attendenceSystem.module.attendance.service.impl.LeaveServiceImpl;
import com.attendenceSystem.module.user.entity.User;
import com.attendenceSystem.module.user.provider.UserContextProvider;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeaveServiceImplTest {

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    @Mock
    private UserContextProvider userContextProvider;

    @Mock
    private LeaveScheduleService leaveScheduleService;

    @Mock
    private LeaveRequestResponseMapper leaveRequestResponseMapper;

    @InjectMocks
    private LeaveServiceImpl leaveService;

    @Test
    void testCreateLeaveRequestCallsValidateBlackoutPeriod() {
        // Arrange
        CreateLeaveRequest request = new CreateLeaveRequest();
        request.setStartDate(LocalDate.now().plusDays(1));
        request.setEndDate(LocalDate.now().plusDays(2));

        User user = new User();
        when(userContextProvider.getCurrentUserEntity()).thenReturn(user);
        when(leaveRequestResponseMapper.fromEntity(org.mockito.ArgumentMatchers.any()))
                .thenReturn(org.mockito.Mockito.mock(LeaveRequestResponse.class));

        // Act
        leaveService.createLeaveRequest(request);

        // Assert
        verify(leaveScheduleService).validateBlackoutPeriod(request.getStartDate());
    }
}
