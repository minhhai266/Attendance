package com.attendenceSystem.module.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.attendenceSystem.module.user.entity.User;
import com.attendenceSystem.module.user.entity.enums.Department;
import com.attendenceSystem.module.user.entity.enums.Specialization;
import com.attendenceSystem.module.user.entity.enums.Role;
import com.attendenceSystem.module.user.entity.enums.Status;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findUserByUsername(String username);
    Optional<User> findUserByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<User> findUserByUsernameOrEmail(String username, String email);
    boolean existsByUsernameOrEmail(String username, String email);
    Page<User> findAllByOrderByIdAsc(Pageable pageable);
    long countByStatus(Status status);
    long countByMustChangePasswordTrue();
    long countByRole(Role role);
    long countByRoleNot(Role role);
    Optional<User> findByUsername(String username);
    boolean existsByPhone(String phone);
    List<User> findBySpecializationAndRoleNot(Specialization specialization, Role role);
    List<User> findByDepartmentAndRoleNot(Department department, Role role);
    Page<User> findByRole(Role role, Pageable pageable);
    List<User> findByRoleNot(Role role);
}
