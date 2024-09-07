package api.product.produtos.dtos.ProdutosDto;

import java.math.BigDecimal;
import java.util.UUID;

import api.product.produtos.enums.CategoriaProduto;

public record ProdutoDtoResponse(
UUID idProduto,
String nomeProduto,
String UrlImagem,
Long descricao,
BigDecimal PrecoProduto,
CategoriaProduto categoriaProduto,
String cidadeVendedor
)
{

}
