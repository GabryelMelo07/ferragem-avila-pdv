package com.ferragem.avila.pdv.model.utils;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RestErrorMessage {
    private LocalDateTime timestamp;
    private HttpStatus status;
    private String error;
}