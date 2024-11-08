package api.product.produtos.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import api.product.produtos.model.Produto;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    Optional<Produto> findByIdVendedor(UUID idVendedor);

    Optional<Produto> findByIdVendedorAndIdProduto(UUID userId, Long idProduto);

    Page<Produto> findAll(Pageable pageable);


}
