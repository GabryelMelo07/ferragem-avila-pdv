package com.ferragem.avila.pdv.dto;

import java.util.Date;

public record LoginResponseDto(String accessToken, Date expiresIn) {
}
