package com.ferragem.avila.pdv.validator;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import com.ferragem.avila.pdv.utils.tokens.RedisTokenUtils;

@Component
public class RevokedTokenValidator implements OAuth2TokenValidator<Jwt> {

	private final RedisTokenUtils revokeTokenUtils;

	public RevokedTokenValidator(RedisTokenUtils revokeTokenUtils) {
		this.revokeTokenUtils = revokeTokenUtils;
	}

	@Override
	public OAuth2TokenValidatorResult validate(Jwt token) {
		if (revokeTokenUtils.isTokenRevoked(token.getTokenValue())) {
			return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Token revoked", null));
		}

		return OAuth2TokenValidatorResult.success();
	}

}
