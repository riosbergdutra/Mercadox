package cart.api.carrinho.dto;


public record CarrinhoDtoRequest(
        Long idProduto,
        int quantidade
) {
}