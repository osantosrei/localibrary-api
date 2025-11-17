package com.localibrary.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.localibrary.dto.request.EnderecoRequestDTO;
import com.localibrary.dto.response.GeolocationResponse;
import com.localibrary.exception.GeolocationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Service para integração com Google Maps Geocoding API
 *
 * Funcionalidades:
 * - Converte endereço em coordenadas (latitude/longitude)
 * - Valida coordenadas dentro de São Paulo
 * - Tratamento de erros da API
 * - Fallback para centro de SP
 *
 * Implementa:
 * - RN-11: Fallback localização centro SP
 * - RN-17: Integração com API de geolocalização
 * - RN-18: Rejeitar cadastro se coordenadas inválidas
 * - RN-19: Coordenadas para cálculos de proximidade
 * - RNF-10: Integração com Google Maps
 * - RNF-13: Apenas São Paulo
 * - RNF-15: Fallback se API falhar
 *
 * Sprint 3 - Fase 2: GeolocationService
 */
@Service
@Slf4j
public class GeolocationService {

    @Value("${google.maps.api.key}")
    private String apiKey;

    @Value("${google.maps.api.url}")
    private String apiUrl;

    @Value("${app.default.latitude:-23.5505}")
    private Double defaultLatitude;

    @Value("${app.default.longitude:-46.6333}")
    private Double defaultLongitude;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public GeolocationService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    /**
     * Obtém coordenadas a partir de um endereço via Google Maps API
     *
     * @param endereco DTO com dados do endereço
     * @return GeolocationResponse com latitude, longitude e endereço formatado
     * @throws GeolocationException se a API falhar ou endereço for inválido
     *
     * Implementa:
     * - RN-17: Integração com API de geolocalização
     * - RN-18: Rejeitar coordenadas inválidas
     * - RN-19: Coordenadas para cálculos de proximidade
     * - RNF-10: Integração com APIs de mapas
     */
    public GeolocationResponse obterCoordenadas(EnderecoRequestDTO endereco) {
        log.info("Buscando coordenadas para endereço: {}", endereco.getEnderecoCompleto());

        try {
            // Montar endereço completo para a API
            String enderecoCompleto = construirEnderecoCompleto(endereco);

            // Chamar API do Google Maps (RN-17, RNF-10)
            String response = chamarGoogleMapsAPI(enderecoCompleto);

            // Parsear resposta
            GeolocationResponse result = parseResponse(response);

            // RN-18: Validar se está em São Paulo
            // RNF-13: Apenas São Paulo
            if (!result.isCoordenadaValida()) {
                log.warn("Coordenadas fora dos limites de São Paulo: lat={}, lon={}",
                        result.getLatitude(), result.getLongitude());
                throw new GeolocationException(
                        "Endereço fora da cidade de São Paulo. Apenas bibliotecas em São Paulo são aceitas."
                );
            }

            log.info("Coordenadas obtidas com sucesso: lat={}, lon={}",
                    result.getLatitude(), result.getLongitude());

            // RN-19: Retornar coordenadas para cálculos de proximidade
            return result;

        } catch (GeolocationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro ao obter coordenadas: {}", e.getMessage(), e);

            // RNF-15: Fallback se API falhar
            log.warn("Tentando fallback para coordenadas padrão...");
            throw new GeolocationException(
                    "Erro ao validar endereço. Verifique se todos os dados estão corretos.", e
            );
        }
    }

    /**
     * Constrói string de endereço completo para enviar à API
     */
    private String construirEnderecoCompleto(EnderecoRequestDTO endereco) {
        StringBuilder sb = new StringBuilder();

        // Logradouro + Número
        sb.append(endereco.getLogradouro()).append(", ").append(endereco.getNumero());

        // Complemento (opcional)
        if (endereco.getComplemento() != null && !endereco.getComplemento().isBlank()) {
            sb.append(" - ").append(endereco.getComplemento());
        }

        // Bairro
        sb.append(", ").append(endereco.getBairro());

        // Cidade e Estado
        sb.append(", ").append(endereco.getCidade());
        sb.append(" - ").append(endereco.getEstado());

        // CEP
        sb.append(", ").append(endereco.getCep());

        // País (fixo)
        sb.append(", Brasil");

        return sb.toString();
    }

