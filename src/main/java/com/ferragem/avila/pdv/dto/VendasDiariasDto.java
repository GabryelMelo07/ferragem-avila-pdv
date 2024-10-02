package com.ferragem.avila.pdv.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record VendasDiariasDto(LocalDate data, BigDecimal totalVendas, BigDecimal totalLucro) {
}
