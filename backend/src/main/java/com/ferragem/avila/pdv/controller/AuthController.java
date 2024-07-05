package com.ferragem.avila.pdv.controller;

import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.ferragem.avila.pdv.dto.CreateUserDto;
import com.ferragem.avila.pdv.dto.LoginRequestDto;
import com.ferragem.avila.pdv.dto.LoginResponseDto;
import com.ferragem.avila.pdv.dto.ResetPassword;
import com.ferragem.avila.pdv.dto.ResetPasswordRequest;
import com.ferragem.avila.pdv.dto.SendEmailDto;
import com.ferragem.avila.pdv.dto.UserResponseDto;
import com.ferragem.avila.pdv.model.ResetPasswordToken;
import com.ferragem.avila.pdv.model.Role;
import com.ferragem.avila.pdv.model.User;
import com.ferragem.avila.pdv.repository.ResetPasswordRepository;
import com.ferragem.avila.pdv.repository.RoleRepository;
import com.ferragem.avila.pdv.repository.UserRepository;
import com.ferragem.avila.pdv.service.interfaces.EmailService;

import jakarta.mail.MessagingException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {
    
    private final JwtEncoder jwtEncoder;

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final ResetPasswordRepository resetPasswordRepository;

    private final BCryptPasswordEncoder passwordEncoder;

    private final EmailService emailService;

    @Value("${jwt.public.key}")
    private RSAPublicKey publicKey;
    
    @Value("${api.issuer}")
    private String issuer;

    @Value("${front-end.url}")
    private String frontEndUrl;
    
    public AuthController(JwtEncoder jwtEncoder, UserRepository userRepository, RoleRepository roleRepository,
            BCryptPasswordEncoder passwordEncoder, EmailService emailService,
            ResetPasswordRepository resetPasswordRepository) {
        this.jwtEncoder = jwtEncoder;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.emailService = emailService;
        this.resetPasswordRepository = resetPasswordRepository;
    }

    @Transactional
    @PostMapping("/register")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public ResponseEntity<Void> register(@RequestBody CreateUserDto dto) {
        var userFromDb = userRepository.findByUsername(dto.username());

        if (userFromDb.isPresent())
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY);

        Set<Role> roles = new HashSet<>();

        for (String roleStr : dto.roles()) {
            var role = roleRepository.findByNameIgnoreCase(roleStr);

            if (role != null)
                roles.add(role);
        }
            
        var user = new User(dto.username(), passwordEncoder.encode(dto.password()), dto.email(), dto.nome(), dto.sobrenome(),
                roles);

        userRepository.save(user);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/users")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public ResponseEntity<List<UserResponseDto>> listUsers() {
        var usersFromDb = userRepository.findAll();
        var users = new ArrayList<UserResponseDto>();
        
        for (User user : usersFromDb) {
            users.add(new UserResponseDto(user.getId(), user.getUsername(), user.getEmail(), user.getNome(), user.getSobrenome(), user.getRoles()));
        }
        
        return ResponseEntity.ok(users);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto loginRequest) {
        var user = userRepository.findByUsername(loginRequest.username());

        if (user.isEmpty() || !user.get().isPasswordCorrect(loginRequest.password(), passwordEncoder))
            throw new BadCredentialsException("Nome de usuário ou senha inválidos.");

        var now = Instant.now();
        var expires = now.plus(7, ChronoUnit.DAYS);

        var scopes = user.get().getRoles()
                .stream()
                .map(Role::getName)
                .collect(Collectors.joining(" "));

        var claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(user.get().getId().toString())
                .issuedAt(now)
                .expiresAt(expires)
                .claim("scope", scopes)
                .build();

        var jwtValue = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
        return ResponseEntity.ok(new LoginResponseDto(jwtValue, LocalDateTime.ofInstant(expires, ZoneId.systemDefault())));
    }

    @PostMapping("/reset-password/request")
    public ResponseEntity<Void> resetPasswordRequest(@RequestBody @Valid ResetPasswordRequest dto) {
        User user = userRepository.findByEmail(dto.email()).orElseThrow(() -> new EntityNotFoundException("User not found with email: " + dto.email()));

        UUID token = UUID.randomUUID();

        ResetPasswordToken resetPasswordToken = new ResetPasswordToken();
        resetPasswordToken.setToken(token);
        resetPasswordRepository.save(resetPasswordToken);

        user.setResetPasswordToken(resetPasswordToken);
        userRepository.save(user);

        String resetPassUrl = String.format("%s/auth/reset-password?userId=%s&token=%s", frontEndUrl, user.getId(), token);
        
        // TODO: Criar template de e-mail padronizado.
        String body = String.format("""
                <h1>Olá %s.</h1><br>
                Recebemos uma solicitação de redefinição de senha para sua conta, clique no link abaixo para redefinir:
                <br>
                <a href="%s"><button>Redefinir Senha</button></a><br>
                <small>Caso o botão não funcione, aperte neste link: %s</small>
                <br>
                <br>
                <h4>Caso não tenha sido você que solicitou a troca de senha, desconsidere e exclua este e-mail.</h4>
                """, user.getNome(), resetPassUrl, resetPassUrl);
        
        try {
            emailService.sendEmailAsync(new SendEmailDto(dto.email(), "Solicitação de redefinição de senha - PDV Ferragem Ávila", body));
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
        
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody @Valid ResetPassword dto) {
        User user = userRepository.findById(UUID.fromString(dto.userId())).orElseThrow(() -> new EntityNotFoundException("User not found."));

        if (!user.getResetPasswordToken().getToken().toString().equals(dto.token()))
            throw new RuntimeException("Token inválido.");

        user.setPassword(passwordEncoder.encode(dto.password()));
        user.setResetPasswordToken(null);
        userRepository.save(user);
        
        return ResponseEntity.ok().build();
    }
    
}
