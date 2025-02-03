package com.ferragem.avila.pdv.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.crypto.RSAEncrypter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@SpringBootTest
class SecurityConfigTest {

    @Value("${jwt.public.key}")
    private RSAPublicKey publicKey;

    @Value("${jwt.private.key}")
    private RSAPrivateKey privateKey;

    private RSAEncrypter encrypter;
    private RSADecrypter decrypter;

    @BeforeEach
    void setUp() {
        encrypter = new RSAEncrypter(publicKey);
        decrypter = new RSADecrypter(privateKey);
    }

    @Test
    void testJWEEncryptionDecryption() throws Exception {
        String originalMessage = "Teste de criptografia JWE";

        JWEObject jweObject = new JWEObject(
                new JWEHeader.Builder(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A256GCM).build(),
                new Payload(originalMessage)
        );

        jweObject.encrypt(encrypter);
        String encryptedMessage = jweObject.serialize();

        JWEObject decryptedJweObject = JWEObject.parse(encryptedMessage);
        decryptedJweObject.decrypt(decrypter);
        String decryptedMessage = decryptedJweObject.getPayload().toString();

        assertEquals(originalMessage, decryptedMessage);
    }
}
