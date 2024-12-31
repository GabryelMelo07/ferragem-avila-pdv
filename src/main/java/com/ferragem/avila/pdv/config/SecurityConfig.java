package com.ferragem.avila.pdv.config;

import com.ferragem.avila.pdv.validator.ExpiresAtValidator;
import com.ferragem.avila.pdv.validator.IssuedAtValidator;
import com.ferragem.avila.pdv.validator.ScopeValidator;
import com.ferragem.avila.pdv.validator.SubjectValidator;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${jwt.public.key}")
    private RSAPublicKey publicKey;

    @Value("${jwt.private.key}")
    private RSAPrivateKey privateKey;

    @Value("${api.issuer}")
    private String issuer;

    @Autowired
    private SubjectValidator subjectValidator;

    @Autowired
    private ScopeValidator scopeValidator;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.authorizeHttpRequests(authorize -> authorize
                    .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                    .requestMatchers(HttpMethod.POST, "/auth/reset-password").permitAll()
                    .requestMatchers(HttpMethod.POST, "/auth/reset-password/request").permitAll()
                    .requestMatchers(HttpMethod.POST, "/auth/refresh-token").permitAll()
                    .requestMatchers(HttpMethod.GET, "/health-check").permitAll()
                    .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                    .anyRequest().authenticated())
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:8080", "https://api.ferragemavila.com.br", "https://ferragemavila.com.br"));
        configuration.setAllowedMethods(Arrays.asList("*"));
        configuration.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    JwtDecoder jwtDecoder() {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withPublicKey(publicKey).build();

        // Default Validators
        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuer);
        OAuth2TokenValidator<Jwt> withDefaultValidators = JwtValidators.createDefault();

        // Custom Validators
        OAuth2TokenValidator<Jwt> withIssuedAtValidator = new IssuedAtValidator();
        OAuth2TokenValidator<Jwt> withExpiresAtValidator = new ExpiresAtValidator();
        OAuth2TokenValidator<Jwt> withScopeValidator = scopeValidator;
        OAuth2TokenValidator<Jwt> withSubjectValidator = subjectValidator;

        OAuth2TokenValidator<Jwt> combinedValidators = new DelegatingOAuth2TokenValidator<>(withIssuer,
                withDefaultValidators, withIssuedAtValidator, withExpiresAtValidator, withScopeValidator,
                withSubjectValidator);
        
        jwtDecoder.setJwtValidator(combinedValidators);
        
        return jwtDecoder;
    }

    @Bean
    JwtEncoder jwtEncoder() {
        JWK jwk = new RSAKey.Builder(this.publicKey).privateKey(privateKey).build();
        var jwks = new ImmutableJWKSet<>(new JWKSet(jwk));
        return new NimbusJwtEncoder(jwks);
    }

    @Bean
    BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

}