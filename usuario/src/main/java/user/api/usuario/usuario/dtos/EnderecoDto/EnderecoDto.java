package user.api.usuario.usuario.dtos.EnderecoDto;


import java.util.UUID;

import jakarta.validation.constraints.NotBlank;

public record EnderecoDto(
        @NotBlank(message = "rua não pode estar vazio") UUID idEndereco,
        @NotBlank(message = "rua não pode estar vazio") String rua,
        @NotBlank(message = "numero não pode estar vazio") String numero,
        @NotBlank(message = "cidade não pode estar vazio") String cidade,
        @NotBlank(message = "estado não pode estar vazio") String estado,
        @NotBlank(message = "cep não pode estar vazio") String cep) {

}
