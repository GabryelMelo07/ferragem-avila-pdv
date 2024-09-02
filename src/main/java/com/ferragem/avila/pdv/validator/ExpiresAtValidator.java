package com.ferragem.avila.pdv.validator;

import java.time.Instant;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

public class ExpiresAtValidator implements OAuth2TokenValidator<Jwt> {
    
    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {
        Instant expiresAt = token.getExpiresAt();

        if (expiresAt != null && expiresAt.isAfter(Instant.now()))
            return OAuth2TokenValidatorResult.success();
        
        return OAuth2TokenValidatorResult
                .failure(new OAuth2Error("invalid_token", "The token has been expired", null));
    }
    
}