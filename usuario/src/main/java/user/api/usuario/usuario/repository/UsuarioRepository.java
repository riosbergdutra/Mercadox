package user.api.usuario.usuario.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import user.api.usuario.usuario.model.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {
    Usuario findByEmail(String email);
}
