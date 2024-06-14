package com.ferragem.avila.pdv.exceptions;

public class VendaInativaException extends RuntimeException {
    
    public VendaInativaException(String message) {
        super(message);
    }

    public VendaInativaException() {
        super("Não existe nenhuma venda ativa.");
    }
    
}
