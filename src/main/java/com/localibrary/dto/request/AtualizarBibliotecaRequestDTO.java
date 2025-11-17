package com.localibrary.dto.request;

import com.localibrary.enums.CategoriaBiblioteca;
import com.localibrary.util.Constants;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para atualização de dados da biblioteca.
 *
 * RF-14: Atualizar dados da biblioteca
 * RN-01: Biblioteca só atualiza próprios dados
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AtualizarBibliotecaRequestDTO {

    @Size(max = 100, message = "Nome fantasia deve ter no máximo 100 caracteres")
    private String nomeFantasia;

    @Size(max = 100, message = "Razão social deve ter no máximo 100 caracteres")
    private String razaoSocial;

    @Pattern(regexp = Constants.REGEX_TELEFONE,
            message = Constants.MSG_TELEFONE_INVALIDO)
    private String telefone;

    private CategoriaBiblioteca categoria;

    @Size(max = 100, message = "Site deve ter no máximo 100 caracteres")
    @Pattern(regexp = "^(https?://)?(www\\.)?[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}(/.*)?$",
            message = "URL do site inválida")
    private String site;

    @Size(max = 255, message = "URL da foto deve ter no máximo 255 caracteres")
    private String fotoBiblioteca;

    /**
     * Endereço pode ser atualizado
     * Se fornecido, será validado e coordenadas recalculadas
     */
    @Valid
    private EnderecoRequestDTO endereco;
}