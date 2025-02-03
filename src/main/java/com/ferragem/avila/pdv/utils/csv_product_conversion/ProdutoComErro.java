package com.ferragem.avila.pdv.utils.csv_product_conversion;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProdutoComErro {
    
    private String descricao;
    private String codigoBarras;
    private String erro;
    
}
