package com.ferragem.avila.pdv.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record ItemDTO(@PositiveOrZero Float quantidade, @Positive Long produtoId) {
}
