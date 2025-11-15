package com.localibrary.dto.request;

import com.localibrary.util.Constants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para requisição de login.
 * Usado tanto por bibliotecas quanto por admins/moderadores.
 *
 * RF-09: Login de biblioteca
 * RF-15: Login de admin/moderador
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDTO {

    @NotBlank(message = Constants.MSG_CAMPO_OBRIGATORIO)
    @Email(message = Constants.MSG_EMAIL_INVALIDO)
    private String email;

    @NotBlank(message = Constants.MSG_CAMPO_OBRIGATORIO)
    @Size(min = Constants.MIN_SENHA_LENGTH, message = Constants.MSG_SENHA_CURTA)
    private String senha;
}