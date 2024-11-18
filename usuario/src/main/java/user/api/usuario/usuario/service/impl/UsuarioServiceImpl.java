package user.api.usuario.usuario.service.impl;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import io.awspring.cloud.sqs.operations.SqsTemplate;
import user.api.usuario.usuario.dtos.AcharUsuarioIdDto.UsuarioIdResponseDto;
import user.api.usuario.usuario.dtos.AcharUsuarioIdDto.UsuarioTokenResponse;
import user.api.usuario.usuario.dtos.AcharUsuarioPorEmail.UsuarioEmailDto;
import user.api.usuario.usuario.dtos.CriarUsuarioDto.UsuarioRequestDto;
import user.api.usuario.usuario.dtos.CriarUsuarioDto.UsuarioResponseDto;
import user.api.usuario.usuario.dtos.MudarSenha.MudarSenhaRequest;
import user.api.usuario.usuario.dtos.enderecosemid.EnderecoSemId;
import user.api.usuario.usuario.enums.Role;
import user.api.usuario.usuario.model.Endereco;
import user.api.usuario.usuario.model.Usuario;
import user.api.usuario.usuario.repository.EnderecoRepository;
import user.api.usuario.usuario.repository.UsuarioRepository;
import user.api.usuario.usuario.service.S3Service;
import user.api.usuario.usuario.service.UsuarioService;
import user.api.usuario.usuario.exceptions.UsuarioNotFoundException;
import user.api.usuario.usuario.exceptions.InvalidCredentialsException;
import user.api.usuario.usuario.exceptions.S3ImageDeletionException;

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

    private EnderecoRepository enderecoRepository;

    /**
     * Salva um novo usuário no sistema.
     *
     * @param usuarioDto Dados do usuário a ser criado.
     * @return Dados do usuário criado.
     */
    @Override
    public UsuarioResponseDto saveUsuario(UsuarioRequestDto usuarioDto) {
        Usuario novoUsuario = new Usuario();
    novoUsuario.setNome(usuarioDto.nome());
    novoUsuario.setEmail(usuarioDto.email());
    novoUsuario.setSenha(passwordEncoder.encode(usuarioDto.senha()));
    novoUsuario.setRole(usuarioDto.role());
    novoUsuario.setDataConta(LocalDate.now());

    // Configurar e salvar o endereço
    Endereco endereco = new Endereco();
    EnderecoSemId dto = usuarioDto.endereco();
    endereco.setRua(dto.rua());
    endereco.setNumero(dto.numero());
    endereco.setCidade(dto.cidade());
    endereco.setEstado(dto.estado());
    endereco.setCep(dto.cep());
    endereco.setUsuario(novoUsuario); // Associa o endereço ao usuário

    novoUsuario.setEnderecos(List.of(endereco)); // Coloque o endereço em uma lista

    // Manipulação de imagem apenas se o usuário for VENDEDOR
    if (usuarioDto.role() == Role.VENDEDOR) {
        Optional.ofNullable(usuarioDto.imagem())
                .filter(imagem -> !imagem.isEmpty())
                .ifPresent(imagem -> {
                    String imagemKey = novoUsuario.getIdUsuario() + "_" + UUID.randomUUID() + ".jpg"; // Inclui o ID do usuário na chave
                    try {
                        String imagemUrl = s3Service.uploadImagemS3(imagemKey, imagem);
                        novoUsuario.setImagem(imagemUrl);
                    } catch (IOException e) {
                        throw new RuntimeException("error.upload", e);
                    }
                });
    }

        // Salva o novo usuário com endereços
        usuarioRepository.save(novoUsuario);

        // Envia uma mensagem para a fila SQS após a criação do usuário
        String queueUrl = "http://localhost:4566/000000000000/usuario"; // URL da fila SQS para criação de usuário
        String messageBody = "idUsuario: " + novoUsuario.getIdUsuario() + "\n" +
                "acao: CRIAR";
        sqsTemplate.send(queueUrl, messageBody);

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

    @Override
    public UsuarioTokenResponse getUsuarioForToken(UUID id) {
        return usuarioRepository.findById(id)
                .map(usuario -> new UsuarioTokenResponse(
                        usuario.getIdUsuario(),
                        usuario.getRole()))
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
    public UsuarioEmailDto getUsuarioByEmail(String email, String senha) {
        Usuario usuario = Optional.ofNullable(usuarioRepository.findByEmail(email))
                .filter(u -> passwordEncoder.matches(senha, u.getSenha()))
                .orElseThrow(() -> new InvalidCredentialsException("invalid.credentials"));

        // Mapear Usuario para UsuarioEmailDto
        return new UsuarioEmailDto(usuario.getIdUsuario(), usuario.getEmail(), usuario.getSenha(), usuario.getRole());
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
     * Exclui uma conta de usuário.
     *
     * @param id     ID do usuário a ser excluído.
     * @param userId ID do usuário autenticado.
     * @return Mensagem de sucesso se a conta for excluída com sucesso.
     */
    @Override
    public String deleteUsuario(UUID id, UUID userId) {
        // Recupera o usuário a partir do ID e verifica se o ID coincide com o ID do
        // usuário autenticado
        Usuario usuario = usuarioRepository.findById(id)
                .filter(u -> id.equals(userId))
                .orElseThrow(() -> new UsuarioNotFoundException("user.not.found"));

        // Exclui a imagem do S3, se houver
        if (usuario.getImagem() != null && !usuario.getImagem().isEmpty()) {
            // Extrai a chave da imagem do URL
            String imagemKey = usuario.getImagem().substring(usuario.getImagem().lastIndexOf("/") + 1);
            try {
                s3Service.deleteImagemS3(imagemKey);
            } catch (S3ImageDeletionException e) {
                // Tratar a falha na exclusão da imagem
                throw new RuntimeException("Falha ao excluir a imagem do S3", e);
            }
        }

        // Exclui os endereços associados ao usuário
        if (usuario.getEnderecos() != null) {
            for (Endereco endereco : usuario.getEnderecos()) {
                enderecoRepository.delete(endereco);
            }
        }

        // Exclui o usuário do banco de dados
        usuarioRepository.delete(usuario);

        // Envia uma mensagem para a fila SQS
        String queueUrl = "http://localhost:4566/000000000000/usuario";
        String messageBody = "IdUsuario: " + usuario.getIdUsuario() + "\n" +
                "acao: DELETAR";
        ;
        sqsTemplate.send(queueUrl, messageBody);

        return "user.deleted.success";
    }
}
