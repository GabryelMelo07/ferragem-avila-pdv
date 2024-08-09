package com.ferragem.avila.pdv.dto;

import com.ferragem.avila.pdv.annotation.ValidString;

public record LoginRequestDto(@ValidString String username, @ValidString String password) {
}