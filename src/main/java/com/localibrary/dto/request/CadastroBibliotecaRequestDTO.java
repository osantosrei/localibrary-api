package com.localibrary.dto.request;

import com.localibrary.enums.CategoriaBiblioteca;
import com.localibrary.util.Constants;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para requisição de cadastro de biblioteca.
 *
 * RF-08: Cadastro de biblioteca
 * RN-12: Cada biblioteca tem um endereço único
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CadastroBibliotecaRequestDTO {

    // ===================================
    // DADOS DA BIBLIOTECA
    // ===================================

    @NotBlank(message = Constants.MSG_CAMPO_OBRIGATORIO)
    @Size(max = 100, message = "Nome fantasia deve ter no máximo 100 caracteres")
    private String nomeFantasia;

    @NotBlank(message = Constants.MSG_CAMPO_OBRIGATORIO)
    @Size(max = 100, message = "Razão social deve ter no máximo 100 caracteres")
    private String razaoSocial;

    @NotBlank(message = Constants.MSG_CAMPO_OBRIGATORIO)
    @Pattern(regexp = "\\d{2}\\.\\d{3}\\.\\d{3}/\\d{4}-\\d{2}",
            message = Constants.MSG_CNPJ_INVALIDO)
    private String cnpj;

    @Pattern(regexp = Constants.REGEX_TELEFONE,
            message = Constants.MSG_TELEFONE_INVALIDO)
    private String telefone;

    @NotNull(message = Constants.MSG_CAMPO_OBRIGATORIO)
    private CategoriaBiblioteca categoria;

    @Size(max = 100, message = "Site deve ter no máximo 100 caracteres")
    @Pattern(regexp = "^(https?://)?(www\\.)?[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}(/.*)?$",
            message = "URL do site inválida")
    private String site;

    // ===================================
    // CREDENCIAIS DE LOGIN
    // ===================================

    @NotBlank(message = Constants.MSG_CAMPO_OBRIGATORIO)
    @Email(message = Constants.MSG_EMAIL_INVALIDO)
    private String email;

    @NotBlank(message = Constants.MSG_CAMPO_OBRIGATORIO)
    @Size(min = Constants.MIN_SENHA_LENGTH,
            max = Constants.MAX_SENHA_LENGTH,
            message = "Senha deve ter entre " + Constants.MIN_SENHA_LENGTH +
                    " e " + Constants.MAX_SENHA_LENGTH + " caracteres")
    private String senha;

    // ===================================
    // ENDEREÇO (nested)
    // ===================================

    @NotNull(message = "Endereço é obrigatório")
    @Valid // Valida o objeto EnderecoRequestDTO
    private EnderecoRequestDTO endereco;
}