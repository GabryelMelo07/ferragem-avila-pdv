package com.ferragem.avila.pdv.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record GraficoVendasDto(LocalDate data, BigDecimal totalVendas, BigDecimal totalLucro) {
}
