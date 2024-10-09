package cart.api.carrinho.service;

import java.util.UUID;

import cart.api.carrinho.model.Carrinho;

public interface CarrinhoService {
    public Carrinho getCarrinhoByIdUsuario(UUID idUsuario, UUID userId);
    public void processarMensagemSQS(String mensagemSQS);
}
