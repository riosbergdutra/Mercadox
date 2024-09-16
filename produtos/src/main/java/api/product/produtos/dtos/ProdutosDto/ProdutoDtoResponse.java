package api.product.produtos.dtos.ProdutosDto;

import java.math.BigDecimal;
import java.util.List;

import api.product.produtos.enums.CategoriaProduto;

public record ProdutoDtoResponse(
Long idProduto,
String nomeProduto,
String UrlImagem,
List<String> UrlFotos,
BigDecimal PrecoProduto,
CategoriaProduto categoriaProduto,
String cidadeVendedor
)
{

}
