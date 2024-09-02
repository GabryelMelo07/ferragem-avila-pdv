package com.ferragem.avila.pdv.exceptions;

public class ProdutoNaoEncontradoException extends RuntimeException {

    public ProdutoNaoEncontradoException(String message) {
        super(message);
    }

    public ProdutoNaoEncontradoException() {
        super("Produto não encontrado.");
    }
    
}
