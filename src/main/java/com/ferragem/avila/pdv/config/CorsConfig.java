package com.ferragem.avila.pdv.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**")
				.allowedOrigins("http://localhost:8080", "http://localhost:8081", "http://localhost:3000",
						"https://api.ferragemavila.com.br", "https://ferragemavila.com.br")
				.allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE");
	}
}
