package com.ferragem.avila.pdv.exceptions;

public class ProdutoSemEstoqueException extends RuntimeException {

    public ProdutoSemEstoqueException(String message) {
        super(message);
    }
    
    public ProdutoSemEstoqueException() {
        super("Produto não tem estoque suficiente.");
    }
    
}
