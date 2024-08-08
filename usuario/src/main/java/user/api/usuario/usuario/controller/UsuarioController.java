package user.api.usuario.usuario.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import user.api.usuario.usuario.dtos.LoginRequest;
import user.api.usuario.usuario.dtos.AcharUsuarioIdDto.UsuarioIdResponseDto;
import user.api.usuario.usuario.dtos.CriarUsuarioDto.UsuarioRequestDto;
import user.api.usuario.usuario.dtos.CriarUsuarioDto.UsuarioResponseDto;
import user.api.usuario.usuario.enums.Role;
import user.api.usuario.usuario.model.Usuario;
import user.api.usuario.usuario.service.UsuarioService;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping(value = "/criar")
    public ResponseEntity<UsuarioResponseDto> criarUsuario(
            @RequestParam("nome") String nome,
            @RequestParam("email") String email,
            @RequestParam("senha") String senha,
            @RequestParam("role") Role role,
            @RequestParam(value = "foto", required = false) MultipartFile foto) {

        UsuarioRequestDto usuarioDto = new UsuarioRequestDto(nome, foto, email, senha, role);
        UsuarioResponseDto resposta = usuarioService.saveUsuario(usuarioDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioIdResponseDto> getUsuarioById(@PathVariable UUID id) {
        UsuarioIdResponseDto usuario = usuarioService.getUsuarioById(id);
        return ResponseEntity.ok(usuario);
    }

    @PostMapping("/email")
    public ResponseEntity<Usuario> getUsuarioByEmail(@RequestBody LoginRequest loginRequest) {
        Usuario usuario = usuarioService.getUsuarioByEmail(loginRequest.email(), loginRequest.senha());
        return ResponseEntity.ok(usuario);
    }
}
