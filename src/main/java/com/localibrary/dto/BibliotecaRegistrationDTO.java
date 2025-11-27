package com.localibrary.dto;

import com.localibrary.enums.CategoriaBiblioteca;
import jakarta.validation.constraints.*;
import lombok.Data;

import static com.localibrary.util.Constants.*;

@Data
public class BibliotecaRegistrationDTO {

    // Dados da Biblioteca (tbl_biblioteca)
    @NotBlank(message = MSG_CAMPO_OBRIGATORIO)
    private String nomeFantasia;

    @NotBlank(message = MSG_CAMPO_OBRIGATORIO)
    private String razaoSocial;

    /**
     * ✅ CORREÇÃO CRÍTICA: CNPJ agora aceita com OU sem formatação
     * Validação customizada no ValidationUtil aceita ambos os formatos
     * O AuthenticationService sanitiza antes de salvar
     */
    @NotBlank(message = MSG_CAMPO_OBRIGATORIO)
    private String cnpj;

    private String telefone;

    @NotNull(message = MSG_CAMPO_OBRIGATORIO)
    private CategoriaBiblioteca categoria;

    private String site;

    @NotBlank(message = MSG_CAMPO_OBRIGATORIO)
    @Email(message = MSG_EMAIL_INVALIDO)
    private String email;

    @NotBlank(message = MSG_CAMPO_OBRIGATORIO)
    @Size(min = MIN_SENHA_LENGTH, max = MAX_SENHA_LENGTH, message = MSG_SENHA_INVALIDA)
    private String senha;

    // Dados do Endereço (tbl_endereco)
    @NotBlank(message = MSG_CAMPO_OBRIGATORIO)
    private String cep;

    @NotBlank(message = MSG_CAMPO_OBRIGATORIO)
    private String logradouro;

    @NotBlank(message = MSG_CAMPO_OBRIGATORIO)
    private String numero;

    private String complemento;

    @NotBlank(message = MSG_CAMPO_OBRIGATORIO)
    private String bairro;

    @NotBlank(message = MSG_CAMPO_OBRIGATORIO)
    private String cidade;

    @NotBlank(message = MSG_CAMPO_OBRIGATORIO)
    private String estado;
}
