package com.localibrary.exception;

/**
 * Exceção lançada quando há erro na integração com a API de geolocalização.
 * Retorna HTTP 500 (Internal Server Error) ou 400 (Bad Request) dependendo do caso.
 *
 * Exemplos de uso:
 * - Falha ao conectar com Google Maps API
 * - Endereço não encontrado pela API (RN-18)
 * - API retornou erro (limite de requisições, chave inválida, etc)
 * - Coordenadas inválidas retornadas pela API
 */
public class GeolocationException extends RuntimeException {

    private String address;
    private String apiError;

    /**
     * Construtor com mensagem simples
     */
    public GeolocationException(String message) {
        super(message);
    }

    /**
     * Construtor com mensagem e endereço que falhou
     */
    public GeolocationException(String message, String address) {
        super(message);
        this.address = address;
    }

    /**
     * Construtor com mensagem, endereço e erro da API
     */
    public GeolocationException(String message, String address, String apiError) {
        super(message);
        this.address = address;
        this.apiError = apiError;
    }

    /**
     * Construtor com mensagem e causa
     */
    public GeolocationException(String message, Throwable cause) {
        super(message, cause);
    }

    public String getAddress() {
        return address;
    }

    public String getApiError() {
        return apiError;
    }
}