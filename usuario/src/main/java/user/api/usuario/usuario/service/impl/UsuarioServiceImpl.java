package user.api.usuario.usuario.service.impl;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import io.awspring.cloud.sqs.operations.SqsTemplate;
import user.api.usuario.usuario.dtos.AcharUsuarioIdDto.UsuarioIdResponseDto;
import user.api.usuario.usuario.dtos.CriarUsuarioDto.UsuarioRequestDto;
import user.api.usuario.usuario.dtos.CriarUsuarioDto.UsuarioResponseDto;
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
        String messageBody = "idUsuario: " + usuarioCriado.getIdUsuario() + "\n" +
                "email: " + usuarioCriado.getEmail() + "\n" +
                "senha: " + usuarioCriado.getSenha();

        sqsTemplate.send(queueUrl, messageBody);

        return new UsuarioResponseDto(novoUsuario.getNome(), novoUsuario.getEmail(), novoUsuario.getImagem());
    }

    public UsuarioIdResponseDto getUsuarioById(UUID id) {
        Optional<Usuario> optionalUsuario = usuarioRepository.findById(id);
        if (optionalUsuario.isPresent()) {
            Usuario usuario = optionalUsuario.get();
            return new UsuarioIdResponseDto(
                    usuario.getNome(),
                    usuario.getEmail(),
                    usuario.getSenha(),
                    usuario.getImagem(),
                    usuario.getDataConta());
        } else {
            throw new UsuarioNotFoundException("Usuário não encontrado com o id: " + id);
        }
    }

    public Usuario getUsuarioByEmail(String email, String senha) {
        Usuario usuario = usuarioRepository.findByEmail(email);
        if (usuario != null && passwordEncoder.matches(senha, usuario.getSenha())) {
            return usuario;
        } else {
            throw new InvalidCredentialsException("Credenciais inválidas");
        }
    }

}
