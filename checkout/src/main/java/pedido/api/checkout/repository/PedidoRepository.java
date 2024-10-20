package pedido.api.checkout.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import pedido.api.checkout.model.Pedido;

public interface PedidoRepository extends JpaRepository<Pedido, UUID>{
    
}
