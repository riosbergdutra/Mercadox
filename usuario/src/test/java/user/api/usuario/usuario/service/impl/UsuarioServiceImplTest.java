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
import user.api.usuario.usuario.dtos.CriarUsuarioDto.UsuarioRequestDto;
import user.api.usuario.usuario.dtos.CriarUsuarioDto.UsuarioResponseDto;
import user.api.usuario.usuario.dtos.MudarSenha.MudarSenhaRequest;
import user.api.usuario.usuario.enums.Role;
import user.api.usuario.usuario.model.Usuario;
import user.api.usuario.usuario.repository.UsuarioRepository;
import user.api.usuario.usuario.service.S3Service;
import user.api.usuario.usuario.exceptions.UsuarioNotFoundException;
import user.api.usuario.usuario.exceptions.InvalidCredentialsException;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class UsuarioServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private S3Service s3Service;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SqsTemplate sqsTemplate;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

   
    @Test
    void saveUsuario_ShouldSaveAndReturnUserResponseDto() throws IOException {
        // Arrange
        MultipartFile imagem = new MockMultipartFile("imagem", "imagem.jpg", "image/jpeg", "imagemConteudo".getBytes());
        UsuarioRequestDto usuarioDto = new UsuarioRequestDto("Nome", imagem, "email@example.com", "senha", Role.VENDEDOR);
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(UUID.randomUUID());
        usuario.setNome("Nome");
        usuario.setEmail("email@example.com");
        usuario.setSenha("senhaCodificada");
        usuario.setRole(Role.VENDEDOR);
        usuario.setDataConta(LocalDate.now());

        when(passwordEncoder.encode(usuarioDto.senha())).thenReturn("senhaCodificada");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        when(s3Service.uploadImagemS3(anyString(), any())).thenReturn("imagemUrl");

        // Act
        UsuarioResponseDto response = usuarioService.saveUsuario(usuarioDto);

        // Assert
        assertNotNull(response);
        assertEquals("Nome", response.nome());
        assertEquals("email@example.com", response.email());
        assertEquals("imagemUrl", response.imagem());
    }

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

    @Test
    void getUsuarioByEmail_ShouldReturnUsuario() {
        // Arrange
        String email = "email@example.com";
        String senha = "senha";
        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setSenha("senhaCodificada");

        when(usuarioRepository.findByEmail(email)).thenReturn(usuario);
        when(passwordEncoder.matches(senha, "senhaCodificada")).thenReturn(true);

        // Act
        Usuario result = usuarioService.getUsuarioByEmail(email, senha);

        // Assert
        assertNotNull(result);
        assertEquals(email, result.getEmail());
    }

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
        assertEquals("Senha alterada com sucesso", result);
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void deleteUsuario_ShouldDeleteAndSendMessage() {
        // Arrange
        UUID id = UUID.randomUUID();
        UUID userId = id;
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(id);

        when(usuarioRepository.findById(id)).thenReturn(Optional.of(usuario));

        // Act
        String result = usuarioService.deleteUsuario(id, userId);

        // Assert
        assertEquals("user.deleted.success", result);
        verify(usuarioRepository).delete(usuario);
        verify(sqsTemplate).send(anyString(), anyString());
    }

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
        assertEquals("Usuário não encontrado", exception.getMessage());
    }

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
        assertEquals("Credenciais inválidas", exception.getMessage());
    }

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
        assertEquals("Usuário não encontrado", exception.getMessage());
    }
}
