package com.localibrary.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.stream.Collectors;

import static com.localibrary.util.Constants.JWT_CLAIM_ID;
import static com.localibrary.util.Constants.JWT_CLAIM_ROLE;

@Service
public class JwtTokenService {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    // O token terá 24h de validade, conforme RNF-09 (configurável)
    private final long jwtExpirationMs = 86400000; // 24 horas

    // Chave secreta para assinar o token
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    // Método para gerar o token
    public String generateToken(Authentication authentication) {
        // O "principal" é o UserDetails que criaremos
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        // Pega as roles (ex: "ROLE_ADMIN", "ROLE_BIBLIOTECA")
        String roles = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        // Adiciona claims personalizados: id, email e roles
        return Jwts.builder()
                .setSubject(userPrincipal.getUsername())
                .claim(JWT_CLAIM_ID, userPrincipal.getId())     // Usa constante "id"
                .claim(JWT_CLAIM_ROLE, roles)                   // Usa constante "role"
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Método para extrair todos os "claims" (dados) do token
    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Método para validar o token
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            // Logar a exceção (JwtException, ExpiredJwtException, etc.)
            return false;
        }
    }
}