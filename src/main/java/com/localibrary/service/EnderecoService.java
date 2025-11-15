package com.localibrary.service;

import com.localibrary.dto.request.EnderecoRequestDTO;
import com.localibrary.entity.Endereco;
import com.localibrary.exception.BusinessException;
import com.localibrary.repository.EnderecoRepository;
import com.localibrary.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Serviço para operações com endereços.
 *
 * RN-12: Cada biblioteca tem exatamente um endereço
 * RN-17: Integração com API de geolocalização (Sprint 3)
 * RN-18: Validação de coordenadas
 */
@Service
public class EnderecoService {

    private static final Logger logger = LoggerFactory.getLogger(EnderecoService.class);

    @Autowired
    private EnderecoRepository enderecoRepository;

    // TODO Sprint 3: Injetar GeolocationService quando implementado
    // @Autowired
    // private GeolocationService geolocationService;

    /**
     * Cria entidade Endereco a partir do DTO.
     *
     * Na Sprint 2: coordenadas mockadas (centro de São Paulo)
     * Na Sprint 3: buscar coordenadas via API Google Maps
     *
     * @param dto DTO com dados do endereço
     * @return Endereco criado
     * @throws BusinessException se endereço inválido
     */
    public Endereco criarEndereco(EnderecoRequestDTO dto) {

        // 1. Validar dados básicos
        validarEndereco(dto);

        // 2. Criar entidade
        Endereco endereco = new Endereco();
        endereco.setCep(ValidationUtil.sanitizeCEP(dto.getCep()));
        endereco.setLogradouro(dto.getLogradouro());
        endereco.setNumero(dto.getNumero());
        endereco.setComplemento(dto.getComplemento());
        endereco.setBairro(dto.getBairro());
        endereco.setCidade(dto.getCidade());
        endereco.setEstado(dto.getEstado());

        // 3. Buscar coordenadas
        // TODO Sprint 3: Chamar API de geolocalização
        // CoordenadasDTO coordenadas = geolocationService.buscarCoordenadas(dto);
        // endereco.setLatitude(coordenadas.getLatitude());
        // endereco.setLongitude(coordenadas.getLongitude());

        // MOCK Sprint 2: Coordenadas do centro de São Paulo
        logger.warn("MOCK: Usando coordenadas padrão de São Paulo. " +
                "Implementar GeolocationService na Sprint 3!");
        endereco.setLatitude(new BigDecimal("-23.5505"));
        endereco.setLongitude(new BigDecimal("-46.6333"));

        // 4. Salvar
        return enderecoRepository.save(endereco);
    }

    /**
     * Atualiza endereço existente.
     *
     * @param endereco Endereco a atualizar
     * @param dto DTO com novos dados
     * @return Endereco atualizado
     */
    public Endereco atualizarEndereco(Endereco endereco, EnderecoRequestDTO dto) {

        // 1. Validar dados
        validarEndereco(dto);

        // 2. Atualizar campos
        endereco.setCep(ValidationUtil.sanitizeCEP(dto.getCep()));
        endereco.setLogradouro(dto.getLogradouro());
        endereco.setNumero(dto.getNumero());
        endereco.setComplemento(dto.getComplemento());
        endereco.setBairro(dto.getBairro());
        endereco.setCidade(dto.getCidade());
        endereco.setEstado(dto.getEstado());

        // 3. Recalcular coordenadas se endereço mudou
        // TODO Sprint 3: Chamar API de geolocalização
        logger.warn("MOCK: Mantendo coordenadas anteriores. " +
                "Implementar recálculo na Sprint 3!");

        // 4. Salvar
        return enderecoRepository.save(endereco);
    }

    /**
     * Valida dados do endereço.
     *
     * @param dto DTO a validar
     * @throws BusinessException se dados inválidos
     */
    private void validarEndereco(EnderecoRequestDTO dto) {

        // Validar CEP
        if (!ValidationUtil.isValidCEP(dto.getCep())) {
            throw new BusinessException("CEP inválido: " + dto.getCep());
        }

        // Validar que está em São Paulo (RN-13)
        if (!"São Paulo".equalsIgnoreCase(dto.getCidade())) {
            throw new BusinessException(
                    "Sistema aceita apenas bibliotecas da cidade de São Paulo. " +
                            "Cidade informada: " + dto.getCidade()
            );
        }

        // Validações adicionais podem ser feitas aqui
        // Ex: verificar se estado é "SP"
        if (!"SP".equalsIgnoreCase(dto.getEstado()) &&
                !"São Paulo".equalsIgnoreCase(dto.getEstado())) {
            throw new BusinessException("Estado deve ser São Paulo (SP)");
        }
    }
}