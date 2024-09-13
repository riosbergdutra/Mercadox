package user.api.usuario.usuario.dtos.AcharUsuarioIdDto;

import java.util.UUID;

import user.api.usuario.usuario.enums.Role;


public record UsuarioTokenResponse(UUID idUsuario, Role role) {
    
}
