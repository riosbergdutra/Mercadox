package user.api.usuario.usuario.service;

import java.util.UUID;

import user.api.usuario.usuario.dtos.AcharUsuarioIdDto.UsuarioIdResponseDto;
import user.api.usuario.usuario.dtos.CriarUsuarioDto.UsuarioRequestDto;
import user.api.usuario.usuario.dtos.CriarUsuarioDto.UsuarioResponseDto;
import user.api.usuario.usuario.model.Usuario;

public interface UsuarioService {
    public UsuarioResponseDto saveUsuario(UsuarioRequestDto usuarioDto);

    public UsuarioIdResponseDto getUsuarioById(UUID id);

    public Usuario getUsuarioByEmail(String email, String senha);
}