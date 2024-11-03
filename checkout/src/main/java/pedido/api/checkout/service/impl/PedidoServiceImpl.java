package pedido.api.checkout.service.impl;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import pedido.api.checkout.dto.pedido.CriarPedidoRequestDto;
import pedido.api.checkout.dto.pedido.CriarPedidoResponseDto;
import pedido.api.checkout.enums.EstadoPedido;
import pedido.api.checkout.exceptions.PedidoException;
import pedido.api.checkout.model.Pedido;
import pedido.api.checkout.repository.PedidoRepository;
import pedido.api.checkout.service.PedidoService;

@Service
public class PedidoServiceImpl implements PedidoService {
    @Autowired
    private PedidoRepository pedidoRepository;

    @Override
    public CriarPedidoResponseDto criarPedido(CriarPedidoRequestDto pedidoRequest, UUID userId) {
        validarUsuario(userId); // Assume que esta função é bem implementada

        Pedido novoPedido = new Pedido();
        novoPedido.setEnderecoEntrega(pedidoRequest.enderecoEntrega());
        novoPedido.setItens(pedidoRequest.itens());
        novoPedido.setValorCompra(pedidoRequest.valorCompra());
        novoPedido.setFormaPagamento(pedidoRequest.formaPagamento());
        novoPedido.setEstadoDoPedido(EstadoPedido.PENDENTE);

        pedidoRepository.save(novoPedido);

        return new CriarPedidoResponseDto(novoPedido.getIdPedido(), novoPedido.getEstadoDoPedido(), "Pedido criado com sucesso");
    }

    private Pedido validarUsuario(UUID userId) {
        return pedidoRepository.findByIdUsuario(userId)
        .filter(pedido -> pedido.getIdUsuario().equals(userId))
        .orElseThrow(() -> new PedidoException("não valido") );
    }

    private Pedido validarPedidoUsuario(UUID idPedido, UUID userId) {
        return pedidoRepository.findByIdPedidoAndIdUsuario(idPedido, userId)
            .filter(pedido -> pedido.getIdUsuario().equals(userId))
            .orElseThrow(() -> new PedidoException("não valido") );
    }
}
