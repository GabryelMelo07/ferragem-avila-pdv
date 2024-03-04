package com.ferragem.avila.pdv.model.enums;

import lombok.Getter;

@Getter
public enum UnidadeMedida {
    UNIDADE("unidade"),
    METRO("metro"),
    GRAMA("grama");

    private String unidadeMedida;

    UnidadeMedida(String unidadeMedida) {
        this.unidadeMedida = unidadeMedida;
    }
}
