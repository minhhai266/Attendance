package com.attendenceSystem.module.user.service;


import com.attendenceSystem.module.user.dto.request.ChangePasswordRequest;
import com.attendenceSystem.module.user.dto.request.UpdatePasswordRequest;
import com.attendenceSystem.module.user.dto.request.UpdatePasswordWithOtpRequest;
import com.attendenceSystem.module.user.dto.request.UpdateUserInformationRequest;
import com.attendenceSystem.module.user.dto.response.UserInformationResponse;
import com.attendenceSystem.module.user.entity.User;

public interface UserService {
    void changePassword(ChangePasswordRequest request);
    void updatePassword(User user, UpdatePasswordRequest request);
    void updatePasswordWithOtp(UpdatePasswordWithOtpRequest request);

    UserInformationResponse updateUserInformation(UpdateUserInformationRequest request);

}
