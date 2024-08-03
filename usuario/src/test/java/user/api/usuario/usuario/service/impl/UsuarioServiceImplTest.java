package user.api.usuario.usuario.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import io.awspring.cloud.sqs.operations.SqsTemplate;
import user.api.usuario.usuario.dtos.CriarUsuarioDto.UsuarioRequestDto;
import user.api.usuario.usuario.dtos.CriarUsuarioDto.UsuarioResponseDto;
import user.api.usuario.usuario.enums.Role;
import user.api.usuario.usuario.model.Usuario;
import user.api.usuario.usuario.repository.UsuarioRepository;
import user.api.usuario.usuario.service.S3Service;

public class UsuarioServiceImplTest {

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private S3Service s3Service;

    @Mock
    private SqsTemplate sqsTemplate;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSaveUsuario_ComImagem() {
        // Arrange
        UUID idUsuario = UUID.randomUUID();
        String nome = "Nome Teste";
        byte[] imagem = new byte[] { 1, 2, 3 };
        String email = "teste@email.com";
        String senha = "senha123";
        Set<Role> roles = Set.of(Role.USUARIO);
        LocalDate dataConta = LocalDate.now();
    
        // Usando uma chave esperada fixada no teste para simular o comportamento
        String chaveImagemEsperada = UUID.randomUUID() + ".jpg";
        String imagemUrlEsperada = "https://FotoPerfil.s3.amazonaws.com/" + chaveImagemEsperada;
    
        UsuarioRequestDto usuarioDto = new UsuarioRequestDto(nome, imagem, email, senha, roles, dataConta);
    
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(idUsuario);
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setSenha("senhaEncriptada");
        usuario.setRoles(roles);
        usuario.setDataConta(dataConta);
        usuario.setImagem(imagemUrlEsperada);
    
        when(passwordEncoder.encode(any(String.class))).thenReturn("senhaEncriptada");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        when(s3Service.uploadImage(any(String.class), any(byte[].class))).thenReturn(chaveImagemEsperada);
        when(s3Service.getImageUrl(any(String.class))).thenReturn(imagemUrlEsperada);
        when(sqsTemplate.send(any(String.class), any(String.class))).thenReturn(null);
    
        // Act
        UsuarioResponseDto response = usuarioService.saveUsuario(usuarioDto);
    
        // Assert
        assertNotNull(response);
        assertEquals(usuario.getNome(), response.nome());
        assertEquals(imagemUrlEsperada, response.imagem()); // Verifica a URL com a chave esperada
    
        verify(usuarioRepository).save(any(Usuario.class));
        verify(s3Service).uploadImage(any(String.class), any(byte[].class));
        verify(sqsTemplate).send(any(String.class), any(String.class));
    }
}