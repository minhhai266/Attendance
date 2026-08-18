package com.attendenceSystem.module.user.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.attendenceSystem.module.user.dto.response.UserSimpleResponse;
import com.attendenceSystem.module.user.dto.response.UserWithSpecializationResponse;
import com.attendenceSystem.module.user.entity.User;
import com.attendenceSystem.module.user.entity.enums.Department;
import com.attendenceSystem.module.user.entity.enums.Specialization;
import com.attendenceSystem.module.user.entity.enums.Role;
import com.attendenceSystem.module.user.entity.enums.Status;
import com.attendenceSystem.module.user.repository.projection.AdminAccountManageList;
import com.attendenceSystem.module.user.repository.projection.ManagerAccountManageList;
import com.attendenceSystem.module.user.repository.projection.RoleCountProjection;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

        // ===== Find =====

        Optional<User> findById(Long id);

        Optional<User> findByEmail(String email);

        Optional<User> findByUsernameOrEmail(String username, String email);

        Optional<User> findByUsername(String username);

        // ===== Exists =====

        boolean existsByUsername(String username);

        boolean existsByEmail(String email);

        boolean existsByUsernameOrEmail(String username, String email);

        boolean existsByPhone(String phone);

        // ===== List =====

        Page<User> findAllByOrderByIdAsc(Pageable pageable);

        Page<User> findByRole(Role role, Pageable pageable);

        List<User> findBySpecializationAndRoleNot(Specialization specialization, Role role);

        List<User> findByDepartmentAndRoleNot(Department department, Role role);

        List<User> findByRoleNot(Role role);

        Page<AdminAccountManageList> findBy(Pageable pageable);

        @Query("SELECT u FROM User u WHERE u.status = :status AND NOT EXISTS " +
                        "(SELECT a FROM AttendanceRecord a WHERE a.user = u AND a.attendanceDate = :date)")
        List<User> findUsersWithoutAttendanceForDate(
                        @Param("date") LocalDate date,
                        @Param("status") Status status);

        // ===== Statistics =====

        long countByStatus(Status status);

        long countByMustChangePasswordTrue();

        long countByRole(Role role);

        long countByRoleNot(Role role);

        @Query("SELECT u.role AS role, COUNT(u) AS count FROM User u GROUP BY u.role")
        List<RoleCountProjection> countUsersGroupByRole();

        // ===== DTO =====

        @Query("SELECT new com.attendenceSystem.module.user.dto.response.UserSimpleResponse(u.id, u.fullName) " +
                        "FROM User u WHERE u.department = :department AND u.role != :role")
        List<UserSimpleResponse> findFullNameByDepartmentAndRoleNot(
                        @Param("department") Department department,
                        @Param("role") Role role);

        @Query("SELECT new com.attendenceSystem.module.user.dto.response.UserWithSpecializationResponse(u.id, u.fullName, u.specialization) " +
                        "FROM User u WHERE u.role != :role ORDER BY u.fullName ASC")
        List<UserWithSpecializationResponse> findAllUsersWithSpecializationByRoleNot(@Param("role") Role role);
}
