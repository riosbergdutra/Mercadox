package cart.api.carrinho.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import cart.api.carrinho.model.Carrinho;
import cart.api.carrinho.service.CarrinhoService;

import org.springframework.security.core.Authentication;

public class CarrinhoController {
     @Autowired
    private CarrinhoService carrinhoService;

    /**
     * Endpoint para obter o carrinho de um usuário.
     *
     * @param idUsuario      ID do usuário a ser consultado.
     * @param authentication Objeto de autenticação contendo o ID do usuário logado.
     * @return ResponseEntity com o carrinho do usuário ou uma mensagem de erro.
     */
    @GetMapping("/{idUsuario}")
    public ResponseEntity<?> getCarrinhoByIdUsuario(
            @PathVariable UUID idUsuario,
            Authentication authentication) {

        UUID userId = UUID.fromString(authentication.getName());

        Carrinho carrinho = carrinhoService.getCarrinhoByIdUsuario(idUsuario, userId);

        return ResponseEntity.ok(carrinho);

    }
}
