package com.ferragem.avila.pdv.dto.auth;

import com.ferragem.avila.pdv.annotation.ValidString;

import io.swagger.v3.oas.annotations.media.Schema;

public record LoginRequestDto(@Schema(description = "Nome de usuário", defaultValue = "admin") @ValidString String username, @Schema(description = "Senha do usuário", defaultValue = "123") @ValidString String password) {
}
