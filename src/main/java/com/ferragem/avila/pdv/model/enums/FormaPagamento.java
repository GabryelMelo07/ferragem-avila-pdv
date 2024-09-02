package com.ferragem.avila.pdv.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum FormaPagamento {
    PIX("Pix"),
    DEBITO("Débito"),
    CREDITO("Crédito"),
    DINHEIRO("Dinheiro");

    @JsonValue
    private String formaPagamento;

    FormaPagamento(String formaPagamento) {
        this.formaPagamento = formaPagamento;
    }
}
