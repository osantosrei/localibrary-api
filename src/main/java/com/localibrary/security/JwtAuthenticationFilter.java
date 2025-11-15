package com.localibrary.security;

import com.localibrary.util.Constants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro de autenticação JWT.
 * Intercepta todas as requisições HTTP e valida o token JWT no header Authorization.
 *
 * Fluxo:
 * 1. Extrai token do header Authorization
 * 2. Valida token
 * 3. Se válido, cria Authentication e adiciona ao SecurityContext
 * 4. Passa requisição adiante (chain.doFilter)
 *
 * OncePerRequestFilter: garante que o filtro é executado apenas uma vez por requisição.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtTokenProvider tokenProvider;

    /**
     * Método principal do filtro.
     * Executado para cada requisição HTTP.
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
            // 1. Extrair token JWT do header
            String jwt = getJwtFromRequest(request);

            // 2. Validar e autenticar
            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {

                // 3. Criar Authentication a partir do token
                Authentication authentication = tokenProvider.getAuthentication(jwt);

                // 4. Adicionar ao SecurityContext (disponível para toda aplicação)
                SecurityContextHolder.getContext().setAuthentication(authentication);

                logger.debug("Usuário autenticado: {}", authentication.getName());
            }

        } catch (Exception ex) {
            logger.error("Erro ao processar autenticação JWT: {}", ex.getMessage());
            // Não bloqueia requisição - apenas não autentica
            // Rotas protegidas retornarão 401/403 via Spring Security
        }

        // 5. Continuar chain de filtros
        filterChain.doFilter(request, response);
    }

    /**
     * Extrai token JWT do header Authorization.
     *
     * Header esperado: "Authorization: Bearer eyJhbGc..."
     *
     * @param request HttpServletRequest
     * @return Token JWT (sem o prefixo "Bearer ")
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(Constants.JWT_HEADER);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(Constants.JWT_PREFIX)) {
            return bearerToken.substring(Constants.JWT_PREFIX.length());
        }

        return null;
    }
}