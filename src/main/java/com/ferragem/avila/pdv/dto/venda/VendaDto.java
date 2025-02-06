package com.ferragem.avila.pdv.dto.venda;

import java.time.LocalDateTime;

import com.ferragem.avila.pdv.model.enums.FormaPagamento;

public record VendaDto(LocalDateTime dataHoraConclusao, FormaPagamento formaPagamento) {
}
