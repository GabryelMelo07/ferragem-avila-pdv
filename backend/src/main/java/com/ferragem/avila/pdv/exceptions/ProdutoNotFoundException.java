package com.ferragem.avila.pdv.exceptions;

public class ProdutoNotFoundException extends RuntimeException {

    public ProdutoNotFoundException(String message) {
        super(message);
    }

    public ProdutoNotFoundException() {
        super("Produto não encontrado.");
    }
    
}
