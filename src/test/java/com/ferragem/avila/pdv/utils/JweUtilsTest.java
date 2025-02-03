package com.ferragem.avila.pdv.utils;

import static org.junit.jupiter.api.Assertions.*;

import com.ferragem.avila.pdv.dto.JweTokenDto;
import com.ferragem.avila.pdv.exceptions.JweTokenException;
import com.ferragem.avila.pdv.utils.tokens.JweUtils;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.crypto.RSAEncrypter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.time.Instant;

@SpringBootTest
class JweUtilsTest {

    private JweUtils jweUtils;
    private RSAPublicKey publicKey;
    private RSAPrivateKey privateKey;

    @BeforeEach
    void setUp() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        
        privateKey = (RSAPrivateKey) keyPair.getPrivate();
        publicKey = (RSAPublicKey) keyPair.getPublic();

        RSAEncrypter encrypter = new RSAEncrypter(publicKey);
        RSADecrypter decrypter = new RSADecrypter(privateKey);

        jweUtils = new JweUtils(encrypter, decrypter, privateKey);
    }

    @Test
    void testGenerateAndDecryptJWE() throws JOSEException, ParseException {
        String issuer = "test_issuer";
        String subject = "test_subject";
        String operationToken = "test_operation_token";
        Instant expiresAt = Instant.now().plusSeconds(60);

        String jweToken = jweUtils.generateJWE(issuer, subject, operationToken, expiresAt);
        assertNotNull(jweToken, "O token JWE não deve ser nulo");

        JweTokenDto decryptedToken = jweUtils.decryptJWE(jweToken);
        assertNotNull(decryptedToken, "O token descriptografado não deve ser nulo");

        assertEquals(issuer, decryptedToken.issuer(), "Issuer não corresponde");
        assertEquals(subject, decryptedToken.subject(), "Subject não corresponde");
        assertEquals(operationToken, decryptedToken.operationToken(), "Operation Token não corresponde");
    }

    @Test
    void testDecryptExpiredJWE() throws JOSEException, InterruptedException {
        String issuer = "expired_issuer";
        String subject = "expired_subject";
        String operationToken = "expired_operation_token";
        Instant expiresAt = Instant.now().minusSeconds(10);

        String jweToken = jweUtils.generateJWE(issuer, subject, operationToken, expiresAt);
        assertNotNull(jweToken, "O token JWE não deve ser nulo");

        assertThrows(JweTokenException.class, () -> jweUtils.decryptJWE(jweToken), "Esperado erro de token expirado");
    }
}
