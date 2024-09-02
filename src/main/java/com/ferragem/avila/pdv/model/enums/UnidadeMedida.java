package com.ferragem.avila.pdv.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum UnidadeMedida {
    UNIDADE("Unidade"),
    METRO("Metro"),
    GRAMA("Grama");

    @JsonValue
    private String unidadeMedida;

    UnidadeMedida(String unidadeMedida) {
        this.unidadeMedida = unidadeMedida;
    }
}
