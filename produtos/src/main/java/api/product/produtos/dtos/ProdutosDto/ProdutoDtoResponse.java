package api.product.produtos.dtos.ProdutosDto;

import java.math.BigDecimal;

import api.product.produtos.enums.CategoriaProduto;

public record ProdutoDtoResponse(
Long idProduto,
String nomeProduto,
String UrlImagem,
BigDecimal PrecoProduto,
CategoriaProduto categoriaProduto,
String cidadeVendedor
)
{

}
