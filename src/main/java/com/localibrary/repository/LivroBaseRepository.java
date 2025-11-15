package com.localibrary.repository;

import com.localibrary.entity.LivroBase;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository para operações com o catálogo global de livros.
 */
@Repository
public interface LivroBaseRepository extends JpaRepository<LivroBase, Long> {

    /**
     * Busca livro por ISBN
     */
    Optional<LivroBase> findByIsbn(String isbn);

    /**
     * Busca livros por título (RF-02)
     * Busca parcial, insensível a maiúsculas/minúsculas
     */
    @Query("SELECT l FROM LivroBase l " +
            "WHERE LOWER(l.titulo) LIKE LOWER(CONCAT('%', :titulo, '%'))")
    List<LivroBase> searchByTitulo(@Param("titulo") String titulo);

    /**
     * Busca livros mais populares (RF-03)
     * Livros com maior quantidade total cadastrada nas bibliotecas
     */
    @Query("SELECT bl.livroBase " +
            "FROM BibliotecaLivro bl " +
            "WHERE bl.biblioteca.status = 'ATIVO' " +
            "GROUP BY bl.livroBase " +
            "ORDER BY SUM(bl.quantidade) DESC")
    List<LivroBase> findLivrosPopulares(Pageable pageable);

    /**
     * Busca livros similares por gênero (para recomendações - RF-05)
     */
    @Query("SELECT DISTINCT l FROM LivroBase l " +
            "JOIN l.generos lg " +
            "WHERE lg.genero.id IN " +
            "(SELECT lg2.genero.id FROM LivroGenero lg2 WHERE lg2.livroBase.id = :idLivro) " +
            "AND l.id != :idLivro")
    List<LivroBase> findLivrosSimilares(@Param("idLivro") Long idLivro, Pageable pageable);

    /**
     * Verifica se ISBN já está cadastrado
     */
    boolean existsByIsbn(String isbn);
}