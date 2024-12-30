package com.ferragem.avila.pdv.utils.api_responses_examples;

public class VendaResponses {

    public final static String GET_ALL_CONCLUIDAS = """
        {
            "page": {
                "size": 20,
                "number": 0,
                "totalElements": 6,
                "totalPages": 1
            },
            "content": [
                {
                    "id": 1,
                    "dataHoraInicio": "2024-02-29T10:00:00",
                    "dataHoraConclusao": "2024-02-29T11:16:20.36",
                    "concluida": true,
                    "precoTotal": 550.2,
                    "formaPagamento": "Pix",
                    "vendedorId": "f6d3487b-841b-4346-940b-1805ddcc64b7",
                    "vendedorNome": "Admin Istrator",
                    "itens": [
                        {
                            "id": 1,
                            "quantidade": 15,
                            "preco": 200.25,
                            "precoUnitarioAtual": 13.35,
                            "precoFornecedorAtual": 9.5,
                            "produto": {
                                "id": 1,
                                "descricao": "string",
                                "unidadeMedida": "UNIDADE",
                                "estoque": 2457,
                                "precoFornecedor": 999999,
                                "preco": 999999,
                                "codigoBarrasEAN13": "7890123456789",
                                "ativo": true,
                                "imagem": "http://ferragem-avila-pdv-images.s3.localhost.localstack.cloud:4566/fbb6fad1-bb7d-4ded-a00d-5fc9f5e36299_Captura%20de%20tela%202024-12-24%20183620.png"
                            }
                        }
                    ]
                }
            ]
        }
    """;

    public final static String GET_BY_ID = """
        {
            "id": 1,
            "dataHoraInicio": "2024-02-29T10:00:00",
            "dataHoraConclusao": "2024-02-29T11:16:20.36",
            "concluida": true,
            "precoTotal": 550.2,
            "formaPagamento": "Pix",
            "vendedorId": "f6d3487b-841b-4346-940b-1805ddcc64b7",
            "vendedorNome": "Admin Istrator",
            "itens": [
                {
                    "id": 1,
                    "quantidade": 15,
                    "preco": 200.25,
                    "precoUnitarioAtual": 13.35,
                    "precoFornecedorAtual": 9.5,
                    "produto": {
                        "id": 1,
                        "descricao": "string",
                        "unidadeMedida": "UNIDADE",
                        "estoque": 2457,
                        "precoFornecedor": 999999,
                        "preco": 999999,
                        "codigoBarrasEAN13": "7890123456789",
                        "ativo": true,
                        "imagem": "http://ferragem-avila-pdv-images.s3.localhost.localstack.cloud:4566/fbb6fad1-bb7d-4ded-a00d-5fc9f5e36299_Captura%20de%20tela%202024-12-24%20183620.png"
                    }
                },
                {
                    "id": 2,
                    "quantidade": 5,
                    "preco": 349.95,
                    "precoUnitarioAtual": 69.99,
                    "precoFornecedorAtual": 49.99,
                    "produto": {
                        "id": 7,
                        "descricao": "Tinta marrom burgues arco iris",
                        "unidadeMedida": "UNIDADE",
                        "estoque": 4995,
                        "precoFornecedor": 49.99,
                        "preco": 69.99,
                        "codigoBarrasEAN13": "7890123456786",
                        "ativo": true,
                        "imagem": null
                    }
                }
            ]
        }
    """;

    public final static String GET_BETWEEN_DATES = """
        {
            "page": {
                "size": 20,
                "number": 0,
                "totalElements": 4,
                "totalPages": 1
            },
            "content": [
                {
                    "id": 2,
                    "dataHoraInicio": "2024-10-01T10:00:00",
                    "dataHoraConclusao": "2024-10-01T11:31:50.374",
                    "concluida": true,
                    "precoTotal": 66.75,
                    "formaPagamento": "Pix",
                    "vendedorId": "f6d3487b-841b-4346-940b-1805ddcc64b7",
                    "vendedorNome": "Admin Istrator",
                    "itens": [
                        {
                            "id": 3,
                            "quantidade": 5,
                            "preco": 66.75,
                            "precoUnitarioAtual": 13.35,
                            "precoFornecedorAtual": 9.5,
                            "produto": {
                                "id": 1,
                                "descricao": "string",
                                "unidadeMedida": "UNIDADE",
                                "estoque": 2457,
                                "precoFornecedor": 999999,
                                "preco": 999999,
                                "codigoBarrasEAN13": "7890123456789",
                                "ativo": true,
                                "imagem": "http://ferragem-avila-pdv-images.s3.localhost.localstack.cloud:4566/fbb6fad1-bb7d-4ded-a00d-5fc9f5e36299_Captura%20de%20tela%202024-12-24%20183620.png"
                            }
                        }
                    ]
                },
                {
                    "id": 3,
                    "dataHoraInicio": "2024-10-06T10:00:00",
                    "dataHoraConclusao": "2024-10-06T15:31:50.374",
                    "concluida": true,
                    "precoTotal": 667.5,
                    "formaPagamento": "Pix",
                    "vendedorId": "f6d3487b-841b-4346-940b-1805ddcc64b7",
                    "vendedorNome": "Admin Istrator",
                    "itens": [
                        {
                            "id": 4,
                            "quantidade": 25,
                            "preco": 333.75,
                            "precoUnitarioAtual": 13.35,
                            "precoFornecedorAtual": 9.5,
                            "produto": {
                                "id": 1,
                                "descricao": "string",
                                "unidadeMedida": "UNIDADE",
                                "estoque": 2457,
                                "precoFornecedor": 999999,
                                "preco": 999999,
                                "codigoBarrasEAN13": "7890123456789",
                                "ativo": true,
                                "imagem": "http://ferragem-avila-pdv-images.s3.localhost.localstack.cloud:4566/fbb6fad1-bb7d-4ded-a00d-5fc9f5e36299_Captura%20de%20tela%202024-12-24%20183620.png"
                            }
                        }
                    ]
                }
            ]
        }
    """;
    
