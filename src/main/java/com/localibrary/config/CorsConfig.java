package com.localibrary.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.Collections;

/**
 * Configuração de CORS (Cross-Origin Resource Sharing).
 * Permite que o frontend acesse a API de origens diferentes.
 *
 * Em produção, configure origens específicas ao invés de "*".
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // Permite credenciais (cookies, headers de autenticação)
        config.setAllowCredentials(true);

        // Origens permitidas
        // Em desenvolvimento: permite qualquer origem
        // Em produção: especifique as origens exatas (ex: "https://meusite.com")
        config.setAllowedOriginPatterns(Collections.singletonList("*"));

        // Headers permitidos
        config.setAllowedHeaders(Arrays.asList(
                "Origin",
                "Content-Type",
                "Accept",
                "Authorization",
                "X-Requested-With"
        ));

        // Métodos HTTP permitidos
        config.setAllowedMethods(Arrays.asList(
                "GET",
                "POST",
                "PUT",
                "PATCH",
                "DELETE",
                "OPTIONS"
        ));

        // Headers expostos ao cliente
        config.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type"
        ));

        // Tempo de cache da configuração CORS (em segundos)
        config.setMaxAge(3600L);

        // Aplica a configuração para todas as rotas
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}