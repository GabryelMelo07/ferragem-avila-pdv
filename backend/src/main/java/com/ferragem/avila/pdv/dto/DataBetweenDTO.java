package com.ferragem.avila.pdv.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.PastOrPresent;

public record DataBetweenDto(@PastOrPresent LocalDate dataHoraInicio, @PastOrPresent LocalDate dataHoraFim) {
}
