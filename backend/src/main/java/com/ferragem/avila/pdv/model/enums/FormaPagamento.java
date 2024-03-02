package com.ferragem.avila.pdv.model.enums;

import lombok.Getter;

@Getter
public enum FormaPagamento {
    PIX("pix"),
    DEBITO("debito"),
    CREDITO("credito"),
    DINHEIRO("dinheiro");

    private String formaPagamento;

    FormaPagamento(String formaPagamento) {
        this.formaPagamento = formaPagamento;
    }
}
