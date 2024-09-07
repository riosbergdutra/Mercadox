package api.product.produtos.service;

import java.util.List;

import api.product.produtos.dtos.ProdutosDto.ProdutoDtoResponse;
import api.product.produtos.dtos.produtobyidDto.ProdutoByIdResponse;

public interface ProdutoService {

     public List<ProdutoDtoResponse> getAllProdutos();
     public ProdutoByIdResponse getProdutoById(Long id);
}
