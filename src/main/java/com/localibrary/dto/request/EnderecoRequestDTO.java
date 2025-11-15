package com.localibrary.dto.request;

import com.localibrary.util.Constants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para requisição de endereço.
 * Usado no cadastro e atualização de biblioteca.
 *
 * RN-12: Cada biblioteca tem exatamente um endereço
 * RN-17: Endereço é validado via API de geolocalização
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnderecoRequestDTO {

    @NotBlank(message = Constants.MSG_CAMPO_OBRIGATORIO)
    @Pattern(regexp = "\\d{5}-?\\d{3}", message = Constants.MSG_CEP_INVALIDO)
    private String cep;

    @NotBlank(message = Constants.MSG_CAMPO_OBRIGATORIO)
    @Size(max = 100, message = "Logradouro deve ter no máximo 100 caracteres")
    private String logradouro;

    @NotBlank(message = Constants.MSG_CAMPO_OBRIGATORIO)
    @Size(max = 10, message = "Número deve ter no máximo 10 caracteres")
    private String numero;

    @Size(max = 50, message = "Complemento deve ter no máximo 50 caracteres")
    private String complemento;

    @NotBlank(message = Constants.MSG_CAMPO_OBRIGATORIO)
    @Size(max = 50, message = "Bairro deve ter no máximo 50 caracteres")
    private String bairro;

    @NotBlank(message = Constants.MSG_CAMPO_OBRIGATORIO)
    @Size(max = 50, message = "Cidade deve ter no máximo 50 caracteres")
    private String cidade;

    @NotBlank(message = Constants.MSG_CAMPO_OBRIGATORIO)
    @Size(max = 50, message = "Estado deve ter no máximo 50 caracteres")
    private String estado;

    /**
     * Retorna endereço completo formatado.
     * Usado para chamada à API de geolocalização.
     */
    public String getEnderecoCompleto() {
        StringBuilder sb = new StringBuilder();
        sb.append(logradouro).append(", ");
        sb.append(numero);

        if (complemento != null && !complemento.isEmpty()) {
            sb.append(" - ").append(complemento);
        }

        sb.append(", ").append(bairro);
        sb.append(", ").append(cidade);
        sb.append(" - ").append(estado);
        sb.append(", ").append(cep);

        return sb.toString();
    }
}