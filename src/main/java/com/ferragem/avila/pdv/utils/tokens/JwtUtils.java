package com.ferragem.avila.pdv.utils.tokens;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

@Component
public class JwtUtils {

    private final JwtEncoder jwtEncoder;

    public JwtUtils(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    private JwtClaimsSet buildJwtTokenClaims(String issuer, String subject, String scopes) {
        Instant now = Instant.now();
		Instant expiresAt = now.plus(3, ChronoUnit.HOURS);

        return JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(subject)
                .issuedAt(now)
                .expiresAt(expiresAt)
                .claim("scope", scopes)
                .build();
    }

    public String buildJwtAccessToken(String issuer, String subject, String scopes) {
        var tokenClaims = buildJwtTokenClaims(issuer, subject, scopes);
        return jwtEncoder.encode(JwtEncoderParameters.from(tokenClaims)).getTokenValue();
    }

}
