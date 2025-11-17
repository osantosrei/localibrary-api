package com.localibrary.dto.response;

import com.localibrary.entity.Biblioteca;
import com.localibrary.util.DistanceCalculator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * DTO de resposta de biblioteca com distância calculada.
 * Usado em listagens com ordenação por proximidade.
 *
 * RF-04: Listar bibliotecas próximas
 * RF-06: Bibliotecas que possuem determinado livro
 * RN-10: Ordenar por proximidade
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BibliotecaComDistanciaDTO extends BibliotecaResponseDTO {

    private Double distanciaKm;
    private String distanciaFormatada;

    /**
     * Construtor a partir da entidade Biblioteca e coordenadas do usuário
     */
    public BibliotecaComDistanciaDTO(Biblioteca biblioteca, Double userLat, Double userLon) {
        super(biblioteca);

        if (userLat != null && userLon != null &&
                biblioteca.getEndereco() != null &&
                biblioteca.getEndereco().getLatitude() != null &&
                biblioteca.getEndereco().getLongitude() != null) {

            this.distanciaKm = DistanceCalculator.calculateDistance(
                    userLat,
                    userLon,
                    biblioteca.getEndereco().getLatitude().doubleValue(),
                    biblioteca.getEndereco().getLongitude().doubleValue()
            );

            this.distanciaFormatada = DistanceCalculator.formatDistance(this.distanciaKm);
        } else {
            this.distanciaKm = null;
            this.distanciaFormatada = "N/A";
        }
    }
}