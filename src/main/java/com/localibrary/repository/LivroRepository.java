package com.localibrary.repository;

import com.localibrary.entity.Livro;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository para operações com o catálogo global de livros.
 */
@Repository
public interface LivroRepository extends JpaRepository<Livro, Long> {

    /**
     * Busca livro por ISBN
     */
    Optional<Livro> findByIsbn(String isbn);

    /**
     * Busca livros por título (RF-02)
     * Busca parcial, insensível a maiúsculas/minúsculas
     */
    @Query("SELECT l FROM Livro l " +
            "WHERE LOWER(l.titulo) LIKE LOWER(CONCAT('%', :titulo, '%'))")
    Page<Livro> searchByTitulo(@Param("titulo") String titulo, Pageable pageable);

    /**
     * Busca livros mais populares (RF-03)
     * Livros com maior quantidade total cadastrada nas bibliotecas
     */
    @Query("SELECT bl.livro " +
            "FROM BibliotecaLivro bl " +
            "WHERE bl.biblioteca.status = 'ATIVO' " +
            "GROUP BY bl.livro " +
            "ORDER BY SUM(bl.quantidade) DESC")
    Page<Livro> findLivrosPopulares(Pageable pageable);

    /**
     * Busca livros similares por gênero (para recomendações - RF-05)
     */
    @Query("SELECT DISTINCT l FROM Livro l " +
            "JOIN l.generos lg " +
            "WHERE lg.genero.id IN " +
            "(SELECT lg2.genero.id FROM LivroGenero lg2 WHERE lg2.livro.id = :idLivro) " +
            "AND l.id != :idLivro")
    Page<Livro> findLivrosSimilares(@Param("idLivro") Long idLivro, Pageable pageable);

    /**
     * Verifica se ISBN já está cadastrado
     */
    boolean existsByIsbn(String isbn);
}