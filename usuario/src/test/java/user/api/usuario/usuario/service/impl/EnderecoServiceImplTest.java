package user.api.usuario.usuario.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import user.api.usuario.usuario.dtos.EnderecoDto.EnderecoDto;
import user.api.usuario.usuario.model.Endereco;
import user.api.usuario.usuario.model.Usuario;
import user.api.usuario.usuario.repository.EnderecoRepository;
import user.api.usuario.usuario.repository.UsuarioRepository;

/**
 * Testes unitários para a implementação do serviço {@link EnderecoServiceImpl}.
 * 
 * Esta classe contém os testes para verificar a correta implementação dos métodos
 * de gerenciamento de endereços na classe {@link EnderecoServiceImpl}.
 */
class EnderecoServiceImplTest {

    @InjectMocks
    private EnderecoServiceImpl enderecoService;

    @Mock
    private EnderecoRepository enderecoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    /**
     * Configura o ambiente para os testes, inicializando os mocks.
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Testa o salvamento bem-sucedido de um endereço.
     * 
     * Verifica se o endereço é salvo corretamente e se o DTO retornado é o esperado.
     * 
     * @throws Exception Se ocorrer algum erro durante o teste.
     */
    @Test
    void testSaveEndereco_Success() {
        UUID userId = UUID.randomUUID();
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(userId);

        EnderecoDto enderecoDto = new EnderecoDto("Rua A", "123", "Cidade B", "Estado C", "12345-678");
        Endereco endereco = new Endereco();
        endereco.setRua(enderecoDto.rua());
        endereco.setNumero(enderecoDto.numero());
        endereco.setCidade(enderecoDto.cidade());
        endereco.setEstado(enderecoDto.estado());
        endereco.setCep(enderecoDto.cep());
        endereco.setUsuario(usuario);

        when(usuarioRepository.findById(userId)).thenReturn(Optional.of(usuario));
        when(enderecoRepository.save(any(Endereco.class))).thenReturn(endereco);

        EnderecoDto result = enderecoService.saveEndereco(enderecoDto, userId);
        assertEquals(enderecoDto, result);
        verify(enderecoRepository).save(any(Endereco.class));
    }

    /**
     * Testa a recuperação de um endereço por ID com sucesso.
     * 
     * Verifica se o endereço é recuperado corretamente e se o DTO retornado é o esperado.
     * 
     * @throws Exception Se ocorrer algum erro durante o teste.
     */
    @Test
    void testGetEnderecoById_Success() {
        UUID userId = UUID.randomUUID();
        UUID enderecoId = UUID.randomUUID();
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(userId);

        Endereco endereco = new Endereco();
        endereco.setIdEndereco(enderecoId);
        endereco.setRua("Rua A");
        endereco.setNumero("123");
        endereco.setCidade("Cidade B");
        endereco.setEstado("Estado C");
        endereco.setCep("12345-678");
        endereco.setUsuario(usuario);

        when(enderecoRepository.findById(enderecoId)).thenReturn(Optional.of(endereco));

        Optional<EnderecoDto> result = enderecoService.getEnderecoById(enderecoId, userId);
        assertTrue(result.isPresent());
        assertEquals(new EnderecoDto("Rua A", "123", "Cidade B", "Estado C", "12345-678"), result.get());
    }

    /**
     * Testa a falha ao recuperar um endereço por ID.
     * 
     * Verifica se a ausência de um endereço é tratada corretamente.
     * 
     * @throws Exception Se ocorrer algum erro durante o teste.
     */
    @Test
    void testGetEnderecoById_Failure() {
        UUID userId = UUID.randomUUID();
        UUID enderecoId = UUID.randomUUID();

        when(enderecoRepository.findById(enderecoId)).thenReturn(Optional.empty());

        Optional<EnderecoDto> result = enderecoService.getEnderecoById(enderecoId, userId);
        assertFalse(result.isPresent());
    }

