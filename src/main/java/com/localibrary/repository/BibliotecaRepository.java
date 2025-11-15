package com.localibrary.repository;

import com.localibrary.entity.Biblioteca;
import com.localibrary.enums.StatusBiblioteca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository para operações de banco de dados com a entidade Biblioteca.
 * Contém queries customizadas para atender aos requisitos do sistema.
 */
@Repository
public interface BibliotecaRepository extends JpaRepository<Biblioteca, Long> {

    /**
     * Busca biblioteca por CNPJ
     */
    Optional<Biblioteca> findByCnpj(String cnpj);

    /**
     * Busca bibliotecas por status
     */
    List<Biblioteca> findByStatus(StatusBiblioteca status);

    /**
     * Busca bibliotecas ativas na cidade de São Paulo (RN-09, RN-13)
     */
    @Query("SELECT b FROM Biblioteca b " +
            "WHERE b.status = 'ATIVO' " +
            "AND b.endereco.cidade = 'São Paulo'")
    List<Biblioteca> findBibliotecasAtivasEmSaoPaulo();

    /**
     * Busca bibliotecas que possuem determinado livro e estão ativas (RF-06)
     */
    @Query("SELECT DISTINCT b FROM Biblioteca b " +
            "JOIN b.livros bl " +
            "WHERE bl.livroBase.id = :idLivro " +
            "AND b.status = 'ATIVO' " +
            "AND b.endereco.cidade = 'São Paulo'")
    List<Biblioteca> findByLivroAndStatusAtivo(@Param("idLivro") Long idLivro);

    /**
     * Verifica se existe biblioteca com o CNPJ informado (exceto a própria)
     */
    boolean existsByCnpjAndIdNot(String cnpj, Long id);
}