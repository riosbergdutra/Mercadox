package user.api.usuario.usuario.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import user.api.usuario.usuario.model.Endereco;

public interface EnderecoRepository extends JpaRepository<Endereco, UUID> {
    List<Endereco> findByUsuarioId(UUID idUsuario);

}
