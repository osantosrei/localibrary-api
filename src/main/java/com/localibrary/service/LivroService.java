package com.localibrary.service;

import com.localibrary.dto.*;
import com.localibrary.dto.request.AddLivroRequestDTO;
import com.localibrary.dto.response.LivroResponseDTO;
import com.localibrary.entity.*;
import com.localibrary.enums.StatusBiblioteca;
import com.localibrary.repository.*;
import com.localibrary.util.Constants;
import com.localibrary.util.DistanceCalculator;
import com.localibrary.util.SecurityUtil;
import com.localibrary.util.ValidationUtil;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class LivroService {

    @Autowired
    private LivroRepository livroRepository;

    @Autowired
    private BibliotecaRepository bibliotecaRepository;

    @Autowired
    private BibliotecaLivroRepository bibliotecaLivroRepository;

    @Autowired
    private GeneroRepository generoRepository;

    @Autowired
    private LivroGeneroRepository livroGeneroRepository;

    @Autowired
    private SecurityUtil securityUtil; // Helper para RN-01

    // ================================================================================
    // 🟢 MÉTODOS PÚBLICOS (BUSCA E VISUALIZAÇÃO - SPRINT 3)
    // ================================================================================

    /**
     * RF-02: Buscar livros por título (parcial, case-insensitive) COM PAGINAÇÃO
     */
    public Page<LivroResponseDTO> buscarLivrosPorTitulo(String titulo, Integer page, Integer size, String sortField, String sortDir) {
        int p = (page == null || page < 0) ? 0 : page;
        int s = (size == null || size <= 0) ? Constants.DEFAULT_PAGE_SIZE : Math.min(size, Constants.MAX_PAGE_SIZE);
        String sf = (sortField == null || sortField.isBlank()) ? Constants.DEFAULT_SORT_FIELD : sortField;
        String sd = (sortDir == null || (!sortDir.equalsIgnoreCase("ASC") && !sortDir.equalsIgnoreCase("DESC"))) ? Constants.DEFAULT_SORT_DIRECTION : sortDir.toUpperCase();
        org.springframework.data.domain.Sort sort = org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.fromString(sd), sf);
        Pageable pageable = PageRequest.of(p, s, sort);

        Page<Livro> resultPage = livroRepository.searchByTitulo(titulo, pageable);
        List<LivroResponseDTO> dtos = resultPage.getContent().stream()
                .map(LivroResponseDTO::new)
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, resultPage.getTotalElements());
    }

    /**
     * RF-03: Buscar Livros Populares
     * Usa Constants para definir o limite (ex: Top 10)
     */
    public List<LivroResponseDTO> buscarLivrosPopulares() {
        Pageable limit = PageRequest.of(0, Constants.LIMITE_LIVROS_POPULARES);

        return livroRepository.findLivrosPopulares(limit).stream()
                .map(LivroResponseDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * RF-05: Detalhes de um livro + Similares
     */
    public LivroDetalhesDTO buscarDetalhesDoLivro(Long id) {
        // 1. Busca o livro
        Livro livro = livroRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Constants.MSG_NAO_ENCONTRADO));

        // 2. Converte para DTO
        LivroDetalhesDTO detalhesDTO = new LivroDetalhesDTO(livro);

        // 3. Busca livros similares (mesmos gêneros) usando query otimizada
        Pageable limit = PageRequest.of(0, Constants.LIMITE_LIVROS_SIMILARES);
        List<Livro> similares = livroRepository.findLivrosSimilares(id, limit).getContent();

        detalhesDTO.setLivrosSimilares(similares);

        return detalhesDTO;
    }

    /**
     * RF-06 e RN-10: Listar bibliotecas que possuem o livro.
     * APLICANDO DISTANCE CALCULATOR: Se o usuário enviar lat/lon, ordenamos por proximidade.
     */
    public List<BibliotecaParaLivroDTO> buscarBibliotecasPorLivro(Long idLivro, Double userLat, Double userLon) {
        // 1. Busca bibliotecas usando a query que já filtra (Status ATIVO e Cidade SP)
        // Nota: Se a query do repo não filtrar status, filtre aqui com .filter()
        List<Biblioteca> bibliotecas = bibliotecaRepository.findByLivroAndStatusAtivo(idLivro);

        List<BibliotecaParaLivroDTO> dtos = bibliotecas.stream()
                // Garante consistência caso o repo traga algo errado
                .filter(b -> b.getStatus() == StatusBiblioteca.ATIVO)
                .map(BibliotecaParaLivroDTO::new)
                .collect(Collectors.toList());

        // 2. Lógica de Ordenação Geográfica (RN-10)
        if (ValidationUtil.isValidCoordinates(userLat, userLon)) {
            dtos.sort(Comparator.comparingDouble(dto ->
                    DistanceCalculator.calculateDistance(
                            userLat, userLon,
                            dto.getLatitude().doubleValue(),
                            dto.getLongitude().doubleValue()
                    )
            ));
        }

        return dtos;
    }

    // ================================================================================
    // 🔒 MÉTODOS PROTEGIDOS (GESTÃO DE ACERVO - SPRINT 4)
    // ================================================================================

    /**
     * RF-11: Adicionar livro ao acervo da biblioteca logada.
     * Inclui validações rigorosas com ValidationUtil.
     */
    @Transactional
    public LivroAcervoDTO addLivroToMyAcervo(Long idBiblioteca, AddLivroRequestDTO dto) {
        // RN-01: Verifica permissão de acesso
        securityUtil.checkHasPermission(idBiblioteca);

        Biblioteca biblioteca = bibliotecaRepository.findById(idBiblioteca)
                .orElseThrow(() -> new EntityNotFoundException(Constants.MSG_NAO_ENCONTRADO));

        // 1. Validações de Negócio (ValidationUtil)
        if (!ValidationUtil.isValidISBN(dto.getIsbn())) {
            throw new IllegalArgumentException(Constants.MSG_ISBN_INVALIDO);
        }

        // Valida ano para não permitir datas futuras absurdas
        if (dto.getAnoPublicacao() != null && !ValidationUtil.isValidAnoPublicacao(dto.getAnoPublicacao())) {
            throw new IllegalArgumentException("Ano de publicação inválido.");
        }

        // 2. Encontra ou Cria o livro no catálogo global
        Livro livro = livroRepository.findByIsbn(dto.getIsbn())
                .orElseGet(() -> createNewlivro(dto));

        // 3. Se for livro novo ou estiver sem gêneros, associa os gêneros enviados
        if (livro.getId() == null || livro.getGeneros().isEmpty()) {
            setGenerosForLivro(livro, dto.getGenerosIds());
        }

        // Salva o livro base (para garantir que tenha ID)
        Livro savedlivro = livroRepository.save(livro);

        if (bibliotecaLivroRepository.existsByBibliotecaIdAndLivroId(idBiblioteca, savedlivro.getId())) {
            throw new EntityExistsException("Este livro já existe no seu acervo. Use a atualização de quantidade.");
        }

        // 5. Cria a relação (Estoque)
        BibliotecaLivro newRelacao = new BibliotecaLivro();
        newRelacao.setBiblioteca(biblioteca);
        newRelacao.setLivro(savedlivro);
        newRelacao.setQuantidade(dto.getQuantidade());

        BibliotecaLivro savedRelacao = bibliotecaLivroRepository.save(newRelacao);

        return new LivroAcervoDTO(savedRelacao);
    }

    /**
     * RF-12: Remover livro do acervo
     */
    @Transactional
    public void removeLivroFromMyAcervo(Long idBiblioteca, Long idLivro) {
        securityUtil.checkHasPermission(idBiblioteca);

        if (!bibliotecaLivroRepository.existsByBibliotecaIdAndLivroId(idBiblioteca, idLivro)) {
            throw new EntityNotFoundException(Constants.MSG_NAO_ENCONTRADO);
        }

        bibliotecaLivroRepository.deleteByBibliotecaIdAndLivroId(idBiblioteca, idLivro);
    }

    /**
     * Opcional: Atualizar quantidade
     */
    @Transactional
    public LivroAcervoDTO updateQuantidadeLivro(Long idBiblioteca, Long idLivro, UpdateQuantidadeDTO dto) {
        securityUtil.checkHasPermission(idBiblioteca);

        BibliotecaLivro relacao = bibliotecaLivroRepository.findByBibliotecaIdAndLivroId(idBiblioteca, idLivro)
                .orElseThrow(() -> new EntityNotFoundException(Constants.MSG_NAO_ENCONTRADO));

        // Se quantidade for 0, remove o livro do acervo
        if (dto.getQuantidade() == 0) {
            bibliotecaLivroRepository.delete(relacao);
            return null;
        }

        relacao.setQuantidade(dto.getQuantidade());
        BibliotecaLivro savedRelacao = bibliotecaLivroRepository.save(relacao);
        return new LivroAcervoDTO(savedRelacao);
    }

    // ================================================================================
    // 🛠️ MÉTODOS AUXILIARES PRIVADOS
    // ================================================================================

    private Livro createNewlivro(AddLivroRequestDTO dto) {
        Livro livro = new Livro();
        livro.setIsbn(dto.getIsbn());
        livro.setTitulo(dto.getTitulo());
        livro.setAutor(dto.getAutor());
        livro.setEditora(dto.getEditora());
        livro.setAnoPublicacao(dto.getAnoPublicacao());
        livro.setCapa(dto.getCapa());
        livro.setResumo(dto.getResumo());
        return livro;
    }

    private void setGenerosForLivro(Livro livro, Set<Long> generosIds) {
        if (livro.getGeneros() == null) {
            livro.setGeneros(new java.util.ArrayList<>());
        }

        if (livro.getId() != null) {
            livroGeneroRepository.deleteByLivroId(livro.getId());
            livro.getGeneros().clear();
        }

        for (Long generoId : generosIds) {
            Genero genero = generoRepository.findById(generoId)
                    .orElseThrow(() -> new EntityNotFoundException("Gênero ID " + generoId + " não encontrado"));

            LivroGenero lg = new LivroGenero();
            lg.setLivro(livro);
            lg.setGenero(genero);

            livro.getGeneros().add(lg);
        }
    }
}