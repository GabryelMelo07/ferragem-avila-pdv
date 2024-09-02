package com.ferragem.avila.pdv.dto;

import java.util.Set;

public record CreateUserDto(String username, String password, String email, String nome, String sobrenome,
        Set<String> roles) {
}
