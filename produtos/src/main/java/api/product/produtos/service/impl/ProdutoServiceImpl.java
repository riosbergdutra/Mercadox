package api.product.produtos.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import api.product.produtos.dtos.ProdutosDto.ProdutoDtoResponse;
import api.product.produtos.exceptions.FindAllProductsException;
import api.product.produtos.model.Produto;
import api.product.produtos.repository.ProdutoRepository;
import api.product.produtos.service.ProdutoService;

@Service
public class ProdutoServiceImpl implements ProdutoService {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Override
    public List<ProdutoDtoResponse> FindAllProducts() {
        try {
            List<Produto> produtos = produtoRepository.findAll();
            return produtos.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        } catch (Exception e) {
            throw new FindAllProductsException("falha ao achar os produtos", e);
        }
    }
    private ProdutoDtoResponse convertToDto(Produto produto) {
        return new ProdutoDtoResponse(
            produto.getIdProduto(),
            produto.getNomeProduto(),
            produto.getUrlImagem(),
            produto.getDescricao(),
            produto.getPrecoProduto(),
            produto.getCategoriaProduto(),
            produto.getCidadeVendedor()
        );
    }

}
