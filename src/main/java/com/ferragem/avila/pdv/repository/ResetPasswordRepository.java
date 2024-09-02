package com.ferragem.avila.pdv.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ferragem.avila.pdv.model.ResetPasswordToken;

public interface ResetPasswordRepository extends JpaRepository<ResetPasswordToken, Long> {
}
