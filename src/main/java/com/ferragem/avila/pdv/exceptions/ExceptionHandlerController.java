package com.ferragem.avila.pdv.exceptions;

import java.io.IOException;
import java.net.ConnectException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.ferragem.avila.pdv.utils.RestErrorMessage;

import jakarta.mail.MessagingException;
import jakarta.persistence.EntityNotFoundException;

@RestControllerAdvice
public class ExceptionHandlerController extends ResponseEntityExceptionHandler {

    private ResponseEntity<RestErrorMessage> buildResponse(HttpStatus status, String message) {
        RestErrorMessage response = new RestErrorMessage(LocalDateTime.now(ZoneId.of("America/Sao_Paulo")), status,
                message);
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(response);
    }

    private ResponseEntity<RestErrorMessage> buildResponse(HttpStatus status, String detailedError, String error) {
        RestErrorMessage response = new RestErrorMessage(LocalDateTime.now(ZoneId.of("America/Sao_Paulo")), status,
                detailedError, error);
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(response);
    }

    @ExceptionHandler(ProdutoNaoEncontradoException.class)
    private ResponseEntity<RestErrorMessage> produtoNotFoundHandler(ProdutoNaoEncontradoException exception) {
        return buildResponse(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(ProdutoSemEstoqueException.class)
    private ResponseEntity<RestErrorMessage> produtoNotFoundHandler(ProdutoSemEstoqueException exception) {
        return buildResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(CodigoBarrasInvalidoException.class)
    private ResponseEntity<RestErrorMessage> produtoNotFoundHandler(CodigoBarrasInvalidoException exception) {
        return buildResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(VendaNotFoundException.class)
    private ResponseEntity<RestErrorMessage> produtoNotFoundHandler(VendaNotFoundException exception) {
        return buildResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    private ResponseEntity<RestErrorMessage> authorizationDeniedHandler(AuthorizationDeniedException exception) {
        return buildResponse(HttpStatus.UNAUTHORIZED, exception.getMessage());
    }

    @ExceptionHandler(JwtTokenValidationException.class)
    private ResponseEntity<RestErrorMessage> jwtTokenValidationExceptionHandler(JwtTokenValidationException exception) {
        return buildResponse(HttpStatus.UNAUTHORIZED, exception.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    private ResponseEntity<RestErrorMessage> badCredentialsExceptionHandler(BadCredentialsException exception) {
        return buildResponse(HttpStatus.UNAUTHORIZED, exception.getMessage());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    private ResponseEntity<RestErrorMessage> entityNotFoundExceptionHandler(EntityNotFoundException exception) {
        return buildResponse(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(XlsxSizeLimitException.class)
    private ResponseEntity<RestErrorMessage> xlsxSizeLimitExceptionHandler(XlsxSizeLimitException exception) {
        return buildResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(MessagingException.class)
    private ResponseEntity<RestErrorMessage> messagingExceptionHandler(MessagingException exception) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
    }

    @ExceptionHandler(JsonMappingException.class)
    private ResponseEntity<RestErrorMessage> jsonMappingExceptionHandler(JsonMappingException exception) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
    }

    @ExceptionHandler(JsonProcessingException.class)
    private ResponseEntity<RestErrorMessage> jsonProcessingExceptionHandler(JsonProcessingException exception) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
    }

    @ExceptionHandler(ConnectException.class)
    private ResponseEntity<RestErrorMessage> connectExceptionHandler(ConnectException exception) {
        String mensagemDetalhada = "Falha ao tentar se conectar ao serviço de armazenamento de arquivos (Object Storage S3 Like). Verifique a disponibilidade do serviço.";
        return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, mensagemDetalhada, exception.getMessage());
    }

    @ExceptionHandler(IOException.class)
    private ResponseEntity<RestErrorMessage> genericIoExceptionHandler(IOException exception) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    private ResponseEntity<RestErrorMessage> genericExceptionHandler(Exception exception) {
        exception.printStackTrace();
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });

        return ResponseEntity.badRequest().body(buildResponse(HttpStatus.BAD_REQUEST, errors.toString()));
    }

}
