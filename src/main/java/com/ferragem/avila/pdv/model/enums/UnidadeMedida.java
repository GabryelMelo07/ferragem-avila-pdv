package com.ferragem.avila.pdv.model.enums;

// import com.fasterxml.jackson.annotation.JsonCreator;
// import com.fasterxml.jackson.annotation.JsonValue;

public enum UnidadeMedida {
    UNIDADE,
    METRO,
    GRAMA;
    // UNIDADE("Unidade"),
    // METRO("Metro"),
    // GRAMA("Grama");

    // @JsonValue
    // private String unidadeMedida;

    // UnidadeMedida(String unidadeMedida) {
    //     this.unidadeMedida = unidadeMedida;
    // }

    // @JsonCreator
    // public static UnidadeMedida fromString(String unidadeMedida) {
    //     for (UnidadeMedida unidade : values()) {
    //         if (unidade.unidadeMedida.equalsIgnoreCase(unidadeMedida)) {
    //             return unidade;
    //         }
    //     }
    //     throw new IllegalArgumentException("Valor inválido para UnidadeMedida: " + unidadeMedida);
    // }
}
