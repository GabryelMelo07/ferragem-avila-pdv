package com.ferragem.avila.pdv.validator;

import java.time.Instant;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

public class IssuedAtValidator implements OAuth2TokenValidator<Jwt> {
    
    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {
        Instant issuedAt = token.getIssuedAt();

        if (issuedAt != null && issuedAt.isBefore(Instant.now()))
            return OAuth2TokenValidatorResult.success();
        
        return OAuth2TokenValidatorResult
                .failure(new OAuth2Error("invalid_token", "The token cannot be used before the issued at time", null));
    }
    
}