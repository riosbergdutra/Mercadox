package user.api.usuario.usuario.dtos.CriarUsuarioDto;

import java.time.LocalDate;
import java.util.Set;

import user.api.usuario.usuario.enums.Role;



public record UsuarioRequestDto(
        String nome,
        byte[] imagem,
        String email,
        String senha,
        Set<Role> roles,
        LocalDate dataConta) {

}
