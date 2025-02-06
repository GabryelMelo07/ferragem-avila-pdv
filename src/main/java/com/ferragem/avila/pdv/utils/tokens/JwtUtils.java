package com.ferragem.avila.pdv.utils.tokens;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import com.ferragem.avila.pdv.model.Role;
import com.ferragem.avila.pdv.model.User;

@Component
public class JwtUtils {

	private final JwtEncoder jwtEncoder;

	public JwtUtils(JwtEncoder jwtEncoder) {
		this.jwtEncoder = jwtEncoder;
	}

	private JwtClaimsSet buildJwtTokenClaims(String issuer, String subject, String name, String surname, String scopes) {
		Instant now = Instant.now();
		Instant expiresAt = now.plus(3, ChronoUnit.HOURS);

		return JwtClaimsSet.builder()
				.issuer(issuer)
				.subject(subject)
				.issuedAt(now)
				.expiresAt(expiresAt)
				.claim("nome", "%s %s".formatted(name, surname))
				.claim("scope", scopes)
				.build();
	}

	public String buildJwtAccessToken(String issuer, String subject, User user) {
		String scopes = user.getRoles()
				.stream()
				.map(Role::getName)
				.collect(Collectors.joining(" "));
		var tokenClaims = buildJwtTokenClaims(issuer, subject, user.getNome(), user.getSobrenome(), scopes);
		return jwtEncoder.encode(JwtEncoderParameters.from(tokenClaims)).getTokenValue();
	}

}
