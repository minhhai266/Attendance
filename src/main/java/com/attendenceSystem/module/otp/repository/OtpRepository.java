package com.attendenceSystem.module.otp.repository;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.attendenceSystem.module.otp.entity.Otp;
import com.attendenceSystem.module.otp.entity.enums.OtpPurpose;

@Repository
public interface OtpRepository extends JpaRepository<Otp, Long> {

    Optional<Otp> findTopByDestinationAndPurposeAndUsedFalseOrderByCreatedAtDesc(
            String destination, OtpPurpose purpose);

    @Modifying
    @Query("UPDATE Otp o SET o.used = true WHERE o.destination = :destination AND o.purpose = :purpose")
    void invalidateByDestinationAndPurpose(
            @Param("destination") String destination,
            @Param("purpose") OtpPurpose purpose);

    @Modifying
    @Query("DELETE FROM Otp o WHERE o.expiredAt < :now")
    void deleteExpiredOtps(@Param("now") Instant now);
}