package com.attendenceSystem.module.user.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.attendenceSystem.module.user.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findUserByUsername(String username);
    Optional<User> findUserByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<User> findUserByUsernameOrEmail(String username, String email);
    boolean existsByUsernameOrEmail(String username, String email);
    Page<User> findAllByOrderByIdAsc(Pageable pageable);
    long countByIsActiveTrue();
    long countByIsActiveFalse();
    Optional<User> findByUsername(String username);
}
