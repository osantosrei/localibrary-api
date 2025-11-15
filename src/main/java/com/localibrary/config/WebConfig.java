package com.localibrary.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuração geral do Spring MVC.
 * Define handlers de recursos estáticos (uploads de imagens).
 */
@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    /**
     * Configura o diretório de uploads para servir arquivos estáticos.
     * Permite acesso público às fotos de bibliotecas e autores.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Mapeia /uploads/** para o diretório físico uploads/
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");

        // Swagger UI (será configurado na Sprint 2)
        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/springdoc-openapi-ui/");
    }
}