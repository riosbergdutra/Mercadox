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
import user.api.usuario.usuario.dtos.MudarSenha.MudarSenhaRequest;
import user.api.usuario.usuario.enums.Role;
import user.api.usuario.usuario.model.Usuario;
import user.api.usuario.usuario.repository.UsuarioRepository;
import user.api.usuario.usuario.service.S3Service;
import user.api.usuario.usuario.service.UsuarioService;
import user.api.usuario.usuario.exceptions.UsuarioNotFoundException;
import user.api.usuario.usuario.exceptions.InvalidCredentialsException;

/**
 * Implementação do serviço de usuário, responsável por gerenciar operações
 * relacionadas a usuários.
 */
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

    /**
     * Salva um novo usuário no sistema.
     *
     * @param usuarioDto Dados do usuário a ser criado.
     * @return Dados do usuário criado.
     */
    @Override
    public UsuarioResponseDto saveUsuario(UsuarioRequestDto usuarioDto) {
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

        // Salva o novo usuário e obtém o usuário criado
        usuarioRepository.save(novoUsuario);

        // Retorna a resposta com os dados do usuário criado
        return new UsuarioResponseDto(novoUsuario.getNome(), novoUsuario.getEmail(), novoUsuario.getImagem());
    }

    /**
     * Recupera um usuário pelo ID e pelo userId.
     *
     * @param id     ID do usuário a ser recuperado.
     * @param userId ID do usuário autenticado.
     * @return Dados do usuário encontrado.
     */
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

    /**
     * Recupera um usuário pelo email e senha.
     *
     * @param email Email do usuário.
     * @param senha Senha do usuário.
     * @return Usuário correspondente às credenciais fornecidas.
     */
    @Override
    public Usuario getUsuarioByEmail(String email, String senha) {
        return Optional.ofNullable(usuarioRepository.findByEmail(email))
                .filter(usuario -> passwordEncoder.matches(senha, usuario.getSenha()))
                .orElseThrow(() -> new InvalidCredentialsException("invalid.credentials"));
    }

    /**
     * Altera a senha do usuário.
     *
     * @param id                ID do usuário.
     * @param userId            ID do usuário autenticado.
     * @param mudarSenhaRequest Dados para a mudança de senha.
     * @return Mensagem de sucesso se a senha for alterada com sucesso.
     */
    @Override
    public String mudarSenha(UUID id, UUID userId, MudarSenhaRequest mudarSenhaRequest) {
        return usuarioRepository.findById(id)
                .filter(u -> id.equals(userId))
                .map(u -> {
                    if (!passwordEncoder.matches(mudarSenhaRequest.SenhaAntiga(), u.getSenha())) {
                        throw new InvalidCredentialsException("password.not.sucess");
                    }
                    u.setSenha(passwordEncoder.encode(mudarSenhaRequest.SenhaNova()));
                    usuarioRepository.save(u);
                    return "password.success";
                })
                .orElseThrow(() -> new UsuarioNotFoundException("user.not.found"));
    }

    /**
     * Exclui um usuário do sistema.
     *
     * @param id     ID do usuário a ser excluído.
     * @param userId ID do usuário autenticado.
     * @return Mensagem de sucesso se o usuário for excluído com sucesso.
     */
    @Override
    public String deleteUsuario(UUID id, UUID userId) {
        return usuarioRepository.findById(id)
                .filter(usuario -> id.equals(userId))
                .map(usuario -> {
                    // Exclui o usuário do banco de dados
                    usuarioRepository.delete(usuario);

                    // Envia uma mensagem para a fila SQS
                    String queueUrl = "http://localhost:4566/000000000000/usuario";
                    String messageBody = "Usuário atualizado pelo ID: " + id;
                    sqsTemplate.send(queueUrl, messageBody);

                    return "user.deleted.success";
                })
                .orElseThrow(() -> new UsuarioNotFoundException("user.not.found"));
    }

}