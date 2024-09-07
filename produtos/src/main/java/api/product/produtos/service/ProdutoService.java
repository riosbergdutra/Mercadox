package api.product.produtos.service;

import java.util.List;

import api.product.produtos.dtos.ProdutosDto.ProdutoDtoResponse;

public interface ProdutoService {

     List<ProdutoDtoResponse> FindAllProducts();

}
