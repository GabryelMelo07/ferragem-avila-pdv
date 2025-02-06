package com.ferragem.avila.pdv.dto.auth;

import java.util.Set;

public record CreateUserDto(String username, String password, String email, String nome, String sobrenome,
        Set<String> roles) {
}
