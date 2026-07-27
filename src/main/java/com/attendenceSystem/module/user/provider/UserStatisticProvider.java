package com.attendenceSystem.module.user.provider;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.attendenceSystem.module.user.entity.enums.Role;
import com.attendenceSystem.module.user.entity.enums.Status;
import com.attendenceSystem.module.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserStatisticProvider {
    private final UserRepository userRepository;

    public long getTotalAccounts() {
        return userRepository.count();
    }

    public long getAccountsByStatus(Status status) {
        return userRepository.countByStatus(status);
    }

    public long getTotalEmployeeAndManager() {
        return userRepository.countByRoleNot(Role.ADMIN);
    }

    public Map<String, Long> getRoleCounts() {
        return userRepository.countUsersGroupByRole()
                .stream()
                .filter(p -> p != null && p.getRole() != null)
                .collect(
                        Collectors.toMap(
                                p -> p.getRole().name(),
                                p -> p.getCount()));
    }

}
