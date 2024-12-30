package com.ferragem.avila.pdv.utils.api_responses_examples;

public class DashboardResponses {
    
    public final static String GET_PRODUTOS_BAIXO_ESTOQUE = """
        {
            "page": {
                "size": 20,
                "number": 0,
                "totalElements": 1,
                "totalPages": 1
            },
            "content": [
                {
                "id": 21,
                "descricao": "teste1234",
                "unidadeMedida": "UNIDADE",
                "estoque": 1,
                "precoFornecedor": 1,
                "preco": 2,
                "codigoBarrasEAN13": "9875642740174",
                "ativo": true,
                "imagem": null
                }
            ]
        }
    """;
    
    public final static String GET_MAIS_VENDIDOS_MES = """
        [
            {
                "id": 1,
                "descricao": "Prego 12mm",
                "unidadeMedida": "UNIDADE",
                "estoque": 1623,
                "precoFornecedor": 9.5,
                "preco": 13.35,
                "codigoBarrasEAN13": "7890123456789",
                "ativo": true,
                "imagem": null
            },
            {
                "id": 2,
                "descricao": "Parafuso 17mm",
                "unidadeMedida": "UNIDADE",
                "estoque": 2453,
                "precoFornecedor": 10.2,
                "preco": 15.45,
                "codigoBarrasEAN13": "7890123456781",
                "ativo": true,
                "imagem": null
            }
        ]
    """;
    
    public final static String GET_GRAFICO_VENDAS = """
        {
            "data": "2024-12-27",
            "vendasDiarias": [
                {
                    "data": "2024-12-01",
                    "totalVendas": 0,
                    "totalLucro": 0
                },
                {
                    "data": "2024-12-02",
                    "totalVendas": 0,
                    "totalLucro": 0
                },
                {
                    "data": "2024-12-03",
                    "totalVendas": 0,
                    "totalLucro": 0
                }
            ]
        }
    """;
    
}
