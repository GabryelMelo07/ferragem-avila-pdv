package com.ferragem.avila.pdv.exceptions;

public class JwtTokenValidationException extends RuntimeException {

    public JwtTokenValidationException(String message) {
        super(message);
    }

    public JwtTokenValidationException() {
        super("Token JWT inválido");
    }
    
}
