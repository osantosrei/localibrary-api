package com.localibrary.dto;

import com.localibrary.enums.CategoriaBiblioteca;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.hibernate.validator.constraints.br.CNPJ;

import static com.localibrary.util.Constants.*;

@Data
public class BibliotecaRegistrationDTO {

    // Dados da Biblioteca (tbl_biblioteca)
    @NotBlank
    private String nomeFantasia;
    @NotBlank
    private String razaoSocial;
    @NotBlank(message = MSG_CAMPO_OBRIGATORIO)
    @Pattern(regexp = REGEX_CNPJ, message = MSG_CNPJ_INVALIDO)
    private String cnpj;
    @Pattern(regexp = REGEX_TELEFONE, message = MSG_TELEFONE_INVALIDO)
    private String telefone;
    @NotNull
    private CategoriaBiblioteca categoria;
    private String site;
    @NotBlank(message = MSG_CAMPO_OBRIGATORIO)
    @Pattern(regexp = REGEX_EMAIL, message = MSG_EMAIL_INVALIDO)
    private String email;
    @NotBlank
    @Size(min = MIN_SENHA_LENGTH, max = MAX_SENHA_LENGTH, message = MSG_SENHA_CURTA)
    private String senha;

    // Dados do Endereço (tbl_endereco)
    @NotBlank(message = MSG_CAMPO_OBRIGATORIO)
    @Pattern(regexp = REGEX_CEP, message = MSG_CEP_INVALIDO)
    private String cep;
    @NotBlank
    private String logradouro;
    @NotBlank
    private String numero;
    private String complemento;
    @NotBlank
    private String bairro;
    @NotBlank
    private String cidade;
    @NotBlank
    private String estado;
}