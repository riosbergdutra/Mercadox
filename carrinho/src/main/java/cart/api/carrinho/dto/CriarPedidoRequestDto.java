package cart.api.carrinho.dto;


import cart.api.carrinho.enums.FormaPagamento;
import cart.api.carrinho.model.ItemCarrinho;

import java.math.BigDecimal;
import java.util.List;

public record CriarPedidoRequestDto(
    Endereco enderecoEntrega,
    List<ItemCarrinho> itens, 
    BigDecimal valorCompra,
    FormaPagamento formaPagamento
) {
    
}
