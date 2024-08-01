package com.ferragem.avila.pdv.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record ItemDto(@PositiveOrZero @NotNull Float quantidade, @Positive Long produtoId) {
}
