package com.ferragem.avila.pdv.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ferragem.avila.pdv.dto.ProdutoDto;
import com.ferragem.avila.pdv.model.enums.UnidadeMedida;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "produto")
public class Produto implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 70, nullable = false, unique = true)
    private String descricao;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UnidadeMedida unidadeMedida;

    @Column(nullable = false)
    private Float estoque;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal precoFornecedor;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal preco;

    @Column(unique = true, length = 13, nullable = true)
    private String codigoBarrasEAN13;

    @Column(nullable = false)
    private boolean ativo;

    @Column()
    private String imagem;

    @OneToMany(mappedBy = "produto")
    @JsonIgnore
    private List<Item> itens;

    public Produto(ProdutoDto dto) {
        this.descricao = dto.descricao();
        this.unidadeMedida = dto.unidadeMedida();
        this.estoque = dto.estoque();
        this.precoFornecedor = dto.precoFornecedor();
        this.preco = dto.preco();
        this.codigoBarrasEAN13 = dto.codigoBarrasEAN13();
        this.ativo = true;
        this.itens = new ArrayList<Item>();
    }

    public Produto(ProdutoDto dto, String imagemUrl) {
        this.descricao = dto.descricao();
        this.unidadeMedida = dto.unidadeMedida();
        this.estoque = dto.estoque();
        this.precoFornecedor = dto.precoFornecedor();
        this.preco = dto.preco();
        this.codigoBarrasEAN13 = dto.codigoBarrasEAN13();
        this.ativo = true;
        this.imagem = imagemUrl;
        this.itens = new ArrayList<Item>();
    }

    public Produto(String descricao, UnidadeMedida unidadeMedida, Float estoque, BigDecimal precoFornecedor,
            BigDecimal preco, String codigoBarrasEAN13) {
        this.descricao = descricao;
        this.unidadeMedida = unidadeMedida;
        this.estoque = estoque;
        this.precoFornecedor = precoFornecedor;
        this.preco = preco;
        this.codigoBarrasEAN13 = codigoBarrasEAN13;
        this.ativo = true;
        this.itens = new ArrayList<Item>();
    }

    public void sumEstoque(float quantidade) {
        this.estoque += quantidade;
    }

    public void subtractEstoque(float quantidade) {
        this.estoque -= quantidade;
    }
    
}
