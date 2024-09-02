package com.ferragem.avila.pdv.dto;

import java.util.Set;
import java.util.UUID;

import com.ferragem.avila.pdv.model.Role;

public record UserResponseDto(UUID id, String username, String email, String nome, String sobrenome, Set<Role> roles) {
}
