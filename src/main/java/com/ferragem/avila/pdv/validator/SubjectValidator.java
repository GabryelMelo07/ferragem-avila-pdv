package com.ferragem.avila.pdv.validator;

import java.util.UUID;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import com.ferragem.avila.pdv.repository.UserRepository;

@Component
public class SubjectValidator implements OAuth2TokenValidator<Jwt> {

    private final UserRepository userRepository;
    
    public SubjectValidator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {
        UUID userId = UUID.fromString(token.getSubject());

        if (userRepository.findById(userId).isPresent())
            return OAuth2TokenValidatorResult.success();
        
        return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Invalid scopes", null));
    }
    
}
