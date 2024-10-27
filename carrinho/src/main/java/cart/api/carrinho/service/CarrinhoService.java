package cart.api.carrinho.service;

import java.util.UUID;

import cart.api.carrinho.dto.CarrinhoDtoRequest;
import cart.api.carrinho.dto.CriarPedidoRequestDto;
import cart.api.carrinho.model.Carrinho;

public interface CarrinhoService {
    public Carrinho getCarrinhoByIdUsuario(UUID idUsuario);
    public void processarMensagemSQS(String mensagemSQS);
    public void adicionarProdutoAoCarrinho(CarrinhoDtoRequest carrinhoDtoRequest,UUID idCarrinho, UUID idUsuario);
    public void limparCarrinho(UUID idCarrinho, UUID idUsuario);
    public void removerItemDoCarrinho(UUID idCarrinho, UUID idUsuario, Long idProduto);
    public void finalizarPedido(UUID idUsuario, UUID idCarrinho, CriarPedidoRequestDto pedidoRequestDto);
}
