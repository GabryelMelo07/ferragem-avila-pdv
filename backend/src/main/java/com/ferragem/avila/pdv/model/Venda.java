package com.ferragem.avila.pdv.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.ferragem.avila.pdv.model.enums.FormaPagamento;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.FutureOrPresent;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "venda")
public class Venda {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @FutureOrPresent
    private LocalDateTime dataHoraInicio;

    @Column(nullable = false)
    @FutureOrPresent
    private LocalDateTime dataHoraConclusao;
    
    @Column(nullable = false)
    private boolean concluida;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal precoTotal;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private FormaPagamento formaPagamento;
    
    @OneToMany(mappedBy = "venda")
    private List<Item> itens;

    public Venda(Long id) {
        this.id = (id != null) ? id + 1 : 1;
        this.dataHoraInicio = LocalDateTime.now();
        this.concluida = false;
        this.precoTotal = BigDecimal.ZERO;
        this.itens = new ArrayList<Item>();
    }

    public void calcularPrecoTotal() {
        for (Item item : this.itens) {
           this.precoTotal = this.precoTotal.add(item.getPreco());
        }
    }

}
