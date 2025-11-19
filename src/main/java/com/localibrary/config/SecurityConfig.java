package com.localibrary.config;

import com.localibrary.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

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
                // Desabilitar CSRF (não é necessário para APIs REST stateless)
                .csrf(csrf -> csrf.disable())
                .cors(org.springframework.security.config.Customizer.withDefaults())

                // Configurar gerenciamento de sessão como STATELESS (sem sessão)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Adicionar nosso filtro JWT antes do filtro padrão
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // Definir regras de autorização
                .authorizeHttpRequests(authorize -> authorize
                        // Rotas públicas (Auth e Consultas Públicas)
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll() // RF-09, RF-15
                        .requestMatchers(HttpMethod.POST, "/auth/cadastro").permitAll() // RF-08
                        .requestMatchers(HttpMethod.GET, "/").permitAll() // RF-01
                        .requestMatchers(HttpMethod.GET, "/livros").permitAll() // RF-02
                        .requestMatchers(HttpMethod.GET, "/livros/populares").permitAll() // RF-03
                        .requestMatchers(HttpMethod.GET, "/bibliotecas").permitAll() // RF-04
                        .requestMatchers(HttpMethod.GET, "/livros/{id_livro}").permitAll() // RF-05
                        .requestMatchers(HttpMethod.GET, "/livros/{id_livro}/bibliotecas").permitAll() // RF-06
                        .requestMatchers(HttpMethod.GET, "/bibliotecas/{id_biblioteca}").permitAll() // RF-07

                        // Rotas de Biblioteca (ROLE_BIBLIOTECA)
                        .requestMatchers(HttpMethod.GET, "/bibliotecas/{id_biblioteca}/profile").hasRole("BIBLIOTECA") // RF-13
                        .requestMatchers(HttpMethod.PUT, "/bibliotecas/{id_biblioteca}").hasRole("BIBLIOTECA") // RF-14
                        .requestMatchers(HttpMethod.GET, "/bibliotecas/{id_biblioteca}/livros").hasRole("BIBLIOTECA") // RF-10
                        .requestMatchers(HttpMethod.POST, "/bibliotecas/{id_biblioteca}/livros").hasRole("BIBLIOTECA") // RF-11
                        .requestMatchers(HttpMethod.DELETE, "/bibliotecas/{id_biblioteca}/livros/{id_livro}").hasRole("BIBLIOTECA") // RF-12
                        .requestMatchers(HttpMethod.PATCH, "/bibliotecas/{id_biblioteca}/livros/{id_livro}").hasRole("BIBLIOTECA") // Opcional

                        // Permitir ver as imagens (público)
                        .requestMatchers(HttpMethod.GET, "/uploads/**").permitAll()
                        // Permitir fazer upload (apenas autenticado)
                        .requestMatchers(HttpMethod.POST, "/uploads").authenticated()

                        // Rotas de Admin (ROLE_ADMIN, ROLE_MODERADOR)
                        // (Serão implementadas nas Sprints 2 e 5)
                        .requestMatchers("/admin/moderadores/**").hasRole("ADMIN") // RF-22 a RF-25
                        .requestMatchers("/admin/bibliotecas/**").hasAnyRole("ADMIN", "MODERADOR") // RF-17 a RF-21
                        .requestMatchers("/admin/dashboard").hasAnyRole("ADMIN", "MODERADOR") // RF-16

                        // Qualquer outra requisição deve ser autenticada
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}