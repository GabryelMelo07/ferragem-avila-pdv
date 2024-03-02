package com.ferragem.avila.pdv.model;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ferragem.avila.pdv.model.enums.FormaPagamento;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.FutureOrPresent;
import lombok.Data;

@Data
@Entity
@Table(name = "venda")
public class Venda {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @FutureOrPresent
    private LocalDateTime dataHora;
    
    @Column(nullable = false)
    private boolean concluida;

    @Column(nullable = false)
    private FormaPagamento formaPagamento;
    
    @OneToMany(mappedBy = "produto")
    @JsonIgnore
    private List<Item> itens;
    
}
