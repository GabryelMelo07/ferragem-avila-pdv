package com.ferragem.avila.pdv.utils.product_conversion.csv;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProdutoComErro {
    
    private String descricao;
    private String codigoBarras;
    private String erro;
    
}
