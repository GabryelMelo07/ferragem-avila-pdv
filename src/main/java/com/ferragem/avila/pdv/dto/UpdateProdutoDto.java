package com.ferragem.avila.pdv.dto;

import java.math.BigDecimal;

import org.springframework.web.multipart.MultipartFile;

import com.ferragem.avila.pdv.annotation.ValidString;
import com.ferragem.avila.pdv.model.enums.UnidadeMedida;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record UpdateProdutoDto(
        @ValidString @Size(max = 70, message = "Campo descrição deve ter no máximo 70 caracteres") String descricao,
        @NotNull UnidadeMedida unidadeMedida,
        @Positive @Max(value = 999999) BigDecimal precoFornecedor,
        @NotNull @Positive @Max(value = 999999) BigDecimal preco,
        @ValidString @Size(min = 13, max = 13, message = "Campo código de barras deve ter 13 caracteres, conforme padrão EAN 13.") String codigoBarrasEAN13,
        @Nullable MultipartFile imagem) {
}
