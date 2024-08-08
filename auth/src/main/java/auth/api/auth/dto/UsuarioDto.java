package auth.api.auth.dto;

import java.util.Set;

public record UsuarioDto(String idUsuario, String email, String senha, Set<Role> roles) {
} 

