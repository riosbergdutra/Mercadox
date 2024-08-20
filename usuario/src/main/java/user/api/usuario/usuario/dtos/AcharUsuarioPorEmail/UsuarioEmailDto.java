package user.api.usuario.usuario.dtos.AcharUsuarioPorEmail;

import java.util.UUID;

import user.api.usuario.usuario.enums.Role;

public record UsuarioEmailDto(UUID idUsuario,String email, String senha, Role role ) {
    
}
