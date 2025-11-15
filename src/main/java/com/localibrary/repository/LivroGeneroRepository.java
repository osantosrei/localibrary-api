package com.localibrary.repository;

import com.localibrary.entity.LivroGenero;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository para o relacionamento Livro-Gênero.
 */
@Repository
public interface LivroGeneroRepository extends JpaRepository<LivroGenero, LivroGenero.LivroGeneroId> {

    /**
     * Lista gêneros de um livro específico
     */
    @Query("SELECT lg FROM LivroGenero lg WHERE lg.livroBase.id = :idLivro")
    List<LivroGenero> findByLivroBaseId(@Param("idLivro") Long idLivro);

    /**
     * Remove todos os gêneros de um livro (útil para atualização)
     */
    void deleteByLivroBaseId(Long idLivro);
}