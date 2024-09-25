package cart.api.carrinho.service;

import java.util.UUID;
import java.util.Optional;

import cart.api.carrinho.model.Carrinho;

public interface CarrinhoService {
    public Optional<Carrinho> getCarrinhoByIdUsuario(UUID idUsuario, UUID userId);
}
