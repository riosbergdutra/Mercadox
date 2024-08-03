package user.api.usuario.usuario.service.impl;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import io.awspring.cloud.sqs.operations.SqsTemplate;
import user.api.usuario.usuario.dtos.CriarUsuarioDto.UsuarioRequestDto;
import user.api.usuario.usuario.dtos.CriarUsuarioDto.UsuarioResponseDto;
import user.api.usuario.usuario.enums.Role;
import user.api.usuario.usuario.model.Usuario;
import user.api.usuario.usuario.repository.UsuarioRepository;
import user.api.usuario.usuario.service.S3Service;

@Service
public class UsuarioServiceImpl {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private S3Service s3Service;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SqsTemplate sqsTemplate;

    public UsuarioResponseDto saveUsuario(UsuarioRequestDto usuarioDto) {
        Set<Role> rolesPermitidas = Set.of(Role.VENDEDOR, Role.USUARIO);

        Usuario novoUsuario = new Usuario();
        novoUsuario.setIdUsuario(UUID.randomUUID());
        novoUsuario.setNome(usuarioDto.nome());
        novoUsuario.setEmail(usuarioDto.email());
        novoUsuario.setSenha(passwordEncoder.encode(usuarioDto.senha()));
        novoUsuario.setRoles(rolesPermitidas);
        novoUsuario.setDataConta(LocalDate.now());

        // Verifica se a imagem foi fornecida
        if (usuarioDto.imagem() != null && usuarioDto.imagem().length > 0) {
            try {
                // Converte a imagem para byte[]
                byte[] imageContent = usuarioDto.imagem();
                String userId = novoUsuario.getIdUsuario().toString();
                String imagemKey = userId + "/" + UUID.randomUUID() + ".jpg";
                s3Service.uploadImage(imagemKey, imageContent);
                String imagemUrl = s3Service.getImageUrl(imagemKey);
                novoUsuario.setImagem(imagemUrl); // Armazena a URL completa da imagem
            } catch (Exception e) {
                throw new RuntimeException("Erro ao processar a imagem", e);
            }
        }

        Usuario usuarioCriado = usuarioRepository.save(novoUsuario);

        // Envia mensagem para a fila SQS
        String queueUrl = "http://localhost:4566/000000000000/usuarios";
        String messageBody = "idUsuario: " + usuarioCriado.getIdUsuario() + "\n" +
                "email: " + usuarioCriado.getEmail() + "\n" +
                "senha: " + usuarioCriado.getSenha();

        sqsTemplate.send(queueUrl, messageBody);

        return new UsuarioResponseDto(novoUsuario.getNome(), novoUsuario.getEmail(), novoUsuario.getImagem());
    }

    public Usuario getUsuarioById(UUID id) {
        return usuarioRepository.findById(id).orElse(null);
    }

    public Usuario getUsuarioByEmail(String email, String senha) {
        Usuario usuario = usuarioRepository.findByEmail(email);
        if (usuario != null && senha.equals(usuario.getSenha())) {
            return usuario;
        }
        return null;
    }
}
