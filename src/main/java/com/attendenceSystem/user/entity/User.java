package com.attendenceSystem.user.entity;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    private Long id;
    private String email;
    private String password;
    private String fullName;
    private String avatar;
    private String role;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
    private Boolean deleted;
}
