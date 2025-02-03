package com.ferragem.avila.pdv.utils.tokens;

import java.security.interfaces.RSAPrivateKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import org.springframework.stereotype.Component;

import com.ferragem.avila.pdv.dto.JweTokenDto;
import com.ferragem.avila.pdv.exceptions.JweTokenException;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEDecrypter;
import com.nimbusds.jose.JWEEncrypter;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.Payload;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

@Component
public class JweUtils {
	
	private final JWEEncrypter jweEncrypter;
	private final JWEDecrypter jweDecrypter;
	private final RSAPrivateKey privateKey;


	public JweUtils(JWEEncrypter jweEncrypter, JWEDecrypter jweDecrypter, RSAPrivateKey privateKey) {
        this.jweEncrypter = jweEncrypter;
        this.jweDecrypter = jweDecrypter;
        this.privateKey = privateKey;
    }
	
	public String generateJWE(String issuer, String subject, String operationToken, Instant expiresAt) throws JOSEException {
		Date expirationTime = Date.from(expiresAt);
		
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
				.issuer(issuer)
                .subject(subject)
				.expirationTime(expirationTime)
				.claim("operation_token", operationToken)
                .build();

        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claimsSet);
		signedJWT.sign(new RSASSASigner(privateKey)); 
		
        JWEHeader jweHeader = new JWEHeader.Builder(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A256GCM).contentType("JWT").build();
        JWEObject jweObject = new JWEObject(jweHeader, new Payload(signedJWT));

        jweObject.encrypt(jweEncrypter);
        return jweObject.serialize();
    }

	public JweTokenDto decryptJWE(String jweString) throws JOSEException, java.text.ParseException {
        JWEObject jweObject = JWEObject.parse(jweString);
        jweObject.decrypt(jweDecrypter);
        SignedJWT signedJWT = jweObject.getPayload().toSignedJWT();
		JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();

		Date expiresAt = claimsSet.getExpirationTime();

		if (expiresAt.before(Date.from(Instant.now().truncatedTo(ChronoUnit.SECONDS)))) {
			throw new JweTokenException("Token expirado");
		}
		
		String issuer = claimsSet.getIssuer();
		String subject = claimsSet.getSubject();
		String operationToken = String.valueOf(claimsSet.getClaim("operation_token"));
		
        return new JweTokenDto(issuer, subject, operationToken);
    }
	
}
