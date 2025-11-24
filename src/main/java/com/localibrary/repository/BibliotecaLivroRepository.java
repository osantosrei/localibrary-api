package com.localibrary.repository;

import com.localibrary.entity.BibliotecaLivro;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
     * Versão paginada para a listagem de acervo
     */
    @Query("SELECT bl FROM BibliotecaLivro bl " +
            "WHERE bl.biblioteca.id = :idBiblioteca")
    Page<BibliotecaLivro> findByBibliotecaId(@Param("idBiblioteca") Long idBiblioteca, Pageable pageable);

    /**
     * Busca relacionamento específico entre biblioteca e livro
     */
    Optional<BibliotecaLivro> findByBibliotecaIdAndLivroId(Long bibliotecaId, Long LivroId);

    /**
     * Verifica se livro já está cadastrado na biblioteca
     */
    boolean existsByBibliotecaIdAndLivroId(Long bibliotecaId, Long LivroId);

    /**
     * Remove associação entre biblioteca e livro (RF-12)
     */
    void deleteByBibliotecaIdAndLivroId(Long bibliotecaId, Long LivroId);

    /**
     * Soma total de exemplares em todo o sistema
     */
    @Query("SELECT SUM(bl.quantidade) FROM BibliotecaLivro bl")
    Long sumTotalExemplares();

}