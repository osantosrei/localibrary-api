package com.localibrary.controller;

import com.localibrary.dto.*;
import com.localibrary.dto.request.CreateModeratorRequestDTO;
import com.localibrary.dto.request.UpdateStatusRequestDTO;
import com.localibrary.dto.response.AdminResponseDTO;
import com.localibrary.enums.StatusBiblioteca;
import com.localibrary.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin") // ⬅️ Prefixo Geral (Correto para Sprint 5)
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

// ==========================================
    // 🟢 MÉTODOS DA SPRINT 2 (MODERADORES)
    // ⚠️ Note que adicionamos "/moderadores" aqui!
    // ==========================================

    // RF-23: Cadastro de Moderador
    // URL Final: POST /admin/moderadores
    @PostMapping("/moderadores")
    public ResponseEntity<AdminResponseDTO> createModerator(
            @Valid @RequestBody CreateModeratorRequestDTO dto
    ) {
        AdminResponseDTO newModerator = adminService.createModerator(dto);
        return new ResponseEntity<>(newModerator, HttpStatus.CREATED);
    }

    // RF-22: Listar Moderadores
    // URL Final: GET /admin/moderadores
    @GetMapping("/moderadores")
    public ResponseEntity<List<AdminResponseDTO>> listModerators() {
        List<AdminResponseDTO> moderators = adminService.listModerators();
        return ResponseEntity.ok(moderators);
    }

    // RF-24: Alterar Status de Moderador
    @PatchMapping("/moderadores/{id}")
    public ResponseEntity<AdminResponseDTO> updateModeratorStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusRequestDTO dto
    ) {
        AdminResponseDTO updatedModerator = adminService.updateModeratorStatus(id, dto);
        return ResponseEntity.ok(updatedModerator);
    }

    // RF-25: Remover Moderador
    @DeleteMapping("/moderadores/{id}")
    public ResponseEntity<Void> deleteModerator(@PathVariable Long id) {
        adminService.deleteModerator(id);
        return ResponseEntity.noContent().build();
    }

    // ==========================================
    // 🔵 MÉTODOS DA SPRINT 5 (DASHBOARD E LIBS)
    // ==========================================

    // RF-16: Dashboard
    // URL Final: GET /admin/dashboard
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardDTO> getDashboard() {
        return ResponseEntity.ok(adminService.getDashboardData());
    }

    // RF-17, RF-19: Listar Bibliotecas
    // URL Final: GET /admin/bibliotecas
    @GetMapping("/bibliotecas")
    public ResponseEntity<List<BibliotecaAdminDTO>> listBibliotecas(
            @RequestParam(required = false) StatusBiblioteca status
    ) {
        return ResponseEntity.ok(adminService.listBibliotecas(status));
    }

    // RF-18, RF-20: Alterar Status (Aprovar/Bloquear)
    // URL Final: PATCH /admin/bibliotecas/{id}/status
    @PatchMapping("/bibliotecas/{id}/status")
    public ResponseEntity<BibliotecaAdminDTO> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusBibliotecaDTO dto
    ) {
        return ResponseEntity.ok(adminService.updateBibliotecaStatus(id, dto));
    }

    // RF-21: Excluir Biblioteca
    @DeleteMapping("/bibliotecas/{id}")
    public ResponseEntity<Void> deleteBiblioteca(@PathVariable Long id) {
        adminService.deleteBiblioteca(id);
        return ResponseEntity.noContent().build();
    }
}