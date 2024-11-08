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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import api.product.produtos.dtos.ProdutosDto.ProdutoDtoRequest;
import api.product.produtos.dtos.ProdutosDto.ProdutoDtoResponse;
import api.product.produtos.dtos.adicionaraocarrinho.AdicionarAoCarrinhoRequestDto;
import api.product.produtos.dtos.produtobyidDto.ProdutoByIdResponse;
import api.product.produtos.dtos.quantidade.QuantidadeRequest;
import api.product.produtos.exceptions.UsuarioNotFoundException;
import api.product.produtos.model.Produto;
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
    
    @GetMapping("/maiscomprados")
public ResponseEntity<List<ProdutoDtoResponse>> getTop10ProdutosMaisComprados() {
    List<ProdutoDtoResponse> produtos = produtoService.getTop10ProdutosMaisComprados();
    return ResponseEntity.ok(produtos);
}


    @GetMapping("/{id}")
    public ResponseEntity<ProdutoByIdResponse> getProdutoById(@PathVariable Long id) {
        ProdutoByIdResponse produto = produtoService.getProdutoById(id);
        return new ResponseEntity<>(produto, HttpStatus.OK);
    }

    @GetMapping("/{idProduto}/verificar-estoque/{quantidade}")
    public ResponseEntity<Boolean> verificarEstoqueDisponivel(
            @PathVariable Long idProduto,
            @PathVariable int quantidade) {

        boolean temEstoqueSuficiente = produtoService.verificarEstoqueDisponivel(idProduto, quantidade);
        return ResponseEntity.ok(temEstoqueSuficiente);
    }

    @PostMapping("/{idCarrinho}/adicionar/{idUsuario}")
    public ResponseEntity<Void> adicionarProdutoAoCarrinho(
            @PathVariable("idCarrinho") UUID idCarrinho,
            @PathVariable("idUsuario") UUID idUsuario,
            @RequestBody AdicionarAoCarrinhoRequestDto requestDto,
            Authentication authentication) {

        UUID userId = getUserIdFromAuthentication(authentication);

        // Verifica se o ID do usuário na URL corresponde ao ID no token
        if (!idUsuario.equals(userId)) {
            throw new UsuarioNotFoundException("O ID do usuário na URL não corresponde ao ID no token.");
        }

        // Chama o serviço para adicionar o produto ao carrinho
        produtoService.adicionarProdutoAoCarrinho(requestDto, userId, idCarrinho);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/vendedores/{idVendedor}/produtos/{idProduto}/estoque")
    public ResponseEntity<Produto> atualizarEstoque(
            @PathVariable UUID idVendedor,
            @PathVariable Long idProduto,
            @RequestBody QuantidadeRequest request,
            Authentication authentication) {
                
        UUID userId = getUserIdFromAuthentication(authentication);

        Produto produtoAtualizado = produtoService.adicionarEstoque(userId, idProduto,
                request.quantidade());
        return ResponseEntity.ok(produtoAtualizado);
    }

    @PostMapping("/{idProduto}/registrar-compra")
public ResponseEntity<Produto> registrarCompra(
        @PathVariable Long idProduto,
        @RequestBody int quantidade) {

    Produto produtoAtualizado = produtoService.registrarCompra(idProduto, quantidade );
    return ResponseEntity.ok(produtoAtualizado);
}


    private UUID getUserIdFromAuthentication(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }
}
