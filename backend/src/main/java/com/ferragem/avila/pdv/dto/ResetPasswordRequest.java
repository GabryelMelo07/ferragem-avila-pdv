package com.ferragem.avila.pdv.dto;

import jakarta.validation.constraints.Email;

public record ResetPasswordRequest(@Email String email) {
}
