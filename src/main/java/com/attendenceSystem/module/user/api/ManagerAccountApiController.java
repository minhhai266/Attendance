package com.attendenceSystem.module.user.api;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.attendenceSystem.constant.Routes;
import com.attendenceSystem.module.user.dto.response.UserResponse;
import com.attendenceSystem.module.user.service.AccountService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping(Routes.API + Routes.Role.MANAGER)
public class ManagerAccountApiController {
    private final AccountService accountService;

    @GetMapping(Routes.Account.ROOT)
    public ResponseEntity<Page<UserResponse>> getListEmployeeAccount(@PageableDefault(size = 10) Pageable pageable){
        Page<UserResponse> users = accountService.getEmployees(pageable);
        return ResponseEntity.ok(users);
    }
    @GetMapping(Routes.Account.ROOT + "/{id}")
    public ResponseEntity<UserResponse> getDetailAccount(@PathVariable("id") Long id){
        UserResponse user = accountService.getUserById(id);
        return ResponseEntity.ok(user);
    }
    @PatchMapping(Routes.Account.ROOT + Routes.Action.UPDATE + "/{id}")
    public ResponseEntity<Void> changeEmployeeDepartment(
            @PathVariable("id") Long id,
            @RequestParam("department") String department) {
        accountService.changeDepartment(id, department);
        return ResponseEntity.ok().build();
    }
}
