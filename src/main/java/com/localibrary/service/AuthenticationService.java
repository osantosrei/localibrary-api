package com.localibrary.service;

import com.localibrary.dto.BibliotecaRegistrationDTO;
import com.localibrary.dto.request.LoginRequestDTO;
import com.localibrary.dto.response.LoginResponseDTO;
import com.localibrary.entity.Biblioteca;
import com.localibrary.entity.CredencialBiblioteca;
import com.localibrary.entity.Endereco;
import com.localibrary.enums.StatusBiblioteca;
import com.localibrary.repository.BibliotecaRepository;
import com.localibrary.repository.CredencialBibliotecaRepository;
import com.localibrary.repository.EnderecoRepository;
import com.localibrary.security.JwtTokenService;
import com.localibrary.util.Constants; // Import Constants
import com.localibrary.util.ValidationUtil; // ⬅️ Import ValidationUtil
import jakarta.persistence.EntityExistsException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class AuthenticationService {


    private final AuthenticationManager authenticationManager;

    private final JwtTokenService jwtTokenService;

    private final BibliotecaRepository bibliotecaRepository;

    private final CredencialBibliotecaRepository credenciaisRepository;

    private final EnderecoRepository enderecoRepository;

    private final GeolocationService geolocationService;

    private final PasswordEncoder passwordEncoder;

    public AuthenticationService(AuthenticationManager authenticationManager,
                                 JwtTokenService jwtTokenService,
                                 BibliotecaRepository bibliotecaRepository,
                                 CredencialBibliotecaRepository credenciaisRepository,
                                 EnderecoRepository enderecoRepository,
                                 GeolocationService geolocationService,
                                 PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenService = jwtTokenService;
        this.bibliotecaRepository = bibliotecaRepository;
        this.credenciaisRepository = credenciaisRepository;
        this.enderecoRepository = enderecoRepository;
        this.geolocationService = geolocationService;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponseDTO login(LoginRequestDTO loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getSenha()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtTokenService.generateToken(authentication);
        return new LoginResponseDTO(token, "Bearer", 86400L);
    }

    @Transactional
    public void registerBiblioteca(BibliotecaRegistrationDTO dto) {

        validarCadastro(dto);

        String emailLimpo = dto.getEmail().trim(); // Remove espaços extras
        String cnpjLimpo = ValidationUtil.sanitizeCNPJ(dto.getCnpj()); // Remove pontuação
        String telefoneLimpo = ValidationUtil.sanitizeTelefone(dto.getTelefone());
        String cepLimpo = ValidationUtil.sanitizeCEP(dto.getCep());

        // 3. Valida Duplicidade (Agora comparando Banana com Banana)
        if (credenciaisRepository.findByEmail(emailLimpo).isPresent()) {
            throw new EntityExistsException("Este email já está em uso: " + emailLimpo);
        }

        // AQUI ESTAVA O BUG: Antes buscávamos o CNPJ com ponto no banco sem ponto.
        // Agora buscamos o CNPJ limpo.
        if (bibliotecaRepository.findByCnpj(cnpjLimpo).isPresent()) {
            throw new EntityExistsException("Este CNPJ já está em uso.");
        }

        // 4. Geolocalização
        Coordinates coords = geolocationService.getCoordinatesFromAddress(
                        dto.getCep(), dto.getLogradouro(), dto.getNumero(), dto.getCidade())
                .orElseThrow(() -> new IllegalArgumentException("Endereço inválido ou não encontrado."));

        if (!ValidationUtil.isValidCoordinates(coords.latitude().doubleValue(), coords.longitude().doubleValue())) {
            throw new IllegalArgumentException("Coordenadas geográficas inválidas.");
        }

        // 5. Persistência (Usando as variáveis limpas)
        Endereco endereco = new Endereco();
        endereco.setCep(cepLimpo); // Usa o limpo
        endereco.setLogradouro(dto.getLogradouro());
        endereco.setNumero(dto.getNumero());
        endereco.setComplemento(dto.getComplemento());
        endereco.setBairro(dto.getBairro());
        endereco.setCidade(dto.getCidade());
        endereco.setEstado(dto.getEstado());
        endereco.setLatitude(coords.latitude());
        endereco.setLongitude(coords.longitude());
        Endereco savedEndereco = enderecoRepository.save(endereco);

        Biblioteca biblioteca = new Biblioteca();
        biblioteca.setNomeFantasia(dto.getNomeFantasia());
        biblioteca.setRazaoSocial(dto.getRazaoSocial());
        biblioteca.setCnpj(cnpjLimpo); // Usa o limpo
        biblioteca.setTelefone(telefoneLimpo); // Usa o limpo
        biblioteca.setCategoria(dto.getCategoria());
        biblioteca.setSite(dto.getSite());
        biblioteca.setStatus(StatusBiblioteca.PENDENTE);
        biblioteca.setEndereco(savedEndereco);
        Biblioteca savedBiblioteca = bibliotecaRepository.save(biblioteca);

        CredencialBiblioteca credenciais = new CredencialBiblioteca();
        credenciais.setEmail(emailLimpo); // Usa o limpo
        credenciais.setSenha(passwordEncoder.encode(dto.getSenha()));
        credenciais.setBiblioteca(savedBiblioteca);

        savedBiblioteca.setCredencial(credenciais);
        credenciaisRepository.save(credenciais);
    }

    private void validarCadastro(BibliotecaRegistrationDTO dto) {

        // Validações com ValidationUtil (Camada extra de segurança)
        if (!ValidationUtil.isValidEmail(dto.getEmail())) {
            throw new IllegalArgumentException(Constants.MSG_EMAIL_INVALIDO);
        }
        if (!ValidationUtil.isValidCNPJ(dto.getCnpj())) {
            throw new IllegalArgumentException(Constants.MSG_CNPJ_INVALIDO);
        }
        if (!ValidationUtil.isValidCEP(dto.getCep())) {
            throw new IllegalArgumentException(Constants.MSG_CEP_INVALIDO);
        }
        if (!ValidationUtil.isValidSenha(dto.getSenha())) {
            throw new IllegalArgumentException(Constants.MSG_SENHA_CURTA);
        }

        if (ValidationUtil.isNotEmpty(dto.getTelefone()) && !ValidationUtil.isValidTelefone(dto.getTelefone())) {
            throw new IllegalArgumentException(Constants.MSG_TELEFONE_INVALIDO);
        }
    }

}