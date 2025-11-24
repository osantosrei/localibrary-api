package com.localibrary.service;

import com.localibrary.dto.BibliotecaAdminDTO;
import com.localibrary.dto.DashboardDTO;
import com.localibrary.dto.UpdateStatusBibliotecaDTO;
import com.localibrary.dto.response.AdminResponseDTO;
import com.localibrary.dto.request.CreateModeratorRequestDTO;
import com.localibrary.dto.request.UpdateStatusRequestDTO;
import com.localibrary.entity.Admin;
import com.localibrary.entity.Biblioteca;
import com.localibrary.enums.RoleAdmin;
import com.localibrary.enums.StatusAdmin;
import com.localibrary.enums.StatusBiblioteca;
import com.localibrary.repository.AdminRepository;
import com.localibrary.repository.BibliotecaLivroRepository;
import com.localibrary.repository.BibliotecaRepository;
import com.localibrary.repository.LivroRepository;
import com.localibrary.util.Constants;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private BibliotecaRepository bibliotecaRepository;

    @Autowired
    private LivroRepository livroRepository;

    @Autowired
    private BibliotecaLivroRepository bibliotecaLivroRepository;

    /**
     * RF-16: Dashboard Administrativo
     */
    public DashboardDTO getDashboardData() {
        long totalLibs = bibliotecaRepository.count();
        long activeLibs = bibliotecaRepository.countByStatus(StatusBiblioteca.ATIVO);
        long pendingLibs = bibliotecaRepository.countByStatus(StatusBiblioteca.PENDENTE);
        long totalBooks = livroRepository.count();
        Long totalCopies = bibliotecaLivroRepository.sumTotalExemplares();

        return DashboardDTO.builder()
                .totalBibliotecas(totalLibs)
                .bibliotecasAtivas(activeLibs)
                .bibliotecasPendentes(pendingLibs)
                .totalLivrosCadastrados(totalBooks)
                .totalExemplares(totalCopies != null ? totalCopies : 0)
                .build();
    }

    /**
     * RF-17, RF-19: Listar bibliotecas filtrando por status (opcional) COM PAGINAÇÃO
     */
    public Page<BibliotecaAdminDTO> listBibliotecas(StatusBiblioteca status, Integer page, Integer size, String sortField, String sortDir) {
        int p = (page == null || page < 0) ? 0 : page;
        int s = (size == null || size <= 0) ? Constants.DEFAULT_PAGE_SIZE : Math.min(size, Constants.MAX_PAGE_SIZE);
        String sf = (sortField == null || sortField.isBlank()) ? Constants.DEFAULT_SORT_FIELD : sortField;
        String sd = (sortDir == null || (!sortDir.equalsIgnoreCase("ASC") && !sortDir.equalsIgnoreCase("DESC"))) ? Constants.DEFAULT_SORT_DIRECTION : sortDir.toUpperCase();
        org.springframework.data.domain.Sort sort = org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.fromString(sd), sf);
        Pageable pageable = PageRequest.of(p, s, sort);

        Page<Biblioteca> libsPage;
        if (status != null) {
            libsPage = bibliotecaRepository.findByStatus(status, pageable);
        } else {
            libsPage = bibliotecaRepository.findAll(pageable);
        }

        List<BibliotecaAdminDTO> dtos = libsPage.getContent().stream()
                .map(BibliotecaAdminDTO::new)
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, libsPage.getTotalElements());
    }

    /**
     * RF-18, RF-20: Alterar status da biblioteca (Aprovar/Bloquear)
     */
    public BibliotecaAdminDTO updateBibliotecaStatus(Long id, UpdateStatusBibliotecaDTO dto) {
        Biblioteca lib = bibliotecaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Biblioteca não encontrada."));

        lib.setStatus(dto.getStatus());
        Biblioteca saved = bibliotecaRepository.save(lib);

        return new BibliotecaAdminDTO(saved);
    }

    /**
     * RF-21: Excluir biblioteca
     */
    public void deleteBiblioteca(Long id) {
        if (!bibliotecaRepository.existsById(id)) {
            throw new EntityNotFoundException("Biblioteca não encontrada.");
        }
        // O CascadeType.ALL na entidade cuidará do Endereço, Credenciais e Livros
        bibliotecaRepository.deleteById(id);
    }

    // RF-23: Cadastrar novos moderadores
    public AdminResponseDTO createModerator(CreateModeratorRequestDTO dto) {
        if (adminRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new EntityExistsException("Email já cadastrado.");
        }

        Admin newModerator = new Admin();
        newModerator.setNome(dto.getNome());
        newModerator.setSobrenome(dto.getSobrenome());
        newModerator.setEmail(dto.getEmail());
        newModerator.setSenha(passwordEncoder.encode(dto.getSenha()));
        newModerator.setRoleAdmin(RoleAdmin.MODERADOR);
        newModerator.setStatus(StatusAdmin.ATIVO); // Padrão

        Admin savedModerator = adminRepository.save(newModerator);
        return new AdminResponseDTO(savedModerator);
    }

    // RF-22: Listar todos os moderadores
    public List<AdminResponseDTO> listModerators() {
        return adminRepository.findAll().stream()
                .filter(admin -> admin.getRoleAdmin() == RoleAdmin.MODERADOR)
                .map(AdminResponseDTO::new) // Converte Admin para AdminResponseDTO
                .collect(Collectors.toList());
    }

    // RF-24: Alterar status de moderador
    public AdminResponseDTO updateModeratorStatus(Long id, UpdateStatusRequestDTO dto) {
        Admin moderator = findModeratorById(id);
        moderator.setStatus(dto.getStatus());
        Admin updatedModerator = adminRepository.save(moderator);
        return new AdminResponseDTO(updatedModerator);
    }

    // RF-25: Remover moderador
    public void deleteModerator(Long id) {
        Admin moderator = findModeratorById(id);
        adminRepository.delete(moderator);
    }

    // Método auxiliar para evitar repetição
    private Admin findModeratorById(Long id) {
        return adminRepository.findById(id)
                .filter(admin -> admin.getRoleAdmin() == RoleAdmin.MODERADOR)
                .orElseThrow(() -> new EntityNotFoundException("Moderador não encontrado com id: " + id));
    }
}
