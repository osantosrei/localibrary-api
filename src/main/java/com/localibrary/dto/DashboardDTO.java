package com.localibrary.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * ✅ CORREÇÃO RF-16: Dashboard agora inclui mapa de localização
 */
@Data
@Builder
public class DashboardDTO {

    // Estatísticas gerais
    private long totalBibliotecas;
    private long bibliotecasAtivas;
    private long bibliotecasPendentes;
    private long totalLivrosCadastrados;
    private long totalExemplares;

    // ✅ NOVO: Mapa de localização (RF-16)
    private List<BibliotecaMapaDTO> bibliotecasMapa;
}