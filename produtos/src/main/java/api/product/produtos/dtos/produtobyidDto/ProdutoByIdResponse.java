package api.product.produtos.dtos.produtobyidDto;

import java.math.BigDecimal;

import api.product.produtos.enums.CategoriaProduto;

public record ProdutoByIdResponse(
        Long idProduto,
        String nomeProduto,
        String UrlImagem,
        Long descricao,
        BigDecimal PrecoProduto,
        CategoriaProduto categoriaProduto,
        String cidadeVendedor) {

}