    /**
     * Testa a recuperação de todos os endereços de um usuário com sucesso.
     * 
     * Verifica se todos os endereços associados ao usuário são recuperados corretamente.
     * 
     * @throws Exception Se ocorrer algum erro durante o teste.
     */
    @Test
    void testGetAllEnderecosByUserId_Success() {
        UUID userId = UUID.randomUUID();
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(userId);

        Endereco endereco1 = new Endereco();
        endereco1.setRua("Rua A");
        endereco1.setNumero("123");
        endereco1.setCidade("Cidade B");
        endereco1.setEstado("Estado C");
        endereco1.setCep("12345-678");
        endereco1.setUsuario(usuario);

        Endereco endereco2 = new Endereco();
        endereco2.setRua("Rua D");
        endereco2.setNumero("456");
        endereco2.setCidade("Cidade E");
        endereco2.setEstado("Estado F");
        endereco2.setCep("98765-432");
        endereco2.setUsuario(usuario);

        when(usuarioRepository.findById(userId)).thenReturn(Optional.of(usuario));
        when(enderecoRepository.findAllByUsuario_IdUsuario(userId))
                .thenReturn(List.of(endereco1, endereco2));

        List<EnderecoDto> result = enderecoService.getAllEnderecosByUserId(userId);
        assertEquals(2, result.size());
        assertTrue(result.contains(new EnderecoDto("Rua A", "123", "Cidade B", "Estado C", "12345-678")));
        assertTrue(result.contains(new EnderecoDto("Rua D", "456", "Cidade E", "Estado F", "98765-432")));
    }

    /**
     * Testa a atualização de um endereço com sucesso.
     * 
     * Verifica se o endereço é atualizado corretamente e se o DTO retornado é o esperado.
     * 
     * @throws Exception Se ocorrer algum erro durante o teste.
     */
    @Test
    void testUpdateEndereco_Success() {
        UUID userId = UUID.randomUUID();
        UUID enderecoId = UUID.randomUUID();
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(userId);

        Endereco endereco = new Endereco();
        endereco.setIdEndereco(enderecoId);
        endereco.setRua("Rua Antiga");
        endereco.setNumero("789");
        endereco.setCidade("Cidade Antiga");
        endereco.setEstado("Estado Antigo");
        endereco.setCep("13579-246");
        endereco.setUsuario(usuario);

        EnderecoDto enderecoDto = new EnderecoDto("Rua Nova", "000", "Cidade Nova", "Estado Novo", "24680-135");
        Endereco enderecoAtualizado = new Endereco();
        enderecoAtualizado.setIdEndereco(enderecoId);
        enderecoAtualizado.setRua(enderecoDto.rua());
        enderecoAtualizado.setNumero(enderecoDto.numero());
        enderecoAtualizado.setCidade(enderecoDto.cidade());
        enderecoAtualizado.setEstado(enderecoDto.estado());
        enderecoAtualizado.setCep(enderecoDto.cep());
        enderecoAtualizado.setUsuario(usuario);

        when(enderecoRepository.findById(enderecoId)).thenReturn(Optional.of(endereco));
        when(enderecoRepository.save(any(Endereco.class))).thenReturn(enderecoAtualizado);

        EnderecoDto result = enderecoService.updateEndereco(enderecoId, enderecoDto, userId);
        assertEquals(enderecoDto, result);
    }

    /**
     * Testa a exclusão de um endereço com sucesso.
     * 
     * Verifica se o endereço é excluído corretamente.
     * 
     * @throws Exception Se ocorrer algum erro durante o teste.
     */
    @Test
    void testDeleteEndereco_Success() {
        UUID userId = UUID.randomUUID();
        UUID enderecoId = UUID.randomUUID();
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(userId);

        Endereco endereco = new Endereco();
        endereco.setIdEndereco(enderecoId);
        endereco.setRua("Rua A");
        endereco.setNumero("123");
        endereco.setCidade("Cidade B");
        endereco.setEstado("Estado C");
        endereco.setCep("12345-678");
        endereco.setUsuario(usuario);

        when(enderecoRepository.findById(enderecoId)).thenReturn(Optional.of(endereco));

        enderecoService.deleteEndereco(enderecoId, userId);
        verify(enderecoRepository).delete(endereco);
    }
}
