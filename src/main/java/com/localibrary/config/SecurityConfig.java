package com.localibrary.config;

import com.localibrary.security.JwtAuthenticationFilter;
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

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

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

        // Em produção, troque "*" pelo domínio exato do seu frontend (ex: "http://meusite.com")
        config.setAllowCredentials(true);
        config.addAllowedOriginPattern("*");
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

                        // --- ROTAS DE BIBLIOTECA (ROLE_BIBLIOTECA) ---
                        .requestMatchers("/bibliotecas/{id_biblioteca}/**").hasRole("BIBLIOTECA")
                        // Nota: O /** acima já cobre profile, livros, put, etc. se a URL base for a mesma.
                        // Se quiser manter granular como você fez para segurança extra, mantenha sua lista:
                        // .requestMatchers(HttpMethod.GET, "/bibliotecas/{id_biblioteca}/profile").hasRole("BIBLIOTECA")
                        // ... etc ...

                        // --- ROTAS DE UPLOADS ---
                        .requestMatchers(HttpMethod.GET, "/uploads/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/uploads/**").authenticated()

                        // --- ROTAS ADMINISTRATIVAS ---

                        // 1. Exclusões (Alto Risco - Apenas ADMIN)
                        // RN-04 e RF-21 exigem controle estrito
                        .requestMatchers(HttpMethod.DELETE, "/admin/bibliotecas/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/admin/moderadores/{id}").hasRole("ADMIN")

                        // 2. Gestão de Moderadores (Apenas ADMIN)
                        // ESTA REGRA DEVE VIR ANTES DO GENÉRICO /admin/**
                        // Isso impede que Moderadores criem ou editem outros moderadores
                        .requestMatchers("/admin/moderadores/**").hasRole("ADMIN")

                        // 3. Dashboard e Gestão de Bibliotecas (ADMIN ou MODERADOR)
                        // Isso cobre: GET /admin/dashboard, GET /admin/bibliotecas, PATCH status
                        .requestMatchers("/admin/**").hasAnyRole("ADMIN", "MODERADOR")

                        // --- CATCH-ALL ---
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}