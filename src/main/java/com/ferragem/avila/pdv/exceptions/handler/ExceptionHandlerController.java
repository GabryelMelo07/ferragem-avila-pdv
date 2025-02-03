package com.ferragem.avila.pdv.exceptions.handler;

import java.io.IOException;
import java.net.ConnectException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

import org.springframework.core.env.Environment;
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
import com.ferragem.avila.pdv.exceptions.CodigoBarrasInvalidoException;
import com.ferragem.avila.pdv.exceptions.JwtTokenValidationException;
import com.ferragem.avila.pdv.exceptions.ProdutoNaoEncontradoException;
import com.ferragem.avila.pdv.exceptions.ProdutoSemEstoqueException;
import com.ferragem.avila.pdv.exceptions.VendaNotFoundException;
import com.ferragem.avila.pdv.exceptions.XlsxSizeLimitException;
import com.ferragem.avila.pdv.utils.RestErrorMessage;

import jakarta.mail.MessagingException;
import jakarta.persistence.EntityNotFoundException;

@RestControllerAdvice
public class ExceptionHandlerController extends ResponseEntityExceptionHandler {

	private final Environment environment;

	public ExceptionHandlerController(Environment environment) {
		this.environment = environment;
	}

	private boolean isDev() {
		String activeProfile = environment.getProperty("spring.profiles.active", "default");
		return "dev".equalsIgnoreCase(activeProfile);
	}

	private ResponseEntity<RestErrorMessage> buildResponse(HttpStatus status, String message, Throwable throwable) {
		if (isDev() && throwable != null) {
			throwable.printStackTrace();
		}
		
        RestErrorMessage response = new RestErrorMessage(LocalDateTime.now(ZoneId.of("America/Sao_Paulo")), status,
                message);
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(response);
    }

	private ResponseEntity<RestErrorMessage> buildResponse(HttpStatus status, String detailedError, String error, Throwable throwable) {
		if (isDev() && throwable != null) {
			throwable.printStackTrace();
		}
		
		RestErrorMessage response = new RestErrorMessage(LocalDateTime.now(ZoneId.of("America/Sao_Paulo")), status,
				detailedError, error);
		return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(response);
	}

	@ExceptionHandler(ProdutoNaoEncontradoException.class)
	private ResponseEntity<RestErrorMessage> produtoNotFoundHandler(ProdutoNaoEncontradoException exception) {
		return buildResponse(HttpStatus.NOT_FOUND, exception.getMessage(), exception);
	}

	@ExceptionHandler(ProdutoSemEstoqueException.class)
	private ResponseEntity<RestErrorMessage> produtoNotFoundHandler(ProdutoSemEstoqueException exception) {
		return buildResponse(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
	}

	@ExceptionHandler(CodigoBarrasInvalidoException.class)
	private ResponseEntity<RestErrorMessage> produtoNotFoundHandler(CodigoBarrasInvalidoException exception) {
		return buildResponse(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
	}

	@ExceptionHandler(VendaNotFoundException.class)
	private ResponseEntity<RestErrorMessage> produtoNotFoundHandler(VendaNotFoundException exception) {
		return buildResponse(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
	}

	@ExceptionHandler(AuthorizationDeniedException.class)
	private ResponseEntity<RestErrorMessage> authorizationDeniedHandler(AuthorizationDeniedException exception) {
		return buildResponse(HttpStatus.UNAUTHORIZED, exception.getMessage(), exception);
	}

	@ExceptionHandler(JwtTokenValidationException.class)
	private ResponseEntity<RestErrorMessage> jwtTokenValidationExceptionHandler(JwtTokenValidationException exception) {
		return buildResponse(HttpStatus.UNAUTHORIZED, exception.getMessage(), exception);
	}

	@ExceptionHandler(BadCredentialsException.class)
	private ResponseEntity<RestErrorMessage> badCredentialsExceptionHandler(BadCredentialsException exception) {
		return buildResponse(HttpStatus.UNAUTHORIZED, exception.getMessage(), exception);
	}

	@ExceptionHandler(EntityNotFoundException.class)
	private ResponseEntity<RestErrorMessage> entityNotFoundExceptionHandler(EntityNotFoundException exception) {
		return buildResponse(HttpStatus.NOT_FOUND, exception.getMessage(), exception);
	}

	@ExceptionHandler(XlsxSizeLimitException.class)
	private ResponseEntity<RestErrorMessage> xlsxSizeLimitExceptionHandler(XlsxSizeLimitException exception) {
		return buildResponse(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
	}

	@ExceptionHandler(MessagingException.class)
	private ResponseEntity<RestErrorMessage> messagingExceptionHandler(MessagingException exception) {
		return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage(), exception);
	}

	@ExceptionHandler(JsonMappingException.class)
	private ResponseEntity<RestErrorMessage> jsonMappingExceptionHandler(JsonMappingException exception) {
		return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage(), exception);
	}

	@ExceptionHandler(JsonProcessingException.class)
	private ResponseEntity<RestErrorMessage> jsonProcessingExceptionHandler(JsonProcessingException exception) {
		return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage(), exception);
	}

	@ExceptionHandler(ConnectException.class)
	private ResponseEntity<RestErrorMessage> connectExceptionHandler(ConnectException exception) {
		String mensagemDetalhada = "Falha ao tentar se conectar ao serviço de armazenamento de arquivos (Object Storage S3 Like). Verifique a disponibilidade do serviço.";
		return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, mensagemDetalhada, exception.getMessage(), exception);
	}

	@ExceptionHandler(IOException.class)
	private ResponseEntity<RestErrorMessage> genericIoExceptionHandler(IOException exception) {
		return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage(), exception);
	}

	@ExceptionHandler(Exception.class)
	private ResponseEntity<RestErrorMessage> genericExceptionHandler(Exception exception) {
		return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage(), exception);
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException exception,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {

		Map<String, String> errors = new HashMap<>();
		exception.getBindingResult().getFieldErrors().forEach(error -> {
			errors.put(error.getField(), error.getDefaultMessage());
		});

		return ResponseEntity.badRequest().body(buildResponse(HttpStatus.BAD_REQUEST, errors.toString(), exception));
	}

}
