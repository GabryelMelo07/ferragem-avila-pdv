package com.ferragem.avila.pdv.utils;

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
    private String detailedError;
    private String error;

    public RestErrorMessage(LocalDateTime timestamp, HttpStatus status, String error) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
    }
}