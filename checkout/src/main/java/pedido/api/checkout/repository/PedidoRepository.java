package pedido.api.checkout.repository;

import java.util.UUID;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import pedido.api.checkout.model.Pedido;

public interface PedidoRepository extends JpaRepository<Pedido, UUID>{
  Optional<Pedido>findByIdPedidoAndIdUsuario(UUID idPedido, UUID idUsuario);
  Optional<Pedido>findByIdUsuario(UUID idUsuario);
}
