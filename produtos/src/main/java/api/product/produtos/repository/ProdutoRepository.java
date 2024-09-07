package api.product.produtos.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import api.product.produtos.model.Produto;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {
}
