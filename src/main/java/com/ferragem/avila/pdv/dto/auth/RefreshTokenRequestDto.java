package com.ferragem.avila.pdv.dto.auth;

import com.ferragem.avila.pdv.annotation.ValidString;

public record RefreshTokenRequestDto(@ValidString String accessToken, @ValidString String refreshToken) {
}
