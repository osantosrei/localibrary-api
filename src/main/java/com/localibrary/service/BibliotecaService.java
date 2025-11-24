package com.localibrary.service;

import com.localibrary.dto.*;
import com.localibrary.dto.request.AddLivroRequestDTO;
import com.localibrary.dto.response.BibliotecaResponseDTO;
import com.localibrary.entity.*;
import com.localibrary.enums.StatusBiblioteca;
import com.localibrary.repository.*;
import com.localibrary.util.Constants;
import com.localibrary.util.SecurityUtil;
import com.localibrary.util.ValidationUtil;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BibliotecaService {

    @Autowired
    private BibliotecaRepository bibliotecaRepository;

    @Autowired
    private SecurityUtil securityUtil; // Helper RN-01

    @Autowired
    private BibliotecaLivroRepository bibliotecaLivroRepository;

    @Autowired
    private LivroRepository livroRepository;

    @Autowired
    private GeneroRepository generoRepository; // Para validar os IDs de gênero

    @Autowired
    private LivroGeneroRepository livroGeneroRepository;

    @Autowired
    private GeolocationService geolocationService; // Da Sprint 2

    @Autowired
    private EnderecoRepository enderecoRepository;

    /**
     * RF-04: Exibir mapa com todas as bibliotecas ATIVAS em SP
     * (Usando sua query 'findBibliotecasAtivas')
     */
    public List<BibliotecaResponseDTO> listarBibliotecasAtivas() {
        return bibliotecaRepository.findBibliotecasAtivasEmSaoPaulo().stream()
                .map(BibliotecaResponseDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * RF-07: Detalhes de uma biblioteca
     * (Query 'findById' é padrão, mas verificamos o status)
     */
    public BibliotecaDetalhesDTO buscarDetalhesBiblioteca(Long id) {
        Biblioteca biblioteca = bibliotecaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Biblioteca não encontrada com id: " + id));

        // RN-02: Se a biblioteca não estiver ATIVA, não mostrar
        // (Sua query de RF-06 já faz isso, mas a de RF-07 não,
        // então mantemos a verificação no service).
        if (biblioteca.getStatus() != StatusBiblioteca.ATIVO) {
            throw new EntityNotFoundException("Biblioteca não disponível");
        }

        return new BibliotecaDetalhesDTO(biblioteca);
    }

    /**
     * RF-13: Exibir dados detalhados da biblioteca logada (para edição)
     */
    public BibliotecaDetalhesDTO getMyBibliotecaDetails(Long idBiblioteca) {
        // RN-01: Verifica se o ID da URL é o mesmo do token
        securityUtil.checkHasPermission(idBiblioteca);

        Biblioteca biblioteca = findBibliotecaById(idBiblioteca);

        // Diferente do RF-07, aqui podemos mostrar mesmo se PENDENTE
        return new BibliotecaDetalhesDTO(biblioteca);
    }

    /**
     * RF-14: Permitir a atualização dos dados de uma biblioteca
     * (RN-14: Re-valida endereço)
     */
    @Transactional
    public BibliotecaDetalhesDTO updateMyBiblioteca(Long idBiblioteca, UpdateBibliotecaDTO dto) {
        securityUtil.checkHasPermission(idBiblioteca);
        Biblioteca biblioteca = findBibliotecaById(idBiblioteca);

        // 1. Validações Prévias
        if (!ValidationUtil.isValidCEP(dto.getCep())) {
            throw new IllegalArgumentException(Constants.MSG_CEP_INVALIDO);
        }
        if (ValidationUtil.isNotEmpty(dto.getTelefone()) && !ValidationUtil.isValidTelefone(dto.getTelefone())) {
            throw new IllegalArgumentException(Constants.MSG_TELEFONE_INVALIDO);
        }

        // 2. Atualiza dados
        biblioteca.setNomeFantasia(dto.getNomeFantasia());
        biblioteca.setRazaoSocial(dto.getRazaoSocial());
        biblioteca.setTelefone(dto.getTelefone());
        biblioteca.setCategoria(dto.getCategoria());
        biblioteca.setSite(dto.getSite());
        biblioteca.setFotoBiblioteca(dto.getFotoBiblioteca());

        // 2. Re-valida Endereço (RN-14)
        Endereco endereco = biblioteca.getEndereco();
        endereco.setCep(dto.getCep());
        endereco.setLogradouro(dto.getLogradouro());
        endereco.setNumero(dto.getNumero());
        endereco.setComplemento(dto.getComplemento());
        endereco.setBairro(dto.getBairro());
        endereco.setCidade(dto.getCidade());
        endereco.setEstado(dto.getEstado());

        // 3. Chama Geolocation API (RN-17)
        Coordinates coords = geolocationService.getCoordinatesFromAddress(
                        dto.getCep(), dto.getLogradouro(), dto.getNumero(), dto.getCidade())
                .orElseThrow(() -> new IllegalArgumentException("Endereço inválido ou não encontrado."));

        // Validação extra de coordenadas
        if (!ValidationUtil.isValidCoordinates(coords.latitude().doubleValue(), coords.longitude().doubleValue())) {
            throw new IllegalArgumentException("Coordenadas inválidas.");
        }

        endereco.setLatitude(coords.latitude());
        endereco.setLongitude(coords.longitude());

        Biblioteca bibliotecaAtualizada = bibliotecaRepository.save(biblioteca);
        return new BibliotecaDetalhesDTO(bibliotecaAtualizada);
    }

    /**
     * RF-10: Listar todos os livros disponíveis em uma biblioteca específica
     */
    public Page<LivroAcervoDTO> listMyLivros(Long idBiblioteca, Integer page, Integer size, String sortField, String sortDir) {
        // RN-01: Verifica permissão
        securityUtil.checkHasPermission(idBiblioteca);

        int p = (page == null || page < 0) ? 0 : page;
        int s = (size == null || size <= 0) ? Constants.DEFAULT_PAGE_SIZE : Math.min(size, Constants.MAX_PAGE_SIZE);
        String sf = (sortField == null || sortField.isBlank()) ? Constants.DEFAULT_SORT_FIELD : sortField;
        String sd = (sortDir == null || (!sortDir.equalsIgnoreCase("ASC") && !sortDir.equalsIgnoreCase("DESC"))) ? Constants.DEFAULT_SORT_DIRECTION : sortDir.toUpperCase();
        org.springframework.data.domain.Sort sort = org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.fromString(sd), sf);
        Pageable pageable = PageRequest.of(p, s, sort);

        // Usa a query do repositório (findByBibliotecaId pageable)
        return bibliotecaLivroRepository.findByBibliotecaId(idBiblioteca, pageable)
                .map(LivroAcervoDTO::new);
    }

    /**
     * RF-11: Permitir que bibliotecas adicionem livros ao seu acervo
     */
    @Transactional
    public LivroAcervoDTO addLivroToMyAcervo(Long idBiblioteca, AddLivroRequestDTO dto) {
        // RN-01: Verifica permissão
        securityUtil.checkHasPermission(idBiblioteca);

        Biblioteca biblioteca = findBibliotecaById(idBiblioteca);

        // Validação Customizada de Ano (que @Min/@Max não pegam dinamicamente)
        if (dto.getAnoPublicacao() != null && !ValidationUtil.isValidAnoPublicacao(dto.getAnoPublicacao())) {
            throw new IllegalArgumentException("Ano de publicação inválido ou no futuro.");
        }

        // 1. Encontra ou Cria o livro
        Livro livro = livroRepository.findByIsbn(dto.getIsbn())
                .orElseGet(() -> createNewlivro(dto)); // Cria se não existir

        // 2. Valida e Seta Gêneros (só se for um livro novo)
        if (livro.getId() == null || livro.getGeneros().isEmpty()) {
            setGenerosForLivro(livro, dto.getGenerosIds());
        }

        // 3. Salva o livro (se for novo ou se os gêneros foram add)
        Livro savedlivro = livroRepository.save(livro);

        // 4. Cria a Relação (BibliotecaLivro)
        if (bibliotecaLivroRepository.existsByBibliotecaIdAndLivroId(idBiblioteca, savedlivro.getId())) {
            throw new EntityExistsException("Este livro já existe no seu acervo. Use o 'Atualizar Quantidade'.");
        }

        BibliotecaLivro newRelacao = new BibliotecaLivro();
        newRelacao.setBiblioteca(biblioteca);
        newRelacao.setLivro(savedlivro);
        newRelacao.setQuantidade(dto.getQuantidade());

        BibliotecaLivro savedRelacao = bibliotecaLivroRepository.save(newRelacao);

        return new LivroAcervoDTO(savedRelacao);
    }

    /**
     * RF-12: Permitir que bibliotecas removam livros de seu acervo
     */
    @Transactional
    public void removeLivroFromMyAcervo(Long idBiblioteca, Long idLivro) {
        // RN-01: Verifica permissão
        securityUtil.checkHasPermission(idBiblioteca);

        // Usa o método delete customizado do seu repositório
        if (!bibliotecaLivroRepository.existsByBibliotecaIdAndLivroId(idBiblioteca, idLivro)) {
            throw new EntityNotFoundException("Livro não encontrado no acervo desta biblioteca.");
        }

        bibliotecaLivroRepository.deleteByBibliotecaIdAndLivroId(idBiblioteca, idLivro);
    }

    /**
     * Atualiza a quantidade de um livro no acervo
     */
    @Transactional
    public LivroAcervoDTO updateQuantidadeLivro(Long idBiblioteca, Long idLivro, UpdateQuantidadeDTO dto) {
        // RN-01: Verifica permissão
        securityUtil.checkHasPermission(idBiblioteca);

        BibliotecaLivro relacao = bibliotecaLivroRepository.findByBibliotecaIdAndLivroId(idBiblioteca, idLivro)
                .orElseThrow(() -> new EntityNotFoundException("Livro não encontrado no acervo."));

        // Se quantidade for 0, remove (alternativa ao RF-12)
        if (dto.getQuantidade() == 0) {
            bibliotecaLivroRepository.delete(relacao);
            return null; // Retorna nulo para o controller enviar 204
        }

        relacao.setQuantidade(dto.getQuantidade());
        BibliotecaLivro savedRelacao = bibliotecaLivroRepository.save(relacao);
        return new LivroAcervoDTO(savedRelacao);
    }

    // --- Métodos Auxiliares Privados ---

    private Biblioteca findBibliotecaById(Long id) {
        return bibliotecaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Biblioteca não encontrada com id: " + id));
    }

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
        // (Garante que a lista de gêneros não é nula, baseado na sua entidade)
        if (livro.getGeneros() == null) {
            livro.setGeneros(new java.util.ArrayList<>());
        }

        // 1. Limpa gêneros antigos (se houver)
        livroGeneroRepository.deleteByLivroId(livro.getId()); // Usa a query do repo
        livro.getGeneros().clear();

        // 2. Busca os novos gêneros e adiciona
        for (Long generoId : generosIds) {
            Genero genero = generoRepository.findById(generoId)
                    .orElseThrow(() -> new EntityNotFoundException("Gênero com ID " + generoId + " não encontrado."));

            LivroGenero lgRelacao = new LivroGenero();
            lgRelacao.setLivro(livro);
            lgRelacao.setGenero(genero);

            livro.getGeneros().add(lgRelacao);
        }
    }
}
