package com.ferragem.avila.pdv.dto;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

import lombok.Data;

@Data
public class GraficoVendasDto {
    private LocalDate data;
    private List<VendasDiariasDto> vendasDiarias;

    public GraficoVendasDto(LocalDate data) {
        this.data = data;
        this.vendasDiarias = new ArrayList<>();
    }
}
