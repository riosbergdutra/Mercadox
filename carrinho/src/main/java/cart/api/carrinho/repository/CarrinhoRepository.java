package cart.api.carrinho.repository;

import java.util.UUID;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import cart.api.carrinho.model.Carrinho;

public interface CarrinhoRepository extends JpaRepository<Carrinho, UUID> {
    Optional<Carrinho> findByIdUsuario(UUID idUsuario);

}
