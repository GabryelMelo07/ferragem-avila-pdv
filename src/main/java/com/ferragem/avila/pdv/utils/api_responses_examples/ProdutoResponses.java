package com.ferragem.avila.pdv.utils.api_responses_examples;

public class ProdutoResponses {

    public final static String GET_ALL_PRODUTOS_ATIVOS = """
        {
            "page": {
                "size": 20,
                "number": 0,
                "totalElements": 8,
                "totalPages": 1
            },
            "content": [
                {
                    "id": 5,
                    "descricao": "Alicate de bico",
                    "unidadeMedida": "UNIDADE",
                    "estoque": 551,
                    "precoFornecedor": 8.99,
                    "preco": 15.25,
                    "codigoBarrasEAN13": "7890123456784",
                    "ativo": true,
                    "imagem": "http://ferragem-avila-pdv-images.s3.localhost.localstack.cloud:4566/976f81b6-2b01-4a10-aa9c-55255f31c238_escada.jpeg"
                },
                {
                    "id": 6,
                    "descricao": "Fita silver tape",
                    "unidadeMedida": "METRO",
                    "estoque": 1195,
                    "precoFornecedor": 1.1,
                    "preco": 2.75,
                    "codigoBarrasEAN13": "7890123456785",
                    "ativo": true,
                    "imagem": null
                }
            ]
        }
    """;

    public final static String GET_ALL_PRODUTOS_INATIVOS = """
        {
            "page": {
                "size": 20,
                "number": 0,
                "totalElements": 8,
                "totalPages": 1
            },
            "content": [
                {
                    "id": 6,
                    "descricao": "Fita silver tape",
                    "unidadeMedida": "METRO",
                    "estoque": 1195,
                    "precoFornecedor": 1.1,
                    "preco": 2.75,
                    "codigoBarrasEAN13": "7890123456785",
                    "ativo": false,
                    "imagem": null
                }
            ]
        }
    """;

    public final static String GET_BY_ID = """
        {
            "id": 4,
            "descricao": "Martelo BigHammer",
            "unidadeMedida": "UNIDADE",
            "estoque": 374,
            "precoFornecedor": 15.99,
            "preco": 25.49,
            "codigoBarrasEAN13": "7890123456783",
            "ativo": true,
            "imagem": null
        }
    """;
    
}
