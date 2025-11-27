package com.localibrary.dto;

import com.localibrary.entity.Biblioteca;
import com.localibrary.enums.StatusBiblioteca;
import lombok.Data;

import java.math.BigDecimal;

/**
 * ✅ NOVO: DTO para exibição de bibliotecas no mapa do Dashboard (RF-16)
 */
@Data
public class BibliotecaMapaDTO {
    private Long id;
    private String nomeFantasia;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private StatusBiblioteca status;
    private String cidade;

    public BibliotecaMapaDTO(Biblioteca b) {
        this.id = b.getId();
        this.nomeFantasia = b.getNomeFantasia();
        this.status = b.getStatus();

        if (b.getEndereco() != null) {
            this.latitude = b.getEndereco().getLatitude();
            this.longitude = b.getEndereco().getLongitude();
            this.cidade = b.getEndereco().getCidade();
        }
    }
}
