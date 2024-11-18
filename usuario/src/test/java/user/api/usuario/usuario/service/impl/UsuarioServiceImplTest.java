package user.api.usuario.usuario.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import io.awspring.cloud.sqs.operations.SqsTemplate;
import user.api.usuario.usuario.dtos.AcharUsuarioIdDto.UsuarioIdResponseDto;
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
import user.api.usuario.usuario.exceptions.UsuarioNotFoundException;
import user.api.usuario.usuario.exceptions.InvalidCredentialsException;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes para o serviço UsuarioServiceImpl.
 * 
 * Os testes verificam a funcionalidade do serviço de usuário, incluindo criação, obtenção, atualização e exclusão de usuários.
 */
public class UsuarioServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private S3Service s3Service;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SqsTemplate sqsTemplate;

    @Mock
    private EnderecoRepository enderecoRepository;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Testa a criação e o salvamento de um usuário.
     * Verifica se o usuário é salvo corretamente e se a URL da imagem é retornada como esperado.
     */
    @Test
    void saveUsuario_ShouldSaveAndReturnUserResponseDto() throws IOException {
        // Arrange
        MultipartFile imagem = new MockMultipartFile("imagem", "imagem.jpg", "image/jpeg", "imagemConteudo".getBytes());
        EnderecoSemId enderecoDto = new EnderecoSemId("Rua 1", "123", "Cidade A", "Estado A", "12345-678");
        UsuarioRequestDto usuarioDto = new UsuarioRequestDto("Nome", imagem, "email@example.com", "senha", Role.VENDEDOR, enderecoDto);

        Usuario usuario = new Usuario();
        usuario.setIdUsuario(UUID.randomUUID());
        usuario.setNome("Nome");
        usuario.setEmail("email@example.com");
        usuario.setSenha("senhaCodificada");
        usuario.setRole(Role.VENDEDOR);
        usuario.setDataConta(LocalDate.now());
        usuario.setEnderecos(List.of(new Endereco())); // Configuração simplificada para o teste

        // Mockando a codificação da senha
        when(passwordEncoder.encode(usuarioDto.senha())).thenReturn("senhaCodificada");

        // Mockando o upload da imagem
        when(s3Service.uploadImagemS3(anyString(), any())).thenReturn("imagemUrl");

        // Mockando o salvamento do usuário
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        // Act
        UsuarioResponseDto response = usuarioService.saveUsuario(usuarioDto);

        // Assert
        assertNotNull(response);
        assertEquals("Nome", response.nome());
        assertEquals("email@example.com", response.email());
        assertEquals("imagemUrl", response.imagem());

        // Verificação se o usuário com o endereço foi salvo corretamente
        verify(usuarioRepository).save(argThat(u -> {
            List<Endereco> enderecosSalvos = u.getEnderecos();
            return enderecosSalvos != null && !enderecosSalvos.isEmpty() &&
                   enderecosSalvos.stream().anyMatch(e -> "Rua 1".equals(e.getRua()) &&
                                                           "123".equals(e.getNumero()) &&
                                                           "Cidade A".equals(e.getCidade()) &&
                                                           "Estado A".equals(e.getEstado()) &&
                                                           "12345-678".equals(e.getCep()));
        }));
    }

    /**
     * Testa a recuperação de um usuário pelo ID.
     * Verifica se o usuário é retornado corretamente quando encontrado.
     */
    @Test
    void getUsuarioById_ShouldReturnUsuarioIdResponseDto() {
        // Arrange
        UUID id = UUID.randomUUID();
        UUID userId = id;
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(id);
        usuario.setNome("Nome");
        usuario.setEmail("email@example.com");
        usuario.setSenha("senha");
        usuario.setImagem("imagemUrl");
        usuario.setDataConta(LocalDate.now());

        when(usuarioRepository.findById(id)).thenReturn(Optional.of(usuario));

        // Act
        UsuarioIdResponseDto response = usuarioService.getUsuarioById(id, userId);

        // Assert
        assertNotNull(response);
        assertEquals("Nome", response.nome());
        assertEquals("email@example.com", response.email());
    }

    /**
     * Testa a recuperação de um usuário pelo e-mail e senha.
     * Verifica se os detalhes do usuário são retornados corretamente quando as credenciais são válidas.
     */
    @Test
    void getUsuarioByEmail_ShouldReturnUsuarioEmailDto() {
        // Arrange
        String email = "email@example.com";
        String senha = "senha";
        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setSenha("senhaCodificada");
        usuario.setIdUsuario(UUID.randomUUID());
        usuario.setRole(Role.USUARIO);

        when(usuarioRepository.findByEmail(email)).thenReturn(usuario);
        when(passwordEncoder.matches(senha, "senhaCodificada")).thenReturn(true);

        // Act
        UsuarioEmailDto response = usuarioService.getUsuarioByEmail(email, senha);

        // Assert
        assertNotNull(response);
        assertEquals(usuario.getIdUsuario(), response.idUsuario());
        assertEquals(email, response.email());
        assertEquals(usuario.getRole(), response.role());
    }

    /**
     * Testa a mudança de senha de um usuário.
     * Verifica se a senha é atualizada corretamente quando as credenciais são válidas.
     */
    @Test
    void mudarSenha_ShouldChangePasswordSuccessfully() {
        // Arrange
        UUID id = UUID.randomUUID();
        UUID userId = id;
        MudarSenhaRequest request = new MudarSenhaRequest("senhaAntiga", "senhaNova");

        Usuario usuario = new Usuario();
        usuario.setIdUsuario(id);
        usuario.setSenha("senhaCodificada");

        when(usuarioRepository.findById(id)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches(request.SenhaAntiga(), "senhaCodificada")).thenReturn(true);
        when(passwordEncoder.encode(request.SenhaNova())).thenReturn("senhaNovaCodificada");

        // Act
        String result = usuarioService.mudarSenha(id, userId, request);

        // Assert
        assertEquals("password.success", result);
        verify(usuarioRepository).save(argThat(u -> "senhaNovaCodificada".equals(u.getSenha())));
    }

    /**
     * Testa a exclusão de um usuário.
     * Verifica se o usuário é excluído corretamente e se as ações associadas são executadas (e.g., exclusão da imagem, envio de mensagem para SQS).
     */
    @Test
    void deleteUsuario_ShouldDeleteAndSendMessage() {
        // Arrange
        UUID id = UUID.randomUUID();
        UUID userId = id;
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(id);
        usuario.setImagem("imagemUrl");
        usuario.setEnderecos(List.of(new Endereco()));

        when(usuarioRepository.findById(id)).thenReturn(Optional.of(usuario));
        doNothing().when(enderecoRepository).delete(any(Endereco.class));

        // Mockando a exclusão da imagem
        doNothing().when(s3Service).deleteImagemS3(anyString());

        // Act
        String result = usuarioService.deleteUsuario(id, userId);

        // Assert
        assertEquals("user.deleted.success", result);
        verify(usuarioRepository).delete(usuario);
        verify(sqsTemplate).send(anyString(), anyString());
        verify(s3Service).deleteImagemS3("imagemUrl".substring("imagemUrl".lastIndexOf("/") + 1));
        verify(enderecoRepository, times(1)).delete(any(Endereco.class));
    }

    /**
     * Testa a recuperação de um usuário pelo ID quando o usuário não é encontrado.
     * Verifica se a exceção UsuarioNotFoundException é lançada corretamente.
     */
    @Test
    void getUsuarioById_ShouldThrowUsuarioNotFoundException() {
        // Arrange
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(usuarioRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        UsuarioNotFoundException exception = assertThrows(UsuarioNotFoundException.class, () -> {
            usuarioService.getUsuarioById(id, userId);
        });
        assertEquals("user.not.found", exception.getMessage());
    }

    /**
     * Testa a recuperação de um usuário pelo e-mail quando o e-mail não é encontrado.
     * Verifica se a exceção InvalidCredentialsException é lançada corretamente.
     */
    @Test
    void getUsuarioByEmail_ShouldThrowInvalidCredentialsException() {
        // Arrange
        String email = "email@example.com";
        String senha = "senha";
        when(usuarioRepository.findByEmail(email)).thenReturn(null);

        // Act & Assert
        InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class, () -> {
            usuarioService.getUsuarioByEmail(email, senha);
        });
        assertEquals("invalid.credentials", exception.getMessage());
    }

    /**
     * Testa a mudança de senha quando o usuário não é encontrado.
     * Verifica se a exceção UsuarioNotFoundException é lançada corretamente.
     */
    @Test
    void mudarSenha_ShouldThrowUsuarioNotFoundException() {
        // Arrange
        UUID id = UUID.randomUUID();
        UUID userId = id;
        MudarSenhaRequest request = new MudarSenhaRequest("senhaAntiga", "senhaNova");

        when(usuarioRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        UsuarioNotFoundException exception = assertThrows(UsuarioNotFoundException.class, () -> {
            usuarioService.mudarSenha(id, userId, request);
        });
        assertEquals("user.not.found", exception.getMessage());
    }
}
