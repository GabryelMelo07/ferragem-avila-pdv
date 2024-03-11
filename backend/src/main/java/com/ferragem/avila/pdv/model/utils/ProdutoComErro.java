package com.ferragem.avila.pdv.model.utils;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProdutoComErro {
    
    private Object produto;
    private String erro;
    
}
