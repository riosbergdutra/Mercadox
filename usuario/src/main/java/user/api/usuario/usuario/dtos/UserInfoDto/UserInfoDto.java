package user.api.usuario.usuario.dtos.UserInfoDto;

import java.util.UUID;
import user.api.usuario.usuario.enums.Role;

public record UserInfoDto(
    UUID idUsuario,
    String nome,
    String email,
    Role role
    ) {
    
}
