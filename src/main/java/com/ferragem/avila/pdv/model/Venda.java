package com.ferragem.avila.pdv.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.ferragem.avila.pdv.model.enums.FormaPagamento;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString.Include;

@Data
@Entity
@Table(name = "venda")
public class Venda implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @Include
    private Long id;

    @Column(nullable = false)
    private LocalDateTime dataHoraInicio;

    @Column
    private LocalDateTime dataHoraConclusao;

    @Column(nullable = false)
    private boolean concluida;

    @Column(nullable = false)
    private BigDecimal precoTotal;

    @Column
    @Enumerated(EnumType.STRING)
    private FormaPagamento formaPagamento;

    @Column(nullable = false)
    private UUID vendedorId;

    @Column(nullable = false)
    private String vendedorNome;

    @OneToMany(mappedBy = "venda", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<Item> itens;

    public Venda() {
        this.dataHoraInicio = LocalDateTime.now(ZoneId.of("America/Sao_Paulo"));
        this.concluida = false;
        this.precoTotal = BigDecimal.ZERO;
        this.itens = new ArrayList<Item>();
    }

    public Venda(UUID vendedorId, String vendedorNome) {
        this();
        this.vendedorId = vendedorId;
        this.vendedorNome = vendedorNome;
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

    public Optional<Item> getItemByProductId(long idProduto) {
        return itens.stream()
                .filter(i -> i.getProduto().getId().equals(idProduto))
                .findFirst();
    }

	public Optional<Item> getItem(long itemId) {
		return itens.stream()
            .filter(i -> i.getId().equals(itemId))
            .findFirst();
	}

    public Item addItem(Item item) {
        this.itens.add(item);
        return item;
    }

    public BigDecimal calcularLucroTotal() {
        return itens.stream()
                .map(item -> item.getPrecoUnitarioAtual().subtract(item.getPrecoFornecedorAtual())
                        .multiply(BigDecimal.valueOf(item.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}
