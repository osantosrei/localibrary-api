package com.localibrary.config;

import com.localibrary.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // ✅ CORREÇÃO CRÍTICA: CORS agora usa variável de ambiente
    @Value("${app.cors.allowed-origins:http://localhost:3000}")
    private String allowedOrigins;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // ✅ CORREÇÃO CRÍTICA: Agora usa origens específicas da variável de ambiente
        config.setAllowCredentials(true);

        // Divide as origens por vírgula e adiciona cada uma
        Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .forEach(config::addAllowedOrigin);

        config.addAllowedHeader("*");
        config.addAllowedMethod("*");

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Desabilitar CSRF e ativar CORS
                .csrf(csrf -> csrf.disable())
                .cors(org.springframework.security.config.Customizer.withDefaults())

                // Sessão Stateless
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Filtro JWT
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                .authorizeHttpRequests(authorize -> authorize
                        // --- ROTAS PÚBLICAS ---
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/cadastro").permitAll()
                        .requestMatchers(HttpMethod.GET, "/").permitAll()
                        .requestMatchers(HttpMethod.GET, "/livros/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/bibliotecas").permitAll()
                        .requestMatchers(HttpMethod.GET, "/bibliotecas/{id_biblioteca}").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        // --- ROTAS DE BIBLIOTECA (ROLE_BIBLIOTECA) ---
                        .requestMatchers("/bibliotecas/{id_biblioteca}/**").hasRole("BIBLIOTECA")

                        // --- ROTAS DE UPLOADS ---
                        .requestMatchers(HttpMethod.GET, "/uploads/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/uploads/**").authenticated()

                        // --- ROTAS ADMINISTRATIVAS ---

                        // 1. Exclusões (Alto Risco - Apenas ADMIN)
                        .requestMatchers(HttpMethod.DELETE, "/admin/bibliotecas/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/admin/moderadores/{id}").hasRole("ADMIN")

                        // 2. Gestão de Moderadores (Apenas ADMIN)
                        .requestMatchers("/admin/moderadores/**").hasRole("ADMIN")

                        // 3. Dashboard e Gestão de Bibliotecas (ADMIN ou MODERADOR)
                        .requestMatchers("/admin/**").hasAnyRole("ADMIN", "MODERADOR")

                        // --- CATCH-ALL ---
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}