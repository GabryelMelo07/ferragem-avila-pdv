package com.ferragem.avila.pdv.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record ItemDTO(@PositiveOrZero Integer quantidade, @Positive Long produtoId, @Positive Long vendaId) {
}