    public final static String GET_ITENS = """
        [
            {
                "id": 8,
                "quantidade": 1,
                "preco": 15.25,
                "precoUnitarioAtual": 15.25,
                "precoFornecedorAtual": 8.99,
                "produto": {
                    "id": 5,
                    "descricao": "Alicate de bico",
                    "unidadeMedida": "UNIDADE",
                    "estoque": 919,
                    "precoFornecedor": 8.99,
                    "preco": 15.25,
                    "codigoBarrasEAN13": "7890123456784",
                    "ativo": true,
                    "imagem": "http://ferragem-avila-pdv-images.s3.localhost.localstack.cloud:4566/976f81b6-2b01-4a10-aa9c-55255f31c238_escada.jpeg"
                }
            },
            {
                "id": 9,
                "quantidade": 12,
                "preco": 11999988,
                "precoUnitarioAtual": 999999,
                "precoFornecedorAtual": 999999,
                "produto": {
                    "id": 1,
                    "descricao": "string",
                    "unidadeMedida": "UNIDADE",
                    "estoque": 2445,
                    "precoFornecedor": 999999,
                    "preco": 999999,
                    "codigoBarrasEAN13": "7890123456789",
                    "ativo": true,
                    "imagem": "http://ferragem-avila-pdv-images.s3.localhost.localstack.cloud:4566/fbb6fad1-bb7d-4ded-a00d-5fc9f5e36299_Captura%20de%20tela%202024-12-24%20183620.png"
                }
            }
        ]
    """;

    public final static String GET = """
        {
            "id": 6,
            "dataHoraInicio": "2024-10-02T05:50:33.804701",
            "dataHoraConclusao": null,
            "concluida": false,
            "precoTotal": 12000003.25,
            "formaPagamento": null,
            "vendedorId": "f6d3487b-841b-4346-940b-1805ddcc64b7",
            "vendedorNome": "Admin Istrator",
            "itens": [
                {
                    "id": 8,
                    "quantidade": 1,
                    "preco": 15.25,
                    "precoUnitarioAtual": 15.25,
                    "precoFornecedorAtual": 8.99,
                    "produto": {
                        "id": 5,
                        "descricao": "Alicate de bico",
                        "unidadeMedida": "UNIDADE",
                        "estoque": 919,
                        "precoFornecedor": 8.99,
                        "preco": 15.25,
                        "codigoBarrasEAN13": "7890123456784",
                        "ativo": true,
                        "imagem": "http://ferragem-avila-pdv-images.s3.localhost.localstack.cloud:4566/976f81b6-2b01-4a10-aa9c-55255f31c238_escada.jpeg"
                    }
                },
                {
                    "id": 9,
                    "quantidade": 12,
                    "preco": 11999988,
                    "precoUnitarioAtual": 999999,
                    "precoFornecedorAtual": 999999,
                    "produto": {
                        "id": 1,
                        "descricao": "string",
                        "unidadeMedida": "UNIDADE",
                        "estoque": 2445,
                        "precoFornecedor": 999999,
                        "preco": 999999,
                        "codigoBarrasEAN13": "7890123456789",
                        "ativo": true,
                        "imagem": "http://ferragem-avila-pdv-images.s3.localhost.localstack.cloud:4566/fbb6fad1-bb7d-4ded-a00d-5fc9f5e36299_Captura%20de%20tela%202024-12-24%20183620.png"
                    }
                }
            ]
        }        
    """;
    
}
