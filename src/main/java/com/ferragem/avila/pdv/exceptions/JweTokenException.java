package com.ferragem.avila.pdv.exceptions;

public class JweTokenException extends RuntimeException {

    public JweTokenException(String message) {
        super(message);
    }

    public JweTokenException() {
        super("Token inválido"); 
    }
    
}
