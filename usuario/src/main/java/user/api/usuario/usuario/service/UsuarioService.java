package user.api.usuario.usuario.service;

import java.util.UUID;

import user.api.usuario.usuario.dtos.AcharUsuarioIdDto.UsuarioIdResponseDto;
import user.api.usuario.usuario.dtos.CriarUsuarioDto.UsuarioRequestDto;
import user.api.usuario.usuario.dtos.CriarUsuarioDto.UsuarioResponseDto;
import user.api.usuario.usuario.dtos.MudarSenha.MudarSenhaRequest;
import user.api.usuario.usuario.model.Usuario;

public interface UsuarioService {
    // metodo para criar uma conta do usuario, sendo admin, usuario ou vendedor
    public UsuarioResponseDto saveUsuario(UsuarioRequestDto usuarioDto);

    // achar o usuario pelo id dele (precisa ser autenticado)
    public UsuarioIdResponseDto getUsuarioById(UUID id, UUID userId);

    // metodo na qual o autenticador vai se comunicar via http para criar o token
    public Usuario getUsuarioByEmail(String email, String senha);

    // permite o usuario mudar a senha (precisa ser autenticado)
    public String mudarSenha(UUID id, UUID userId, MudarSenhaRequest mudarSenhaRequest);
    public String deleteUsuario(UUID id, UUID userId);
}