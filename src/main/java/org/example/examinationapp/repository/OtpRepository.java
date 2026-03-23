package org.example.examinationapp.repository;

import org.example.examinationapp.entity.Otp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpRepository extends JpaRepository<Otp, Long> {
    // Find the latest unused OTP by user email and code
    Optional<Otp> findTopByUserEmailAndCodeAndUsedFalseOrderByIssuedAtDesc(String email, String code);

    // Useful for deleting old OTPs before issuing a new one
    void deleteAllByUserEmail(String email);
}
