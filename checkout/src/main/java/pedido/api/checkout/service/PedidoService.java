package pedido.api.checkout.service;

import java.util.UUID;

import pedido.api.checkout.dto.pedido.CriarPedidoRequestDto;
import pedido.api.checkout.dto.pedido.CriarPedidoResponseDto;

public interface PedidoService {
     public CriarPedidoResponseDto criarPedido(CriarPedidoRequestDto pedidoRequest, UUID idUsuario);
}