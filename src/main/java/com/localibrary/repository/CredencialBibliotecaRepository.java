package com.localibrary.repository;

import com.localibrary.entity.CredencialBiblioteca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository para operações com credenciais de bibliotecas.
 */
@Repository
public interface CredencialBibliotecaRepository extends JpaRepository<CredencialBiblioteca, Long> {

    /**
     * Busca credencial por email
     */
    Optional<CredencialBiblioteca> findByEmail(String email);

    /**
     * Verifica se email já está cadastrado
     */
    boolean existsByEmail(String email);

    /**
     * Busca credencial pela biblioteca
     */
    Optional<CredencialBiblioteca> findByBibliotecaId(Long bibliotecaId);
}