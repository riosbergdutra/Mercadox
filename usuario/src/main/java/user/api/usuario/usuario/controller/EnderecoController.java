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
     * Endpoint para criar um novo endereço para o usuário autenticado.
     * 
     * @param enderecoDto DTO contendo os dados do endereço a ser criado.
     * @param authentication Objeto de autenticação contendo o ID do usuário logado.
     * @return ResponseEntity com o DTO do endereço criado e status 201 (CREATED).
     */
    @PostMapping("/criar")
    public ResponseEntity<EnderecoDto> criarEndereco(
            @Valid @RequestBody EnderecoDto enderecoDto,
            Authentication authentication) {

        UUID userId = UUID.fromString(authentication.getName());
        EnderecoDto resposta = enderecoService.saveEndereco(enderecoDto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    /**
     * Endpoint para obter um endereço por ID, se ele pertencer ao usuário autenticado.
     * 
     * @param id ID do endereço a ser consultado.
     * @param authentication Objeto de autenticação contendo o ID do usuário logado.
     * @return ResponseEntity com o DTO do endereço e status 200 (OK).
     */
    @GetMapping("/{id}")
    public ResponseEntity<EnderecoDto> getEnderecoById(
            @PathVariable UUID id,
            Authentication authentication) {

        UUID userId = UUID.fromString(authentication.getName());
        EnderecoDto endereco = enderecoService.getEnderecoById(id, userId)
                .orElseThrow(() -> new RuntimeException("Endereco not found or not associated with user"));
        return ResponseEntity.ok(endereco);
    }

    /**
     * Endpoint para recuperar todos os endereços associados ao usuário autenticado.
     * 
     * @param authentication Objeto de autenticação contendo o ID do usuário logado.
     * @return ResponseEntity com uma lista de DTOs de endereços e status 200 (OK).
     */
    @GetMapping("/todos")
    public ResponseEntity<List<EnderecoDto>> getAllEnderecos(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        List<EnderecoDto> enderecos = enderecoService.getAllEnderecos(userId);
        return ResponseEntity.ok(enderecos);
    }

    /**
     * Endpoint para atualizar um endereço existente associado ao usuário autenticado.
     * 
     * @param id ID do endereço a ser atualizado.
     * @param enderecoDto DTO com os dados atualizados do endereço.
     * @param authentication Objeto de autenticação contendo o ID do usuário logado.
     * @return ResponseEntity com o DTO do endereço atualizado e status 200 (OK).
     */
    @PutMapping("/{id}")
    public ResponseEntity<EnderecoDto> atualizarEndereco(
            @PathVariable UUID id,
            @Valid @RequestBody EnderecoDto enderecoDto,
            Authentication authentication) {

        UUID userId = UUID.fromString(authentication.getName());
        EnderecoDto enderecoAtualizado = enderecoService.updateEndereco(id, enderecoDto, userId);
        return ResponseEntity.ok(enderecoAtualizado);
    }

    /**
     * Endpoint para excluir um endereço existente associado ao usuário autenticado.
     * 
     * @param id ID do endereço a ser excluído.
     * @param authentication Objeto de autenticação contendo o ID do usuário logado.
     * @return ResponseEntity com uma mensagem de sucesso e status 200 (OK).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> excluirEndereco(
            @PathVariable UUID id,
            Authentication authentication) {

        UUID userId = UUID.fromString(authentication.getName());
        enderecoService.deleteEndereco(id, userId);
        return ResponseEntity.ok("Endereco excluído com sucesso");
    }
}
