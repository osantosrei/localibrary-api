package com.localibrary.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para resposta da API de Geolocalização
 * Usado para encapsular coordenadas retornadas pelo Google Maps API
 *
 * Implementa:
 * - RN-17: Integração com API de geolocalização
 * - RN-18: Validação de coordenadas
 * - RN-19: Coordenadas para cálculos de proximidade
 *
 * Sprint 3 - Fase 2: GeolocationService
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeolocationResponse {

    /**
     * Latitude do endereço
     * Exemplo: -23.5505 (São Paulo)
     */
    private Double latitude;

    /**
     * Longitude do endereço
     * Exemplo: -46.6333 (São Paulo)
     */
    private Double longitude;

    /**
     * Endereço formatado retornado pela API
     * Exemplo: "Av. Paulista, 1000 - Bela Vista, São Paulo - SP, 01310-100, Brasil"
     */
    private String enderecoFormatado;

    /**
     * Indica se a geolocalização foi bem-sucedida
     */
    private Boolean sucesso;

    /**
     * Mensagem de erro caso geolocalização falhe
     */
    private String mensagemErro;

    /**
     * Construtor para sucesso
     */
    public GeolocationResponse(Double latitude, Double longitude, String enderecoFormatado) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.enderecoFormatado = enderecoFormatado;
        this.sucesso = true;
        this.mensagemErro = null;
    }

    /**
     * Construtor para erro
     */
    public static GeolocationResponse erro(String mensagem) {
        GeolocationResponse response = new GeolocationResponse();
        response.setSucesso(false);
        response.setMensagemErro(mensagem);
        return response;
    }

    /**
     * Valida se as coordenadas estão dentro dos limites de São Paulo
     *
     * Implementa RN-18: Rejeitar coordenadas inválidas
     * RNF-13: Apenas São Paulo
     *
     * Limites aproximados da cidade de São Paulo:
     * - Latitude: -24.0 a -23.0
     * - Longitude: -47.0 a -46.0
     */
    public boolean isCoordenadaValida() {
        if (latitude == null || longitude == null) {
            return false;
        }

        return latitude >= -24.0 && latitude <= -23.0
                && longitude >= -47.0 && longitude <= -46.0;
    }
}