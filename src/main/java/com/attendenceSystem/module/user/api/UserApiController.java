package com.attendenceSystem.module.user.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.attendenceSystem.constant.Routes;
import com.attendenceSystem.module.user.dto.request.ChangePasswordRequest;
import com.attendenceSystem.module.user.dto.request.UpdatePasswordWithOtpRequest;
import com.attendenceSystem.module.user.dto.request.UpdateUserInformationRequest;
import com.attendenceSystem.module.user.dto.response.UserInformationResponse;
import com.attendenceSystem.module.user.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(Routes.API + Routes.User.ROOT)
@RequiredArgsConstructor
public class UserApiController {
    private final UserService userService;

    @PutMapping(Routes.User.INFORMATION + Routes.Action.UPDATE)
    public ResponseEntity<UserInformationResponse> updateUserInformation(@Valid @RequestBody UpdateUserInformationRequest request) {
        UserInformationResponse response = userService.updateUserInformation(request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping(Routes.User.CHANGE_PASSWORD)
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(request);
        return ResponseEntity.ok().build();
    }

    @PatchMapping(Routes.User.UPDATE_PASSWORD)
    public ResponseEntity<Void> updatePasswordWithOtp(@Valid @RequestBody UpdatePasswordWithOtpRequest request) {
        userService.updatePasswordWithOtp(request);
        return ResponseEntity.ok().build();
    }
}
