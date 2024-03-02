package com.ferragem.avila.pdv.model.enums;

import lombok.Getter;

@Getter
public enum UnidadeMedida {
    QUANTIDADE("quantidade"),
    CENTIMETRO("centimetro"),
    METRO("metro"),
    GRAMA("grama");

    private String unidadeMedida;

    UnidadeMedida(String unidadeMedida) {
        this.unidadeMedida = unidadeMedida;
    }
}
