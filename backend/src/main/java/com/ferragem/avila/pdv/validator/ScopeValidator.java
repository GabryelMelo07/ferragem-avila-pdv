package com.ferragem.avila.pdv.validator;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import com.ferragem.avila.pdv.model.Role;
import com.ferragem.avila.pdv.repository.RoleRepository;

@Component
public class ScopeValidator implements OAuth2TokenValidator<Jwt> {

    @Autowired
    private RoleRepository roleRepository;
    
    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {
        List<String> scopes = token.getClaimAsStringList("scope");

        if (scopes != null && !scopes.isEmpty()) {
            for (String string : scopes) {
                Role role = roleRepository.findByNameIgnoreCase(string);
                if (role != null) {
                    return OAuth2TokenValidatorResult.success();
                }
            }
        }
        
        return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Invalid scopes", null));
    }
    
}