package com.ferragem.avila.pdv.dto;

import java.math.BigDecimal;

import com.ferragem.avila.pdv.model.enums.UnidadeMedida;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ProdutoDTO(
        @Size(max = 70, message = "Campo descrição deve ter no máximo 70 caracteres") @NotBlank @NotEmpty String descricao,
        UnidadeMedida unidadeMedida,
        @PositiveOrZero Float estoque, @Positive @Max(value = 999999) BigDecimal preco,
        @Size(max = 13, message = "Campo código de barras deve ter no máximo 13 caracteres, padrão EAN 13.") String codigoBarrasEAN13) {
}
