package com.localibrary.service;

import com.localibrary.dto.BibliotecaParaLivroDTO;
import com.localibrary.dto.LivroDetalhesDTO;
import com.localibrary.dto.response.LivroResponseDTO;
import com.localibrary.entity.Biblioteca;
import com.localibrary.entity.BibliotecaLivro;
import com.localibrary.entity.LivroBase;
import com.localibrary.enums.StatusBiblioteca;
import com.localibrary.repository.BibliotecaLivroRepository;
import com.localibrary.repository.BibliotecaRepository;
import com.localibrary.repository.LivroBaseRepository;
import com.localibrary.util.DistanceCalculator;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LivroService {

    @Autowired
    private LivroBaseRepository livroBaseRepository;

    @Autowired
    private BibliotecaRepository bibliotecaRepository; // ⬅️ Nova dependência (para RF-06)

    @Autowired
    private BibliotecaLivroRepository bibliotecaLivroRepository;

    /**
     * RF-06 e RN-10: Listar bibliotecas, ordenando por proximidade se o usuário informar localização.
     */
    public List<BibliotecaParaLivroDTO> buscarBibliotecasPorLivro(Long idLivro, Double userLat, Double userLon) {
        List<BibliotecaLivro> relacoes = bibliotecaLivroRepository.findByLivroBase_Id(idLivro);

        List<BibliotecaParaLivroDTO> dtos = relacoes.stream()
                .filter(rel -> rel.getBiblioteca().getStatus() == StatusBiblioteca.ATIVO)
                .map(rel -> new BibliotecaParaLivroDTO(rel.getBiblioteca())) // Removemos quantidade do DTO anterior, lembra?
                .collect(Collectors.toList());

        // LÓGICA DE ORDENAÇÃO (RN-10)
        if (userLat != null && userLon != null) {
            // Se o usuário enviou coordenadas, ordena do mais perto para o mais longe
            dtos.sort(Comparator.comparingDouble(dto ->
                    DistanceCalculator.calculateDistance(
                            userLat, userLon,
                            dto.getLatitude().doubleValue(),
                            dto.getLongitude().doubleValue()
                    )
            ));

            // Opcional: Calcular e setar a distância no DTO para mostrar "A 2.5km de você"
            // (Precisaria adicionar o campo 'distancia' no BibliotecaParaLivroDTO)
        }

        return dtos;
    }

    /**
     * RF-02: Buscar livros por título
     * (Usando sua query 'searchByTitulo')
     */
    public List<LivroResponseDTO> buscarLivrosPorTitulo(String titulo) {
        return livroBaseRepository.searchByTitulo(titulo).stream()
                .map(LivroResponseDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * RF-03: Livros Populares
     * (Usando sua query com Pageable)
     */
    public List<LivroResponseDTO> buscarLivrosPopulares() {
        // Define uma paginação (ex: Top 10)
        Pageable top10 = PageRequest.of(0, 10);
        return livroBaseRepository.findLivrosPopulares(top10).stream()
                .map(LivroResponseDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * RF-05: Detalhes de um livro
     * (Usando sua query 'findLivrosSimilares')
     */
    public LivroDetalhesDTO buscarDetalhesDoLivro(Long id) {
        // 1. Busca o livro principal
        LivroBase livro = livroBaseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Livro não encontrado com id: " + id));

        // 2. Converte para o DTO
        LivroDetalhesDTO detalhesDTO = new LivroDetalhesDTO(livro);

        // 3. Busca livros similares (ex: Top 5)
        Pageable top5 = PageRequest.of(0, 5);
        List<LivroBase> similares = livroBaseRepository.findLivrosSimilares(id, top5);

        // 4. Adiciona os similares ao DTO
        detalhesDTO.setLivrosSimilares(similares);

        return detalhesDTO;
    }

    /**
     * RF-06: Listar bibliotecas que possuem um livro
     * (Usando sua query 'findByLivroAndStatusAtivo')
     */
    public List<BibliotecaParaLivroDTO> buscarBibliotecasPorLivro(Long idLivro) {
        // Sua query otimizada já filtra ATIVO e São Paulo
        List<Biblioteca> bibliotecas = bibliotecaRepository.findByLivroAndStatusAtivo(idLivro);

        return bibliotecas.stream()
                .map(BibliotecaParaLivroDTO::new)
                .collect(Collectors.toList());
    }
}