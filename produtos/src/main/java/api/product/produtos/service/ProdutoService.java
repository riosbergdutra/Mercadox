package api.product.produtos.service;

import java.util.List;
import java.util.UUID;

import api.product.produtos.dtos.ProdutosDto.ProdutoDtoRequest;
import api.product.produtos.dtos.ProdutosDto.ProdutoDtoResponse;
import api.product.produtos.dtos.produtobyidDto.ProdutoByIdResponse;

public interface ProdutoService {

     public List<ProdutoDtoResponse> getAllProdutos();
     public ProdutoByIdResponse getProdutoById(Long id);
     public ProdutoDtoResponse addProduto(ProdutoDtoRequest produtoDtoRequest, UUID IdVendedor);
     public ProdutoDtoResponse updateProduto(Long idProduto, ProdutoDtoRequest produtoDtoRequest, UUID userId);
     public void deleteProduto(Long idProduto, UUID userId);
}
