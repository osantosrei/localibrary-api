package com.localibrary.service;

import com.localibrary.dto.google.GoogleGeocodingResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Optional;

// Record auxiliar para retorno interno
record Coordinates(BigDecimal latitude, BigDecimal longitude) {}

@Service
public class GeolocationService {

    @Value("${app.google.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public Optional<Coordinates> getCoordinatesFromAddress(String cep, String logradouro, String numero, String cidade) {
        // Monta o endereço completo
        String address = String.format("%s, %s, %s, %s", logradouro, numero, cidade, cep);

        // Se não houver chave configurada (dev), usa o Mock antigo
        if (apiKey == null || apiKey.equals("SUA_CHAVE_DA_GOOGLE_API_AQUI") || apiKey.isEmpty()) {
            return getMockCoordinates(logradouro);
        }

        try {
            String url = "https://maps.googleapis.com/maps/api/geocode/json?address="
                    + address.replace(" ", "+")
                    + "&key=" + apiKey;

            GoogleGeocodingResponse response = restTemplate.getForObject(url, GoogleGeocodingResponse.class);

            if (response != null && "OK".equals(response.getStatus()) && !response.getResults().isEmpty()) {
                GoogleGeocodingResponse.Location location = response.getResults().get(0).getGeometry().getLocation();

                return Optional.of(new Coordinates(
                        BigDecimal.valueOf(location.getLat()),
                        BigDecimal.valueOf(location.getLng())
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    // Mantemos o Mock como fallback se a API falhar ou não tiver chave
    private Optional<Coordinates> getMockCoordinates(String logradouro) {
        if (logradouro.equalsIgnoreCase("Av. Paulista")) {
            return Optional.of(new Coordinates(new BigDecimal("-23.5614"), new BigDecimal("-46.6560")));
        }
        return Optional.of(new Coordinates(new BigDecimal("-23.5505"), new BigDecimal("-46.6333")));
    }
}