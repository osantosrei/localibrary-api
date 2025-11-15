package com.localibrary.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuração principal do Spring Security.
 *
 * Define:
 * - Rotas públicas vs protegidas
 * - Autenticação JWT (stateless)
 * - AuthenticationManager
 * - Filtros customizados
 * - CORS e CSRF
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // Habilita @PreAuthorize
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Configura a cadeia de filtros de segurança.
     * Define quais rotas são públicas e quais requerem autenticação.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF desabilitado (API REST stateless com JWT)
                .csrf(csrf -> csrf.disable())

                // CORS (configurado em CorsConfig.java)
                .cors(cors -> {})

                // Sessão stateless (não mantém estado no servidor)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Autorização de requisições
                .authorizeHttpRequests(auth -> auth

                        // ===================================
                        // ROTAS PÚBLICAS (sem autenticação)
                        // ===================================

                        // Home
                        .requestMatchers(HttpMethod.GET, "/").permitAll()

                        // Autenticação
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/cadastro").permitAll()

                        // Livros (consulta pública - RF-02, RF-03, RF-05, RF-06)
                        .requestMatchers(HttpMethod.GET, "/livros").permitAll()
                        .requestMatchers(HttpMethod.GET, "/livros/populares").permitAll()
                        .requestMatchers(HttpMethod.GET, "/livros/{id}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/livros/{id}/bibliotecas").permitAll()

                        // Bibliotecas (consulta pública - RF-04, RF-07)
                        .requestMatchers(HttpMethod.GET, "/bibliotecas").permitAll()
                        .requestMatchers(HttpMethod.GET, "/bibliotecas/{id}").permitAll()

                        // Swagger UI (descomente quando implementar)
                        // .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // Uploads (imagens públicas)
                        .requestMatchers("/uploads/**").permitAll()

                        // ===================================
                        // ROTAS PROTEGIDAS - BIBLIOTECA
                        // ===================================
                        // Controle adicional via @PreAuthorize nos controllers
                        // RN-01: Biblioteca só acessa próprios dados (validado no service)

                        .requestMatchers("/bibliotecas/{id}/livros/**").hasRole("BIBLIOTECA")
                        .requestMatchers(HttpMethod.PUT, "/bibliotecas/{id}").hasRole("BIBLIOTECA")

                        // ===================================
                        // ROTAS PROTEGIDAS - ADMIN/MODERADOR
                        // ===================================
                        // RN-03: Apenas ADMIN/MODERADOR aprova bibliotecas
                        // RN-04: Apenas ADMIN gerencia moderadores

                        .requestMatchers("/admin/**").hasAnyRole("ADMIN", "MODERADOR")

                        // ===================================
                        // QUALQUER OUTRA ROTA
                        // ===================================
                        .anyRequest().authenticated()
                )

                // Adicionar filtro JWT antes do UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configura o AuthenticationManager.
     * Usado para autenticar usuários no login.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Configura o provedor de autenticação.
     * Liga UserDetailsService com PasswordEncoder.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);

        return authProvider;
    }
}