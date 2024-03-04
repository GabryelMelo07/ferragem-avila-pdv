package com.ferragem.avila.pdv.model;

import java.time.LocalDateTime;
import java.util.List;

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

    @Column(nullable = false)
    private FormaPagamento formaPagamento;
    
    @OneToMany(mappedBy = "venda")
    private List<Item> itens;

}
