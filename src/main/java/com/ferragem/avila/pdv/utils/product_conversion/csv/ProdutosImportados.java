package com.ferragem.avila.pdv.utils.product_conversion.csv;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class ProdutosImportados {
    
    private Integer produtosSalvos;
    private List<ProdutoComErro> produtosComErro;

    public ProdutosImportados() {
        this.produtosSalvos = 0;
        this.produtosComErro = new ArrayList<ProdutoComErro>();
    }

    public void somar() {
        this.produtosSalvos++;
    }
    
}
