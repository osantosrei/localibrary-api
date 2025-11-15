package com.localibrary.repository;

import com.localibrary.entity.BibliotecaLivro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository para o relacionamento Biblioteca-Livro.
 */
@Repository
public interface BibliotecaLivroRepository extends JpaRepository<BibliotecaLivro, BibliotecaLivro.BibliotecaLivroId> {

    /**
     * Lista livros de uma biblioteca específica (RF-10)
     */
    @Query("SELECT bl FROM BibliotecaLivro bl " +
            "WHERE bl.biblioteca.id = :idBiblioteca")
    List<BibliotecaLivro> findByBibliotecaId(@Param("idBiblioteca") Long idBiblioteca);

    /**
     * Busca relacionamento específico entre biblioteca e livro
     */
    Optional<BibliotecaLivro> findByBibliotecaIdAndLivroBaseId(Long bibliotecaId, Long livroBaseId);

    /**
     * Verifica se livro já está cadastrado na biblioteca
     */
    boolean existsByBibliotecaIdAndLivroBaseId(Long bibliotecaId, Long livroBaseId);

    /**
     * Remove associação entre biblioteca e livro (RF-12)
     */
    void deleteByBibliotecaIdAndLivroBaseId(Long bibliotecaId, Long livroBaseId);
}