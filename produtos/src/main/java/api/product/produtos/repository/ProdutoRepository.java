package api.product.produtos.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import api.product.produtos.model.Produto;

public interface ProdutoRepository extends JpaRepository<Produto, UUID> {
}
