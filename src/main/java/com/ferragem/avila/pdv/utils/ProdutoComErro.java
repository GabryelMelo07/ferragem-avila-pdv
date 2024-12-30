package com.ferragem.avila.pdv.utils;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProdutoComErro {
    
    private String descricao;
    private String codigoBarras;
    private String erro;
    
}
