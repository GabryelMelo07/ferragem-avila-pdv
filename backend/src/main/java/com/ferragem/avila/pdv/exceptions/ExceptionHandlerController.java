package com.ferragem.avila.pdv.exceptions;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.ferragem.avila.pdv.model.utils.RestErrorMessage;

@ControllerAdvice
public class ExceptionHandlerController extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ProdutoNaoEncontradoException.class)
    private ResponseEntity<RestErrorMessage> produtoNotFoundHandler(ProdutoNaoEncontradoException exception) {
        RestErrorMessage response = new RestErrorMessage(LocalDateTime.now().atOffset(ZoneOffset.ofHours(-3)).toLocalDateTime(), HttpStatus.NOT_FOUND, exception.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(ProdutoSemEstoqueException.class)
    private ResponseEntity<RestErrorMessage> produtoNotFoundHandler(ProdutoSemEstoqueException exception) {
        RestErrorMessage response = new RestErrorMessage(LocalDateTime.now().atOffset(ZoneOffset.ofHours(-3)).toLocalDateTime(), HttpStatus.BAD_REQUEST, exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(CodigoBarrasInvalidoException.class)
    private ResponseEntity<RestErrorMessage> produtoNotFoundHandler(CodigoBarrasInvalidoException exception) {
        RestErrorMessage response = new RestErrorMessage(LocalDateTime.now().atOffset(ZoneOffset.ofHours(-3)).toLocalDateTime(), HttpStatus.BAD_REQUEST, exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(VendaInativaException.class)
    private ResponseEntity<RestErrorMessage> produtoNotFoundHandler(VendaInativaException exception) {
        RestErrorMessage response = new RestErrorMessage(LocalDateTime.now().atOffset(ZoneOffset.ofHours(-3)).toLocalDateTime(), HttpStatus.BAD_REQUEST, exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    private ResponseEntity<RestErrorMessage> authorizationDeniedHandler(AuthorizationDeniedException exception) {
        RestErrorMessage response = new RestErrorMessage(LocalDateTime.now().atOffset(ZoneOffset.ofHours(-3)).toLocalDateTime(), HttpStatus.UNAUTHORIZED, exception.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(Exception.class)
    private ResponseEntity<RestErrorMessage> genericExceptionHandler(Exception exception) {
        RestErrorMessage response = new RestErrorMessage(LocalDateTime.now().atOffset(ZoneOffset.ofHours(-3)).toLocalDateTime(), HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(IOException.class)
    private ResponseEntity<RestErrorMessage> genericIoExceptionHandler(IOException exception) {
        RestErrorMessage response = new RestErrorMessage(LocalDateTime.now().atOffset(ZoneOffset.ofHours(-3)).toLocalDateTime(), HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
}
