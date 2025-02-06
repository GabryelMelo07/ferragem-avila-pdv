package com.ferragem.avila.pdv.dto.auth;

public record JweTokenDto(String issuer, String subject, String operationToken) {
}
