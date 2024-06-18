package com.ferragem.avila.pdv.model;

import java.io.Serializable;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ferragem.avila.pdv.model.enums.UnidadeMedida;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "item")
public class Item implements Serializable {
    
    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private Float quantidade;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal preco;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal precoUnitarioProduto;

    @ManyToOne
    @JoinColumn(name = "produto_id")
    private Produto produto;

    @ManyToOne
    @JoinColumn(name = "venda_id")
    @JsonIgnore
    private Venda venda;

    public Item(Float quantidade, Produto produto, Venda venda) {
        this.quantidade = quantidade;
        this.precoUnitarioProduto = produto.getPreco();
        this.produto = produto;
        this.venda = venda;
        calcularPrecoTotal();
    }
    
    public void calcularPrecoTotal() {
        if (produto.getUnidadeMedida() == UnidadeMedida.GRAMA) {
            BigDecimal precoPorKg = produto.getPreco().divide(new BigDecimal(1000));
            this.setPreco(precoPorKg.multiply(new BigDecimal(this.quantidade)));
        } else if (produto.getUnidadeMedida() == UnidadeMedida.METRO) {
            BigDecimal precoPorMetro = produto.getPreco().divide(new BigDecimal(100));
            this.setPreco(precoPorMetro.multiply(new BigDecimal(this.quantidade)));
        } else {
            this.setPreco(produto.getPreco().multiply(new BigDecimal(this.quantidade)));
        }
    }

    public void sumQuantidade(float quantidade) {
        this.quantidade += quantidade;
    }

    public void subtractQuantidade(float quantidade) {
        this.quantidade -= quantidade;
    }
    
}
