package com.localibrary.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import static com.localibrary.util.Constants.MSG_SENHA_INVALIDA;
import static com.localibrary.util.Constants.REGEX_SENHA;

@Data
public class CreateModeratorRequestDTO {
    @NotBlank
    private String nome;

    @NotBlank
    private String sobrenome;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Pattern(regexp = REGEX_SENHA, message = MSG_SENHA_INVALIDA)
    private String senha;
}
