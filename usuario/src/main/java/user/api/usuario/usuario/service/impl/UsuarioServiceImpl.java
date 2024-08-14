package user.api.usuario.usuario.service.impl;

import java.io.IOException;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import io.awspring.cloud.sqs.operations.SqsTemplate;
import user.api.usuario.usuario.dtos.AcharUsuarioIdDto.UsuarioIdResponseDto;
import user.api.usuario.usuario.dtos.CriarUsuarioDto.UsuarioRequestDto;
import user.api.usuario.usuario.dtos.CriarUsuarioDto.UsuarioResponseDto;
import user.api.usuario.usuario.dtos.MudarSenha.MudarSenhaRequest;
import user.api.usuario.usuario.enums.Role;
import user.api.usuario.usuario.model.Usuario;
import user.api.usuario.usuario.repository.UsuarioRepository;
import user.api.usuario.usuario.service.S3Service;
import user.api.usuario.usuario.service.UsuarioService;
import user.api.usuario.usuario.exceptions.UsuarioNotFoundException;
import user.api.usuario.usuario.exceptions.InvalidCredentialsException;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private S3Service s3Service;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SqsTemplate sqsTemplate;

    public UsuarioResponseDto saveUsuario(UsuarioRequestDto usuarioDto) {

        Usuario novoUsuario = new Usuario();
        novoUsuario.setIdUsuario(UUID.randomUUID());
        novoUsuario.setNome(usuarioDto.nome());
        novoUsuario.setEmail(usuarioDto.email());
        novoUsuario.setSenha(passwordEncoder.encode(usuarioDto.senha()));
        novoUsuario.setRole(usuarioDto.role());
        novoUsuario.setDataConta(LocalDate.now());

        if (usuarioDto.role() == Role.VENDEDOR && usuarioDto.imagem() != null && !usuarioDto.imagem().isEmpty()) {
            String userId = novoUsuario.getIdUsuario().toString();
            String imagemKey = userId + "/" + UUID.randomUUID() + ".jpg";
            try {
                String imagemUrl = s3Service.uploadImagemS3(imagemKey, usuarioDto.imagem()); // Passando MultipartFile
                novoUsuario.setImagem(imagemUrl);
            } catch (IOException e) {
                throw new RuntimeException("Erro ao fazer upload da imagem", e);
            }
        }

        Usuario usuarioCriado = usuarioRepository.save(novoUsuario);

        String queueUrl = "http://localhost:4566/000000000000/usuarios";
        String messageBody = String.format("idUsuario: %s\nemail: %s\nsenha: %s",
                usuarioCriado.getIdUsuario(), usuarioCriado.getEmail(), usuarioCriado.getSenha());

        sqsTemplate.send(queueUrl, messageBody);

        return new UsuarioResponseDto(novoUsuario.getNome(), novoUsuario.getEmail(), novoUsuario.getImagem());
    }

    public UsuarioIdResponseDto getUsuarioById(UUID id, UUID userId) {
        return usuarioRepository.findById(id)
                .filter(usuario -> id.equals(userId))
                .map(usuario -> new UsuarioIdResponseDto(
                        usuario.getNome(),
                        usuario.getEmail(),
                        usuario.getSenha(),
                        usuario.getImagem(),
                        usuario.getDataConta()))
                .orElseThrow(() -> new UsuarioNotFoundException("Usuário não encontrado"));
    }

    public Usuario getUsuarioByEmail(String email, String senha) {
        Usuario usuario = usuarioRepository.findByEmail(email);
        if (usuario != null && passwordEncoder.matches(senha, usuario.getSenha())) {
            return usuario;
        } else {
            throw new InvalidCredentialsException("Credenciais inválidas");
        }
    }

    

    public String mudarSenha(UUID id, UUID userId, MudarSenhaRequest mudarSenhaRequest) {
        usuarioRepository.findById(id)
                .filter(u -> id.equals(userId))
                .map(u -> {
                    if (!passwordEncoder.matches(mudarSenhaRequest.SenhaAntiga(), u.getSenha())) {
                        throw new InvalidCredentialsException("Senha antiga incorreta");
                    }
                    u.setSenha(passwordEncoder.encode(mudarSenhaRequest.SenhaNova()));
                    return usuarioRepository.save(u);
                })
                .orElseThrow(
                        () -> new UsuarioNotFoundException("Usuário não encontrado"));

        return "Senha alterada com sucesso";
    }
}
