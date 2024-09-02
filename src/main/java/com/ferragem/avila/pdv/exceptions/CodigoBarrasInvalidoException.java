package com.ferragem.avila.pdv.exceptions;

public class CodigoBarrasInvalidoException extends RuntimeException {

    public CodigoBarrasInvalidoException(String message) {
        super(message);
    }

    public CodigoBarrasInvalidoException() {
        super("Código de barras inválido.");
    }
    
}
