package pedido.api.checkout.dto.pedido;

import java.util.UUID;

import pedido.api.checkout.enums.EstadoPedido;

public record CriarPedidoResponseDto(
      UUID idPedido,
     EstadoPedido estadoPedido,
     String mensagem

) {
} 