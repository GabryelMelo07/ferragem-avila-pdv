package com.ferragem.avila.pdv.exceptions;

public class VendaNotFoundException extends RuntimeException {
    
    public VendaNotFoundException(String message) {
        super(message);
    }

    public VendaNotFoundException() {
        super("Não existe nenhuma venda ativa.");
    }
    
}
