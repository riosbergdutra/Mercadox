package cart.api.carrinho.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cart.api.carrinho.dto.CarrinhoDtoRequest;
import cart.api.carrinho.model.Carrinho;
import cart.api.carrinho.service.CarrinhoService;

import org.springframework.security.core.Authentication;
@RestController
@RequestMapping("/carrinho")
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

        Carrinho carrinho = carrinhoService.getCarrinhoByIdUsuario(userId);

        return ResponseEntity.ok(carrinho);

    }

    @PostMapping("/{idCarrinho}/adicionar/{idUsuario}")
    public ResponseEntity<?> adicionarProdutoAoCarrinho(
            @PathVariable UUID idUsuario, 
            @PathVariable UUID idCarrinho, 
            @RequestBody CarrinhoDtoRequest carrinhoDtoRequest, 
            Authentication authentication) {
    
        // O userId é obtido da autenticação, ou seja, o usuário logado
        UUID userId = UUID.fromString(authentication.getName());
    
        // Passa o idUsuario e o userId (ambos podem ser usados para validação)
        carrinhoService.adicionarProdutoAoCarrinho(carrinhoDtoRequest, idCarrinho, userId);
    
        return ResponseEntity.ok("Produto adicionado ao carrinho com sucesso!");
    }
    

 @PostMapping("/{idCarrinho}/limpar/{idUsuario}")
public ResponseEntity<?> limparCarrinho(
        @PathVariable UUID idCarrinho, 
        @PathVariable UUID idUsuario, 
        Authentication authentication) {

    // O userId é obtido da autenticação, ou seja, o usuário logado
    UUID userId = UUID.fromString(authentication.getName());

    // Passa os ids para o serviço limpar o carrinho
    carrinhoService.limparCarrinho(idCarrinho, userId);

    return ResponseEntity.ok("Carrinho esvaziado com sucesso!");
}

@PostMapping("/{idCarrinho}/remover/{idUsuario}/produto/{idProduto}")
public ResponseEntity<?> removerItemDoCarrinho(
        @PathVariable UUID idCarrinho,
        @PathVariable UUID idUsuario,
        @PathVariable Long idProduto,
        Authentication authentication) {

    // O userId é obtido da autenticação, ou seja, o usuário logado
    UUID userId = UUID.fromString(authentication.getName());

    // Passa os IDs para o serviço remover o item do carrinho
    carrinhoService.removerItemDoCarrinho(idCarrinho, userId, idProduto);

    return ResponseEntity.ok("Produto removido do carrinho com sucesso!");
}

}
