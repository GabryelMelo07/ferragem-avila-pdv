package com.ferragem.avila.pdv.dto;

import java.time.LocalDateTime;

import com.ferragem.avila.pdv.model.enums.FormaPagamento;

import jakarta.validation.constraints.FutureOrPresent;

public record VendaDto(@FutureOrPresent LocalDateTime dataHoraConclusao, FormaPagamento formaPagamento) {
}
