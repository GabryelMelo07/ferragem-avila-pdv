package com.ferragem.avila.pdv.dto;

import java.time.LocalDateTime;

public record LoginResponseDto(String accessToken, LocalDateTime expiresIn) {
}
