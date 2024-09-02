package com.ferragem.avila.pdv.model.utils;

import java.math.BigDecimal;

import com.ferragem.avila.pdv.model.enums.UnidadeMedida;
import com.opencsv.bean.CsvBindByName;

import lombok.Data;

@Data
public class CsvToProduto {

    @CsvBindByName(column = "descricao")
    private String descricao;
    
    @CsvBindByName(column = "unidade_medida")
    private UnidadeMedida unidadeMedida;
    
    @CsvBindByName(column = "estoque")
    private Float estoque;
    
    @CsvBindByName(column = "preco_fornecedor")
    private BigDecimal precoFornecedor;
    
    @CsvBindByName(column = "preco")
    private BigDecimal preco;
    
    @CsvBindByName(column = "codigo_barras")
    private String codigoBarrasEAN13;
    
}
