package com.ferragem.avila.pdv.model.utils;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class ProdutosFromCsv {
    
    private Integer produtosSalvos;
    private List<ProdutoComErro> produtosComErro;

    public ProdutosFromCsv() {
        this.produtosSalvos = 0;
        this.produtosComErro = new ArrayList<ProdutoComErro>();
    }

    public void somar() {
        this.produtosSalvos++;
    }
    
}