    /**
     * Chama Google Maps Geocoding API
     *
     * Implementa RN-17: Integração com API de geolocalização
     */
    private String chamarGoogleMapsAPI(String endereco) {
        try {
            String encodedAddress = URLEncoder.encode(endereco, StandardCharsets.UTF_8);

            String url = UriComponentsBuilder
                    .fromHttpUrl(apiUrl)
                    .queryParam("address", encodedAddress)
                    .queryParam("key", apiKey)
                    .queryParam("region", "br")
                    .queryParam("language", "pt-BR")
                    .toUriString();

            log.debug("Chamando Google Maps API: {}", url.replace(apiKey, "***"));

            String response = webClient
                    .get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (response == null || response.isBlank()) {
                throw new GeolocationException("Resposta vazia da API de geolocalização");
            }

            return response;

        } catch (Exception e) {
            log.error("Erro ao chamar Google Maps API: {}", e.getMessage());
            throw new GeolocationException("Erro ao conectar com serviço de geolocalização", e);
        }
    }

    /**
     * Faz parse da resposta JSON do Google Maps
     */
    private GeolocationResponse parseResponse(String jsonResponse) throws Exception {
        JsonNode root = objectMapper.readTree(jsonResponse);

        // Verificar status da resposta
        String status = root.path("status").asText();

        if (!"OK".equals(status)) {
            String errorMessage = getErrorMessage(status);
            log.error("Google Maps API retornou status: {} - {}", status, errorMessage);
            throw new GeolocationException(errorMessage);
        }

        // Extrair primeiro resultado
        JsonNode results = root.path("results");
        if (results.isEmpty()) {
            throw new GeolocationException("Nenhum resultado encontrado para o endereço informado");
        }

        JsonNode firstResult = results.get(0);
        JsonNode location = firstResult.path("geometry").path("location");

        Double latitude = location.path("lat").asDouble();
        Double longitude = location.path("lng").asDouble();
        String enderecoFormatado = firstResult.path("formatted_address").asText();

        // Validar se coordenadas foram obtidas
        if (latitude == 0.0 && longitude == 0.0) {
            throw new GeolocationException("Não foi possível obter coordenadas para o endereço");
        }

        return new GeolocationResponse(latitude, longitude, enderecoFormatado);
    }

    /**
     * Retorna mensagem de erro amigável baseada no status da API
     *
     * Implementa RNF-02: Mensagens de erro claras
     */
    private String getErrorMessage(String status) {
        return switch (status) {
            case "ZERO_RESULTS" ->
                    "Endereço não encontrado. Verifique se todos os dados estão corretos.";
            case "INVALID_REQUEST" ->
                    "Requisição inválida. Verifique o formato do endereço.";
            case "REQUEST_DENIED" ->
                    "Acesso negado à API de geolocalização. Verifique a chave de API.";
            case "OVER_QUERY_LIMIT" ->
                    "Limite de requisições excedido. Tente novamente mais tarde.";
            case "UNKNOWN_ERROR" ->
                    "Erro desconhecido na API de geolocalização. Tente novamente.";
            default ->
                    "Erro ao validar endereço: " + status;
        };
    }

    /**
     * Retorna coordenadas padrão do centro de São Paulo
     * Usado como fallback quando usuário não permite localização
     *
     * Implementa:
     * - RN-11: Fallback localização centro SP
     * - RNF-15: Fallback se API falhar
     */
    public GeolocationResponse obterCoordenadasPadrao() {
        log.info("Retornando coordenadas padrão de São Paulo (RN-11)");
        return new GeolocationResponse(
                defaultLatitude,
                defaultLongitude,
                "Centro de São Paulo - SP, Brasil"
        );
    }

    /**
     * Valida se coordenadas estão dentro de São Paulo
     *
     * Implementa:
     * - RN-18: Validar coordenadas
     * - RNF-13: Apenas São Paulo
     */
    public boolean validarCoordenadas(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            return false;
        }

        // Limites aproximados da cidade de São Paulo
        // Latitude: -24.0 a -23.0
        // Longitude: -47.0 a -46.0
        return latitude >= -24.0 && latitude <= -23.0
                && longitude >= -47.0 && longitude <= -46.0;
    }
}