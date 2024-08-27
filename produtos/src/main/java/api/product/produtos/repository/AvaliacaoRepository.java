package api.product.produtos.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import api.product.produtos.model.Avaliacao;

public interface AvaliacaoRepository extends JpaRepository<Avaliacao, UUID> {

}
