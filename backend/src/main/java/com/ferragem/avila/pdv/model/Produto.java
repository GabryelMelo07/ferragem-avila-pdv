package com.ferragem.avila.pdv.model;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ferragem.avila.pdv.model.enums.UnidadeMedida;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "produto")
public class Produto {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 70, nullable = false)
    private String descricao;

    @Column(nullable = false)
    private UnidadeMedida unidadeMedida;

    @Column(nullable = false)
    private Number estoque;

    @Column(nullable = false)
    private BigDecimal preco;

    @Column(unique = true, length = 13)
    private String codigoBarrasEAN13;

    @Column
    private boolean ativo;

    @OneToMany(mappedBy = "produto")
    @JsonIgnore
    private List<Item> itens;
    
}
