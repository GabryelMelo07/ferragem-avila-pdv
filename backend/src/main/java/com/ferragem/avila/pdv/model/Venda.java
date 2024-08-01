package com.ferragem.avila.pdv.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.ferragem.avila.pdv.model.enums.FormaPagamento;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
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
public class Venda implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @FutureOrPresent
    private LocalDateTime dataHoraInicio;

    @Column
    @FutureOrPresent
    private LocalDateTime dataHoraConclusao;
    
    @Column(nullable = false)
    private boolean concluida;

    @Column(nullable = false)
    private BigDecimal precoTotal;

    @Column
    @Enumerated(EnumType.STRING)
    private FormaPagamento formaPagamento;
    
    @OneToMany(mappedBy = "venda", fetch = FetchType.EAGER)
    private List<Item> itens;

    public Venda() {
        this.dataHoraInicio = LocalDateTime.now();
        this.concluida = false;
        this.precoTotal = BigDecimal.ZERO;
        this.itens = new ArrayList<Item>();
    }

    public void calcularPrecoTotal() {
        if (!this.itens.isEmpty()) {
            this.precoTotal = BigDecimal.ZERO;
            
            for (Item item : this.itens) {
               this.precoTotal = this.precoTotal.add(item.getPreco());
            }
        } else {
            this.precoTotal = BigDecimal.ZERO;
        }
    }

}
