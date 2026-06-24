package com.attendenceSystem.module.user.entity;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.attendenceSystem.module.user.entity.enums.Department;
import com.attendenceSystem.module.user.entity.enums.Role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "username", unique = true, nullable = false, length = 30)
    private String username;
    @Column(name = "email", unique = true, nullable = false, length = 255)
    private String email;
    @Column(name = "phone", unique = true)
    private String phone;
    @Column(name = "password", nullable = false, length = 255)
    private String password;
    @Column(name = "role", nullable = false)
    private Role role;
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;
    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;
    @Column(name = "department")
    private Department department;
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    @Column(name = "must_change_password", nullable = false)
    @Builder.Default
    private boolean mustChangePassword = false;
}
