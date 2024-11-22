package user.api.usuario.usuario.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

import org.springframework.security.core.Authentication;
import user.api.usuario.usuario.dtos.AcharUsuarioIdDto.UsuarioIdResponseDto;
import user.api.usuario.usuario.dtos.AcharUsuarioIdDto.UsuarioTokenResponse;
import user.api.usuario.usuario.dtos.AcharUsuarioPorEmail.UsuarioEmailDto;
import user.api.usuario.usuario.dtos.CriarUsuarioDto.UsuarioRequestDto;
import user.api.usuario.usuario.dtos.CriarUsuarioDto.UsuarioResponseDto;
import user.api.usuario.usuario.dtos.MudarSenha.MudarSenhaRequest;
import user.api.usuario.usuario.dtos.UserInfoDto.UserInfoDto;
import user.api.usuario.usuario.service.UsuarioService;

/**
 * Controlador para gerenciar operações relacionadas aos usuários.
 * 
 * Fornece endpoints para criar, consultar, atualizar e excluir usuários.
 */
@RestController
@RequestMapping("/usuario")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    /**
     * Endpoint para criar um novo usuário.
     * 
     * @param usuarioDto DTO contendo os dados do usuário a ser criado.
     * @return ResponseEntity com o DTO do usuário criado e status 201 (CREATED).
     */
    @PostMapping("/criar")
    public ResponseEntity<UsuarioResponseDto> criarUsuario(
            @Valid @RequestBody UsuarioRequestDto usuarioDto) {
        UsuarioResponseDto resposta = usuarioService.saveUsuario(usuarioDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);

    }

    /**
 * Endpoint para obter informações do usuário autenticado.
 *
 * @param authentication Objeto de autenticação contendo o token do usuário logado.
 * @return ResponseEntity com o DTO do usuário autenticado e status 200 (OK).
 */
@GetMapping("/user-info")
public ResponseEntity<UserInfoDto> getUserInfo() {
    UserInfoDto userInfo = usuarioService.getUserInfo();
    return ResponseEntity.ok(userInfo);
}


    /**
     * Endpoint para obter detalhes de um usuário por ID.
     * 
     * @param id             ID do usuário a ser consultado.
     * @param authentication Objeto de autenticação contendo o ID do usuário logado.
     * @return ResponseEntity com o DTO do usuário e status 200 (OK).
     */
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioIdResponseDto> getUsuarioById(@PathVariable UUID id, Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        UsuarioIdResponseDto usuario = usuarioService.getUsuarioById(id, userId);
        return ResponseEntity.ok(usuario);
    }

     /**
     * Endpoint para obter detalhes de um usuário por ID.
     * 
     * @param id             ID do usuário a ser consultado.
     * @param authentication Objeto de autenticação contendo o ID do usuário logado.
     * @return ResponseEntity com o DTO do usuário e status 200 (OK).
     */
    @GetMapping("/token/{id}")
    public ResponseEntity<UsuarioTokenResponse> getUsuarioForToken(@PathVariable UUID id) {
        UsuarioTokenResponse usuario = usuarioService.getUsuarioForToken(id);
        return ResponseEntity.ok(usuario);
    }

    /**
     * Endpoint para obter um usuário por email e senha.
     * 
     * @param email Email do usuário a ser consultado.
     * @param senha Senha do usuário a ser consultado.
     * @return ResponseEntity com o objeto Usuario e status 200 (OK).
     */
    @GetMapping("/email/")
    public ResponseEntity<UsuarioEmailDto> getUsuarioByEmail(@RequestParam String email, @RequestParam String senha) {
        UsuarioEmailDto usuarioEmailDto = usuarioService.getUsuarioByEmail(email, senha);
        return ResponseEntity.ok(usuarioEmailDto);
    }

    /**
     * Endpoint para alterar a senha do usuário.
     * 
     * @param id                ID do usuário cuja senha será alterada.
     * @param authentication    Objeto de autenticação contendo o ID do usuário
     *                          logado.
     * @param mudarSenhaRequest DTO com as informações para alterar a senha.
     * @return ResponseEntity com uma mensagem de sucesso e status 200 (OK).
     */
    @PutMapping("/senha/{id}") // Use PUT para alterações de recurso
    public ResponseEntity<String> mudarSenha(
            @PathVariable UUID id,
            Authentication authentication,
            @RequestBody MudarSenhaRequest mudarSenhaRequest) {

        UUID userId = UUID.fromString(authentication.getName());
        String resultado = usuarioService.mudarSenha(id, userId, mudarSenhaRequest);
        return ResponseEntity.ok(resultado);
    }

    /**
     * Endpoint para excluir um usuário.
     * 
     * @param id             ID do usuário a ser excluído.
     * @param authentication Objeto de autenticação contendo o ID do usuário logado.
     * @return ResponseEntity com uma mensagem de sucesso e status 200 (OK).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUsuario(@PathVariable UUID id, Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        String resultado = usuarioService.deleteUsuario(id, userId);
        return ResponseEntity.ok(resultado);
    }
}
