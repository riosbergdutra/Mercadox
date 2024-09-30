package api.product.produtos.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import api.product.produtos.dtos.ProdutosDto.ProdutoDtoRequest;
import api.product.produtos.dtos.ProdutosDto.ProdutoDtoResponse;
import api.product.produtos.dtos.produtobyidDto.ProdutoByIdResponse;
import api.product.produtos.exceptions.UsuarioNotFoundException;
import api.product.produtos.service.ProdutoService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/produtos")
public class ProdutoController {

    @Autowired
    private ProdutoService produtoService;

    // Atualizar produto
    @PutMapping("/{idVendedor}/atualizarproduto/{idProduto}")
    public ResponseEntity<ProdutoDtoResponse> updateProduto(
            @PathVariable("idVendedor") UUID idVendedor,
            @PathVariable("idProduto") Long idProduto,
            @ModelAttribute @Valid ProdutoDtoRequest produtoDtoRequest,
            Authentication authentication) {
        UUID userId = getUserIdFromAuthentication(authentication);

        // Chama o serviço para atualizar o produto
        ProdutoDtoResponse produtoAtualizado = produtoService.updateProduto(idProduto, produtoDtoRequest, userId);
        return ResponseEntity.status(HttpStatus.OK).body(produtoAtualizado);
    }

    // Remover produto
    @DeleteMapping("/{idVendedor}/deletarproduto/{idProduto}")
    public ResponseEntity<Void> deleteProduto(
            @PathVariable("idVendedor") UUID idVendedor,
            @PathVariable("idProduto") Long idProduto,
            Authentication authentication) {
        UUID userId = getUserIdFromAuthentication(authentication);

        // Chama o serviço para deletar o produto
        produtoService.deleteProduto(idProduto, userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/{idVendedor}/criarproduto")
    public ResponseEntity<ProdutoDtoResponse> addProduto(
            @PathVariable("idVendedor") UUID idVendedor,
            @ModelAttribute @Valid ProdutoDtoRequest produtoDtoRequest,
            Authentication authentication) {
        UUID userId = getUserIdFromAuthentication(authentication);
        ProdutoDtoResponse resposta = produtoService.addProduto(produtoDtoRequest, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    @GetMapping("/findall")
    public ResponseEntity<List<ProdutoDtoResponse>> getAllProdutos() {
        List<ProdutoDtoResponse> produtos = produtoService.getAllProdutos();
        return new ResponseEntity<>(produtos, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProdutoByIdResponse> getProdutoById(@PathVariable Long id) {
        ProdutoByIdResponse produto = produtoService.getProdutoById(id);
        return new ResponseEntity<>(produto, HttpStatus.OK);
    }

    @PostMapping("/{userId}/adicionarAoCarrinho/{idProduto}")
    public ResponseEntity<Void> adicionarProdutoAoCarrinho(
            @PathVariable("userId") UUID idUsuario,
            @PathVariable("idProduto") Long idProduto,
            @RequestParam int quantidade,
            Authentication authentication) {
        UUID userId = getUserIdFromAuthentication(authentication);

        if (!idUsuario.equals(userId)) {
            throw new UsuarioNotFoundException("O ID do usuário na URL não corresponde ao ID no token.");
        }

        produtoService.adicionarProdutoAoCarrinho(idProduto, userId, quantidade);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    private UUID getUserIdFromAuthentication(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }
}
