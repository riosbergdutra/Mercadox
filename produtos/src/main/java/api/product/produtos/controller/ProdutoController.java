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

/**
 * Controlador para gerenciar operações relacionadas aos produtos.
 * 
 * Fornece endpoints para criar, atualizar, excluir e consultar produtos.
 */
@RestController
@RequestMapping("/produtos")
public class ProdutoController {

    @Autowired
    private ProdutoService produtoService;

    /**
     * Endpoint para atualizar as informações de um produto existente.
     * 
     * @param idVendedor      ID do vendedor que está atualizando o produto.
     * @param idProduto       ID do produto a ser atualizado.
     * @param produtoDtoRequest DTO contendo os dados atualizados do produto.
     * @param authentication  Objeto de autenticação para obter o ID do usuário logado.
     * @return ResponseEntity com o DTO do produto atualizado e status 200 (OK).
     */
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

    /**
     * Endpoint para excluir um produto.
     * 
     * @param idVendedor     ID do vendedor que está deletando o produto.
     * @param idProduto      ID do produto a ser excluído.
     * @param authentication Objeto de autenticação para verificar o ID do usuário logado.
     * @return ResponseEntity com status 204 (NO CONTENT) após a exclusão do produto.
     */
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

    /**
     * Endpoint para criar um novo produto.
     * 
     * @param idVendedor      ID do vendedor que está criando o produto.
     * @param produtoDtoRequest DTO contendo os dados do produto a ser criado.
     * @param authentication  Objeto de autenticação para obter o ID do usuário logado.
     * @return ResponseEntity com o DTO do produto criado e status 201 (CREATED).
     */
    @PostMapping("/{idVendedor}/criarproduto")
    public ResponseEntity<ProdutoDtoResponse> addProduto(
            @PathVariable("idVendedor") UUID idVendedor,
            @ModelAttribute @Valid ProdutoDtoRequest produtoDtoRequest,
            Authentication authentication) {
        UUID userId = getUserIdFromAuthentication(authentication);
        ProdutoDtoResponse resposta = produtoService.addProduto(produtoDtoRequest, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    /**
     * Endpoint para obter todos os produtos cadastrados.
     * 
     * @return ResponseEntity com a lista de produtos e status 200 (OK).
     */
    @GetMapping("/findall")
    public ResponseEntity<List<ProdutoDtoResponse>> getAllProdutos() {
        List<ProdutoDtoResponse> produtos = produtoService.getAllProdutos();
        return new ResponseEntity<>(produtos, HttpStatus.OK);
    }

    /**
     * Endpoint para obter os produtos mais comprados.
     * 
     * @return ResponseEntity com a lista dos produtos mais comprados e status 200 (OK).
     */
    @GetMapping("/maiscomprados")
    public ResponseEntity<List<ProdutoDtoResponse>> getTop10ProdutosMaisComprados() {
        List<ProdutoDtoResponse> produtos = produtoService.getTop10ProdutosMaisComprados();
        return ResponseEntity.ok(produtos);
    }

    /**
     * Endpoint para obter um produto específico pelo seu ID.
     * 
     * @param id ID do produto a ser consultado.
     * @return ResponseEntity com o DTO do produto e status 200 (OK).
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProdutoByIdResponse> getProdutoById(@PathVariable Long id) {
        ProdutoByIdResponse produto = produtoService.getProdutoById(id);
        return new ResponseEntity<>(produto, HttpStatus.OK);
    }

    /**
     * Endpoint para verificar se há estoque suficiente para um produto específico.
     * 
     * @param idProduto ID do produto a ser consultado.
     * @param quantidade Quantidade de produtos a ser verificada.
     * @return ResponseEntity com o status de estoque (true ou false) e status 200 (OK).
     */
    @GetMapping("/{idProduto}/verificar-estoque/{quantidade}")
    public ResponseEntity<Boolean> verificarEstoqueDisponivel(
            @PathVariable Long idProduto,
            @PathVariable int quantidade) {

        boolean temEstoqueSuficiente = produtoService.verificarEstoqueDisponivel(idProduto, quantidade);
        return ResponseEntity.ok(temEstoqueSuficiente);
    }

    /**
     * Endpoint para adicionar um produto ao carrinho de um usuário.
     * 
     * @param idCarrinho ID do carrinho do usuário.
     * @param idUsuario  ID do usuário que está adicionando o produto.
     * @param requestDto Dados do produto a ser adicionado ao carrinho.
     * @param authentication Objeto de autenticação contendo o ID do usuário logado.
     * @return ResponseEntity com status 204 (NO CONTENT) após adicionar o produto ao carrinho.
     */
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

    /**
     * Endpoint para atualizar o estoque de um produto.
     * 
     * @param idVendedor   ID do vendedor que está atualizando o estoque.
     * @param idProduto    ID do produto cujo estoque será atualizado.
     * @param request      DTO com a quantidade a ser adicionada ao estoque.
     * @param authentication Objeto de autenticação para verificar o ID do usuário logado.
     * @return ResponseEntity com o produto atualizado e status 200 (OK).
     */
    @PostMapping("/vendedores/{idVendedor}/produtos/{idProduto}/estoque")
    public ResponseEntity<Produto> atualizarEstoque(
            @PathVariable UUID idVendedor,
            @PathVariable Long idProduto,
            @RequestBody QuantidadeRequest request,
            Authentication authentication) {
                
        UUID userId = getUserIdFromAuthentication(authentication);

        Produto produtoAtualizado = produtoService.adicionarEstoque(userId, idProduto, request.quantidade());
        return ResponseEntity.ok(produtoAtualizado);
    }

    /**
     * Endpoint para registrar a compra de um produto.
     * 
     * @param idProduto ID do produto que foi comprado.
     * @param quantidade Quantidade do produto comprada.
     * @return ResponseEntity com o produto atualizado após a compra e status 200 (OK).
     */
    @PostMapping("/{idProduto}/registrar-compra")
    public ResponseEntity<Produto> registrarCompra(
            @PathVariable Long idProduto,
            @RequestBody int quantidade) {

        Produto produtoAtualizado = produtoService.registrarCompra(idProduto, quantidade);
        return ResponseEntity.ok(produtoAtualizado);
    }

    /**
     * Método auxiliar para obter o ID do usuário a partir do objeto de autenticação.
     * 
     * @param authentication Objeto de autenticação contendo os dados do usuário logado.
     * @return O ID do usuário logado como UUID.
     */
    private UUID getUserIdFromAuthentication(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }
}
