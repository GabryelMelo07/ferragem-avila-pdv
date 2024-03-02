package com.ferragem.avila.pdv.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "item")
public class Item {
    
    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private Integer quantidade;

    @Column
    private BigDecimal preco;

    @ManyToOne
    @JoinColumn(name = "produto_id")
    private Produto produto;

    @ManyToOne
    @JoinColumn(name = "venda_id")
    private Venda venda;
    
}
