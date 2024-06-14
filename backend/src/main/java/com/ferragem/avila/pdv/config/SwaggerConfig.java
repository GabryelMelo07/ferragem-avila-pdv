package com.ferragem.avila.pdv.config;

import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

@Configuration
@SecurityScheme(name = "Bearer Authentication", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Sistema PDV - Ferragem Ávila")
                        .version("v1")
                        .description("APIs REST para gerenciamento e monitoramento de vendas e controle de estoque.")
                        .termsOfService("")
                        .license(new License().name("MIT License")
                                .url("https://github.com/GabryelMelo07/ferragem-avila-pdv/blob/master/LICENSE")));
    }

    @Bean
    @Primary
    public SwaggerUiConfigProperties swaggerUiConfigProperties() {
        SwaggerUiConfigProperties swaggerUiConfig = new SwaggerUiConfigProperties();
        swaggerUiConfig.setDefaultModelExpandDepth(-1);
        swaggerUiConfig.setDocExpansion("none");
        return swaggerUiConfig;
    }

}
