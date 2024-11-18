package user.api.usuario.usuario.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import user.api.usuario.usuario.dtos.EnderecoDto.EnderecoDto;
import user.api.usuario.usuario.dtos.enderecosemid.EnderecoSemId;
import user.api.usuario.usuario.service.EnderecoService;

/**
 * Controlador para gerenciar operações relacionadas aos endereços.
 * 
 * Fornece endpoints para criar, consultar, atualizar e excluir endereços.
 */
@RestController
@RequestMapping("/endereco")
public class EnderecoController {

    @Autowired
    private EnderecoService enderecoService;

    /**
 * Endpoint para recuperar todos os endereços associados ao usuário autenticado.
 * 
 * @param userId         ID do usuário.
 * @param authentication Objeto de autenticação contendo o ID do usuário logado.
 * @return ResponseEntity com uma lista de DTOs de endereços e status 200 (OK).
 */
@GetMapping("/{userId}")
public ResponseEntity<List<EnderecoDto>> getAllEnderecosByUser(
        @PathVariable UUID userId,
        Authentication authentication) {

    // Valida se o ID do usuário na URL corresponde ao usuário autenticado
    validarUsuarioAutenticado(userId, authentication);

    // Recupera todos os endereços do usuário
    List<EnderecoDto> enderecos = enderecoService.getAllEnderecosByUserId(userId);

    // Retorna a lista de endereços com status 200 (OK)
    return ResponseEntity.ok(enderecos);
}
    /**
     * Endpoint para criar um novo endereço para o usuário autenticado.
     * 
     * @param userId         ID do usuário.
     * @param enderecoDto    DTO contendo os dados do endereço a ser criado.
     * @param authentication Objeto de autenticação contendo o ID do usuário logado.
     * @return ResponseEntity com o DTO do endereço criado e status 201 (CREATED).
     */
    @PostMapping("/{userId}/criar")
    public ResponseEntity<EnderecoSemId> criarEndereco(
            @PathVariable UUID userId,
            @Valid @RequestBody EnderecoSemId enderecoDto,
            Authentication authentication) {

        validarUsuarioAutenticado(userId, authentication);
        EnderecoSemId resposta = enderecoService.saveEndereco(enderecoDto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    /**
     * Endpoint para obter um endereço por ID, se ele pertencer ao usuário
     * autenticado.
     * 
     * @param userId         ID do usuário.
     * @param idEndereco             ID do endereço a ser consultado.
     * @param authentication Objeto de autenticação contendo o ID do usuário logado.
     * @return ResponseEntity com o DTO do endereço e status 200 (OK).
     */
    @GetMapping("/{userId}/{idEndereco}")
    public ResponseEntity<EnderecoDto> getEnderecoById(
            @PathVariable UUID userId,
            @PathVariable UUID idEndereco,
            Authentication authentication) {

        validarUsuarioAutenticado(userId, authentication);
        EnderecoDto endereco = enderecoService.getEnderecoById(idEndereco, userId)
                .orElseThrow(() -> new RuntimeException("Endereco not found or not associated with user"));
        return ResponseEntity.ok(endereco);
    }

    /**
     * Endpoint para recuperar todos os endereços associados ao usuário autenticado.
     * 
     * @param userId         ID do usuário.
     * @param authentication Objeto de autenticação contendo o ID do usuário logado.
     * @return ResponseEntity com uma lista de DTOs de endereços e status 200 (OK).
     */
     

    /**
     * Endpoint para atualizar um endereço existente associado ao usuário
     * autenticado.
     * 
     * @param userId         ID do usuário.
     * @param id             ID do endereço a ser atualizado.
     * @param enderecoDto    DTO com os dados atualizados do endereço.
     * @param authentication Objeto de autenticação contendo o ID do usuário logado.
     * @return ResponseEntity com o DTO do endereço atualizado e status 200 (OK).
     */
    @PutMapping("/{userId}/{idEndereco}")
    public ResponseEntity<EnderecoSemId> atualizarEndereco(
            @PathVariable UUID userId,
            @PathVariable UUID idEndereco,
            @Valid @RequestBody EnderecoSemId enderecoDto,
            Authentication authentication) {

        validarUsuarioAutenticado(userId, authentication);
        EnderecoSemId enderecoAtualizado = enderecoService.updateEndereco(idEndereco, enderecoDto, userId);
        return ResponseEntity.ok(enderecoAtualizado);
    }

    /**
     * Endpoint para excluir um endereço existente associado ao usuário autenticado.
     * 
     * @param userId         ID do usuário.
     * @param id             ID do endereço a ser excluído.
     * @param authentication Objeto de autenticação contendo o ID do usuário logado.
     * @return ResponseEntity com uma mensagem de sucesso e status 200 (OK).
     */
    @DeleteMapping("/{userId}/{idEndereco}")
    public ResponseEntity<String> excluirEndereco(
            @PathVariable UUID userId,
            @PathVariable UUID idEndereco,
            Authentication authentication) {

        validarUsuarioAutenticado(userId, authentication);
        enderecoService.deleteEndereco(idEndereco, userId);
        return ResponseEntity.ok("Endereco excluído com sucesso");
    }

    /**
     * Valida se o ID do usuário na URL corresponde ao usuário autenticado.
     * 
     * @param userId         ID do usuário na URL.
     * @param authentication Objeto de autenticação contendo o ID do usuário logado.
     */
    private void validarUsuarioAutenticado(UUID userId, Authentication authentication) {
        UUID authenticatedUserId = UUID.fromString(authentication.getName());
        if (!authenticatedUserId.equals(userId)) {
            throw new RuntimeException("Usuário não autorizado");
        }
    }
}
