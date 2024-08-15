package user.api.usuario.usuario.service.impl;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ModelAttribute;

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

    @Override
    public UsuarioResponseDto saveUsuario(@ModelAttribute UsuarioRequestDto usuarioDto) {
        Usuario novoUsuario = new Usuario();
        novoUsuario.setIdUsuario(UUID.randomUUID());
        novoUsuario.setNome(usuarioDto.nome());
        novoUsuario.setEmail(usuarioDto.email());
        novoUsuario.setSenha(passwordEncoder.encode(usuarioDto.senha()));
        novoUsuario.setRole(usuarioDto.role());
        novoUsuario.setDataConta(LocalDate.now());

        // Manipulação de imagem apenas se o usuário for VENDEDOR
        if (usuarioDto.role() == Role.VENDEDOR) {
            Optional.ofNullable(usuarioDto.imagem())
                    .filter(imagem -> !imagem.isEmpty())
                    .ifPresent(imagem -> {
                        String userId = novoUsuario.getIdUsuario().toString();
                        String imagemKey = userId + "/" + UUID.randomUUID() + ".jpg";
                        try {
                            String imagemUrl = s3Service.uploadImagemS3(imagemKey, imagem);
                            novoUsuario.setImagem(imagemUrl);
                        } catch (IOException e) {
                            throw new RuntimeException("error.upload", e);
                        }
                    });
        }

        // Salva o novo usuário
        Usuario usuarioCriado = usuarioRepository.save(novoUsuario);

        // Verifica se o usuário foi salvo com sucesso antes de enviar a mensagem
        if (usuarioCriado != null) {
            String queueUrl = "http://localhost:4566/000000000000/usuarios";
            String messageBody = String.format("idUsuario: %s\nemail: %s\nsenha: %s",
                    usuarioCriado.getIdUsuario(), usuarioCriado.getEmail(), usuarioCriado.getSenha());

            // Envia a mensagem para a fila SQS
            sqsTemplate.send(queueUrl, messageBody);
        } else {
            throw new RuntimeException("failed.save.user");
        }

        // Retorna a resposta com os dados do usuário criado
        return new UsuarioResponseDto(novoUsuario.getNome(), novoUsuario.getEmail(), novoUsuario.getImagem());
    }

    @Override
    public UsuarioIdResponseDto getUsuarioById(UUID id, UUID userId) {
        return usuarioRepository.findById(id)
                .filter(usuario -> id.equals(userId))
                .map(usuario -> new UsuarioIdResponseDto(
                        usuario.getNome(),
                        usuario.getEmail(),
                        usuario.getSenha(),
                        usuario.getImagem(),
                        usuario.getDataConta()))
                .orElseThrow(() -> new UsuarioNotFoundException("user.not.found"));
    }

    @Override
    public Usuario getUsuarioByEmail(String email, String senha) {
        return Optional.ofNullable(usuarioRepository.findByEmail(email))
                .filter(usuario -> passwordEncoder.matches(senha, usuario.getSenha()))
                .orElseThrow(() -> new InvalidCredentialsException("invalid.credentials"));
    }

    @Override
    public String mudarSenha(UUID id, UUID userId, MudarSenhaRequest mudarSenhaRequest) {
        return usuarioRepository.findById(id)
                .filter(u -> id.equals(userId))
                .map(u -> {
                    if (!passwordEncoder.matches(mudarSenhaRequest.SenhaAntiga(), u.getSenha())) {
                        throw new InvalidCredentialsException("Senha antiga incorreta");
                    }
                    u.setSenha(passwordEncoder.encode(mudarSenhaRequest.SenhaNova()));
                    usuarioRepository.save(u);
                    return "password.success";
                })
                .orElseThrow(() -> new UsuarioNotFoundException("user.not.found"));
    }

}
