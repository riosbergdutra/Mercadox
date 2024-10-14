package cart.api.carrinho.dto;

import java.math.BigDecimal;

public record CarrinhoDtoRequest(
        Long idProduto,
        int quantidade,
        BigDecimal precoUnitario
) {
}