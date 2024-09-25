package cart.api.carrinho.service.impl;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;

import cart.api.carrinho.exceptions.CarrinhoNotFoundException;
import cart.api.carrinho.model.Carrinho;
import cart.api.carrinho.repository.CarrinhoRepository;

public class CarrinhoServiceImpl {
    @Autowired
    CarrinhoRepository carrinhoRepository;

    public Carrinho getCarrinhoByIdUsuario(UUID idUsuario, UUID userId) {
        return carrinhoRepository.findByIdUsuario(idUsuario)
                .filter(carrinho -> carrinho.getIdUsuario().equals(userId))
                .orElseThrow(() -> new CarrinhoNotFoundException("Carrinho não encontrado ou não autorizado."));
    }

}
