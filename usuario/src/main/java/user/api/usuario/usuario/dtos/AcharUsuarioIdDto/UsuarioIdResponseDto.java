package user.api.usuario.usuario.dtos.AcharUsuarioIdDto;

import java.time.LocalDate;

public record UsuarioIdResponseDto(
                String nome,
                String email,
                String senha,
                String imagem,
                LocalDate dataConta) {

}
