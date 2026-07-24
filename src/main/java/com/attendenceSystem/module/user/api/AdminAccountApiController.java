package com.attendenceSystem.module.user.api;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.attendenceSystem.constant.Routes;
import com.attendenceSystem.module.user.dto.response.UserResponse;
import com.attendenceSystem.module.user.service.AccountService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequestMapping(Routes.API + Routes.Role.ADMIN)
@RequiredArgsConstructor
public class AdminAccountApiController {
    private final AccountService accountService;

    @GetMapping(Routes.Account.ROOT)
    public ResponseEntity<Page<UserResponse>> getListAccount(@PageableDefault(size = 10) Pageable pageable) {
        Page<UserResponse> users = accountService.getUsers(pageable);
        return ResponseEntity.ok(users);
    }
    
    @GetMapping(Routes.Account.ROOT + "/{id}")
    public ResponseEntity<UserResponse> getDetailUser(@PathVariable("id") Long id){
        UserResponse user = accountService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @PatchMapping(Routes.Account.ROOT + Routes.Action.ACTIVATE + "/{id}")
    public ResponseEntity<Void> activeUser(@PathVariable("id") Long id){
        accountService.activateUser(id);
        return ResponseEntity.ok().build();
    }
    @PatchMapping(Routes.Account.ROOT + Routes.Action.DEACTIVATE + "/{id}")
    public ResponseEntity<Void> deactiveUser(@PathVariable("id") Long id){
        accountService.deactivateUser(id);
        return ResponseEntity.ok().build();
    }
}
