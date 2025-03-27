package com.ferragem.avila.pdv.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
public class SwaggerConfig {

	private final String activeProfile;

	public SwaggerConfig(@Value("${spring.profiles.active}") String activeProfile) {
		this.activeProfile = activeProfile;
	}
	
	@Bean
	OpenAPI customOpenApi() {
		return new OpenAPI()
				.servers(Arrays.asList(
						new Server().url("https://api.ferragemavila.com.br").description("Servidor de Produção"),
						new Server().url("http://localhost:8080").description("Servidor Local")))
				.components(new Components().addSecuritySchemes("bearerAuth",
						new io.swagger.v3.oas.models.security.SecurityScheme()
								.type(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP).scheme("bearer")
								.bearerFormat("JWT")))
				.addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
				.info(new Info()
						.title("Sistema PDV - Ferragem Ávila")
						.version("v1")
						.description("APIs REST para gerenciamento e monitoramento de vendas e controle de estoque.")
						.termsOfService("")
						.license(new License().name("MIT License")
								.url("https://github.com/GabryelMelo07/ferragem-avila-pdv/blob/master/LICENSE")));
	}

	@Bean
	OpenApiCustomizer globalApiResponsesCustomizer() {
		Set<String> endpointsPublicos = Set.of(
				"/auth/login",
				"/auth/reset-password",
				"/auth/reset-password/request",
				"/auth/refresh-token",
				"/swagger-ui/**",
				"/v3/api-docs/**");

		return openApi -> openApi.getPaths()
				.forEach((path, pathItem) -> pathItem.readOperations().forEach(operation -> {
					if (endpointsPublicos.contains(path)) {
						return;
					}

					ApiResponses responses = operation.getResponses();
					responses.addApiResponse("400", new ApiResponse().description("Bad Request")
							.content(new Content().addMediaType("application/json", new MediaType())));
					responses.addApiResponse("401",
							new ApiResponse().description("Unauthorized - Necessário autenticação")
									.content(new Content().addMediaType("application/json", new MediaType())));
					responses.addApiResponse("403",
							new ApiResponse().description("Forbidden - Sem permissão para acessar este recurso")
									.content(new Content().addMediaType("application/json", new MediaType())));
					responses.addApiResponse("500", new ApiResponse().description("Internal Server Error")
							.content(new Content().addMediaType("application/json", new MediaType())));
				}));
	}

	@Bean
	@Primary
	SwaggerUiConfigProperties swaggerUiConfigProperties() {
		SwaggerUiConfigProperties swaggerUiConfig = new SwaggerUiConfigProperties();
		swaggerUiConfig.setDefaultModelExpandDepth(-1);
		swaggerUiConfig.setDocExpansion("none");

		// if ("prd".equals(activeProfile)) {
		// 	swaggerUiConfig.setSupportedSubmitMethods(new ArrayList<>());
        // }
		
		return swaggerUiConfig;
	}

}
