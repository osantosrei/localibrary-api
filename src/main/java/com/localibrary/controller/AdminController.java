package com.localibrary.controller;

import com.localibrary.dto.*;
import com.localibrary.dto.request.CreateModeratorRequestDTO;
import com.localibrary.dto.request.UpdateStatusRequestDTO;
import com.localibrary.dto.response.AdminResponseDTO;
import com.localibrary.enums.StatusBiblioteca;
import com.localibrary.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@Tag(name = "2. Administração", description = "Gestão do sistema (Admins e Moderadores)")
@SecurityRequirement(name = "bearerAuth") // Cadeado para todos os métodos
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // --- MODERADORES ---

    @Operation(summary = "Cadastrar Moderador", description = "Cria um novo usuário com perfil MODERADOR. (Apenas ADMIN)")
    @PostMapping("/moderadores")
    public ResponseEntity<AdminResponseDTO> createModerator(@Valid @RequestBody CreateModeratorRequestDTO dto) {
        AdminResponseDTO newModerator = adminService.createModerator(dto);
        return new ResponseEntity<>(newModerator, HttpStatus.CREATED);
    }

    @Operation(summary = "Listar Moderadores", description = "Retorna todos os moderadores do sistema. (Apenas ADMIN)")
    @GetMapping("/moderadores")
    public ResponseEntity<List<AdminResponseDTO>> listModerators() {
        return ResponseEntity.ok(adminService.listModerators());
    }

    @Operation(summary = "Atualizar Status de Moderador", description = "Ativa ou inativa um moderador. (Apenas ADMIN)")
    @PatchMapping("/moderadores/{id}")
    public ResponseEntity<AdminResponseDTO> updateModeratorStatus(@PathVariable Long id, @Valid @RequestBody UpdateStatusRequestDTO dto) {
        return ResponseEntity.ok(adminService.updateModeratorStatus(id, dto));
    }

    @Operation(summary = "Excluir Moderador", description = "Remove permanentemente um moderador. (Apenas ADMIN)")
    @DeleteMapping("/moderadores/{id}")
    public ResponseEntity<Void> deleteModerator(@PathVariable Long id) {
        adminService.deleteModerator(id);
        return ResponseEntity.noContent().build();
    }

    // --- BIBLIOTECAS ---

    @Operation(summary = "Dashboard", description = "Estatísticas gerais do sistema para a tela inicial do Admin.")
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardDTO> getDashboard() {
        return ResponseEntity.ok(adminService.getDashboardData());
    }

    @Operation(summary = "Listar Bibliotecas (Visão Admin)", description = "Lista bibliotecas com dados sensíveis (CNPJ, Email). Permite filtrar por status.")
    @GetMapping("/bibliotecas")
    public ResponseEntity<Page<BibliotecaAdminDTO>> listBibliotecas(
            @Parameter(description = "Filtro opcional: ATIVO, PENDENTE ou INATIVO")
            @RequestParam(required = false) StatusBiblioteca status,
            @Parameter(description = "Página (0-based)") @RequestParam(required = false) Integer page,
            @Parameter(description = "Tamanho da página") @RequestParam(required = false) Integer size,
            @Parameter(description = "Campo para ordenar (ex: nomeFantasia)") @RequestParam(required = false) String sortField,
            @Parameter(description = "Direção da ordenação: ASC ou DESC") @RequestParam(required = false) String sortDir
    ) {
        return ResponseEntity.ok(adminService.listBibliotecas(status, page, size, sortField, sortDir));
    }

    @Operation(summary = "Moderar Biblioteca", description = "Aprova (ATIVO), Reprova (INATIVO) ou coloca em análise (PENDENTE).")
    @PatchMapping("/bibliotecas/{id}/status")
    public ResponseEntity<BibliotecaAdminDTO> updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateStatusBibliotecaDTO dto) {
        return ResponseEntity.ok(adminService.updateBibliotecaStatus(id, dto));
    }

    @Operation(summary = "Excluir Biblioteca", description = "Remove uma biblioteca e todo seu acervo. (Apenas ADMIN)")
    @DeleteMapping("/bibliotecas/{id}")
    public ResponseEntity<Void> deleteBiblioteca(@PathVariable Long id) {
        adminService.deleteBiblioteca(id);
        return ResponseEntity.noContent().build();
    }
}