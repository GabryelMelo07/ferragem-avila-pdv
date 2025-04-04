package com.ferragem.avila.pdv.controller;

import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.ferragem.avila.pdv.dto.UserResponseDto;
import com.ferragem.avila.pdv.dto.auth.CreateUserDto;
import com.ferragem.avila.pdv.dto.auth.JweTokenDto;
import com.ferragem.avila.pdv.dto.auth.LoginRequestDto;
import com.ferragem.avila.pdv.dto.auth.LoginResponseDto;
import com.ferragem.avila.pdv.dto.auth.RefreshTokenRequestDto;
import com.ferragem.avila.pdv.dto.auth.ResetPasswordDto;
import com.ferragem.avila.pdv.dto.auth.SendEmailDto;
import com.ferragem.avila.pdv.exceptions.JweTokenException;
import com.ferragem.avila.pdv.exceptions.JwtTokenValidationException;
import com.ferragem.avila.pdv.model.Role;
import com.ferragem.avila.pdv.model.User;
import com.ferragem.avila.pdv.repository.RoleRepository;
import com.ferragem.avila.pdv.repository.UserRepository;
import com.ferragem.avila.pdv.service.EmailService;
import com.ferragem.avila.pdv.utils.EmailTemplate;
import com.ferragem.avila.pdv.utils.api_responses_examples.AuthResponses;
import com.ferragem.avila.pdv.utils.tokens.JweUtils;
import com.ferragem.avila.pdv.utils.tokens.JwtUtils;
import com.ferragem.avila.pdv.utils.tokens.RedisTokenUtils;
import com.nimbusds.jose.JOSEException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.mail.MessagingException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

	private final JwtUtils jwtUtils;
	private final JweUtils jweUtils;
	private final JwtDecoder jwtDecoder;
	private final RedisTokenUtils redisTokenUtils;
	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final BCryptPasswordEncoder passwordEncoder;
	private final EmailService emailService;

	@Value("${jwt.public.key}")
	private RSAPublicKey publicKey;

	@Value("${api.issuer}")
	private String issuer;

	@Value("${front-end.url}")
	private String frontEndUrl;

	public AuthController(
			JwtUtils jwtUtils,
			JweUtils jweUtils,
			JwtDecoder jwtDecoder,
			RedisTokenUtils revokeTokenUtils, UserRepository userRepository,
			RoleRepository roleRepository, BCryptPasswordEncoder passwordEncoder,
			EmailService emailService) {
		this.jwtUtils = jwtUtils;
		this.jweUtils = jweUtils;
		this.jwtDecoder = jwtDecoder;
		this.redisTokenUtils = revokeTokenUtils;
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.roleRepository = roleRepository;
		this.emailService = emailService;
	}

	@Operation(summary = "Cadastrar novo usuário", description = "Este recurso só pode ser usado por usuários administradores e realiza o cadastro de um novo usuário")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Usuário cadastrado com sucesso", content = @Content(mediaType = "application/json")),
			@ApiResponse(responseCode = "422", description = "Nome de usuário já existente no banco de dados", content = @Content(mediaType = "application/json")),
	})
	@Transactional
	@PostMapping("/register")
	@PreAuthorize("hasAuthority('SCOPE_ADMIN')")
	public ResponseEntity<Void> register(@RequestBody CreateUserDto dto) {
		var userFromDb = userRepository.findByUsername(dto.username());

		if (userFromDb.isPresent())
			throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
					"Nome de usuário já existente no banco de dados");

		Set<Role> roles = new HashSet<>();

		for (String roleStr : dto.roles()) {
			var role = roleRepository.findByNameIgnoreCase(roleStr);

			if (role != null)
				roles.add(role);
		}

		var user = new User(dto.username(), passwordEncoder.encode(dto.password()), dto.email(), dto.nome(),
				dto.sobrenome(), roles);

		userRepository.save(user);
		return ResponseEntity.status(201).build();
	}

	@Operation(summary = "Listar usuários", description = "Este recurso só pode ser usado por usuários administradores e lista todos usuários cadastrados no banco de dados")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = AuthResponses.LISTAR_USUARIOS)))
	})
	@GetMapping("/users")
	@PreAuthorize("hasAuthority('SCOPE_ADMIN')")
	public ResponseEntity<List<UserResponseDto>> listUsers() {
		var usersFromDb = userRepository.findAll();
		var users = new ArrayList<UserResponseDto>();

		for (User user : usersFromDb) {
			users.add(new UserResponseDto(user.getId(), user.getUsername(), user.getEmail(), user.getNome(),
					user.getSobrenome(), user.getRoles()));
		}

		return ResponseEntity.ok(users);
	}

	@Operation(summary = "Logar usuário", description = "Este recurso realiza o login de um usuário devolvendo 2 tokens, um de acesso e outro para pegar outro token quando o de acesso expirar")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = AuthResponses.LOGIN_SUCESSO))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json")),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = AuthResponses.LOGIN_FALHA))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json"))
	})
	@PostMapping("/login")
	public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto loginRequest) {
		var user = userRepository.findByUsername(loginRequest.username());

		if (user.isEmpty() || !user.get().isPasswordCorrect(loginRequest.password(), passwordEncoder))
			throw new BadCredentialsException("Nome de usuário ou senha inválidos.");

		User usuario = user.get();
		String userId = usuario.getId().toString();

		var accessToken = jwtUtils.buildJwtAccessToken(issuer, userId, usuario);
		var refreshToken = jwtUtils.buildJwtAccessToken(issuer, userId, usuario);

		return ResponseEntity.ok(new LoginResponseDto(accessToken, refreshToken));
	}

	@Operation(summary = "Solicitar troca de senha", description = "Este recurso cria uma solicitação de troca de senha e envia para o e-mail do usuário um link com um token no parâmetro da URL, para identificar e validar a troca de senha")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json")),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json")),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = AuthResponses.RESET_PASSWORD_USER_NOT_FOUND))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json"))
	})
	@PostMapping("/reset-password/request")
	public ResponseEntity<Void> resetPasswordRequest(@RequestParam @Email String email) {
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

		String userId = user.getId().toString();
		String operationToken = UUID.randomUUID().toString();
		Instant expiresAt = Instant.now().plus(1, ChronoUnit.HOURS);
		String token;

		try {
			token = jweUtils.generateJWE(issuer, userId, operationToken, expiresAt);
		} catch (JOSEException e) {
			throw new JweTokenException("Erro ao gerar o token JWE para a operação de redefinição de senha");
		}
		
		String resetPassUrl = String.format("%s/reset-password?token=%s", frontEndUrl, token);
		String body = new EmailTemplate().getResetPasswordTemplate(user.getNome(), resetPassUrl);

		try {
			emailService.sendEmailAsync(new SendEmailDto(email, "Solicitação de redefinição de senha - PDV Ferragem Ávila", body));
		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}

		redisTokenUtils.storeOperationToken(userId, operationToken, expiresAt);
		return ResponseEntity.ok().build();
	}

	@Operation(summary = "Redefinir senha", description = "Este recurso redefine a senha do usuário a partir de um token previamente gerado por ele")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json")),
			@ApiResponse(responseCode = "404", description = "Not Found - Usuário não encontrado com o username: admin", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = AuthResponses.RESET_PASSWORD_USER_NOT_FOUND))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json")),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json"))
	})
	@PostMapping("/reset-password")
	public ResponseEntity<Void> resetPassword(@RequestParam String token, @RequestBody @Valid ResetPasswordDto dto) {
		JweTokenDto decryptedToken;

		try {
			decryptedToken = jweUtils.decryptJWE(token);
		} catch (JOSEException | ParseException e) {
			throw new JweTokenException("Erro ao decriptar o token");
		}

		User user = userRepository.findById(UUID.fromString(decryptedToken.subject()))
				.orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));
		String userId = user.getId().toString();

		if (!decryptedToken.issuer().equals(issuer))
			throw new JweTokenException();
		if (!decryptedToken.subject().equals(userId))
			throw new JweTokenException();
		
		String operationToken = redisTokenUtils.getJweStoredToken(userId).orElseThrow(() -> new JweTokenException());
		
		if (!decryptedToken.operationToken().equals(operationToken))
			throw new JweTokenException();
			
		user.setPassword(passwordEncoder.encode(dto.newPassword()));
		userRepository.save(user);

		redisTokenUtils.deleteOperationToken(userId);
		return ResponseEntity.ok().build();
	}

	@Operation(summary = "Atualizar token de acesso", description = "Este recurso renova o 'access_token' e o 'refresh_token' permitindo que o usuário fique logado por mais tempo na aplicação")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Tokens renovados com sucesso", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = AuthResponses.LOGIN_SUCESSO))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json")),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json"))
	})
	@PostMapping("/refresh-token")
	public ResponseEntity<LoginResponseDto> refreshToken(@RequestBody @Valid RefreshTokenRequestDto dto) {
		Jwt accessToken = jwtDecoder.decode(dto.accessToken());
		Jwt refreshToken = jwtDecoder.decode(dto.refreshToken());

		if (!refreshToken.getIssuer().equals(accessToken.getIssuer()))
			throw new JwtTokenValidationException("Erro ao validar refresh token: Issuer diferente.");
		if (!refreshToken.getSubject().equals(accessToken.getSubject()))
			throw new JwtTokenValidationException("Erro ao validar refresh token: Subject diferente.");
		if (!refreshToken.getIssuedAt().equals(accessToken.getIssuedAt()))
			throw new JwtTokenValidationException("Erro ao validar refresh token: Issued at diferente.");

		User usuario = userRepository.findById(UUID.fromString(accessToken.getSubject()))
				.orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado."));
		String userId = usuario.getId().toString();

		var newAccessToken = jwtUtils.buildJwtAccessToken(issuer, userId, usuario);
		var newRefreshToken = jwtUtils.buildJwtAccessToken(issuer, userId, usuario);

		// Revoking old tokens
		redisTokenUtils.revokeToken(accessToken.getTokenValue(), accessToken.getExpiresAt());
		redisTokenUtils.revokeToken(refreshToken.getTokenValue(), refreshToken.getExpiresAt());

		return ResponseEntity.ok(new LoginResponseDto(newAccessToken, newRefreshToken));
	}

}
