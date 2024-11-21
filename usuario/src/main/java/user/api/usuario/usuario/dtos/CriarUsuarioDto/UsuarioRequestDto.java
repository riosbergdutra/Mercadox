package user.api.usuario.usuario.dtos.CriarUsuarioDto;



import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import user.api.usuario.usuario.enums.Role;

public record UsuarioRequestDto(
                @NotBlank(message = "Nome não pode estar vazio") String nome,
                @NotBlank(message = "Email não pode estar vazio") @Email(message = "Email inválido") String email,
                @NotBlank(message = "Senha não pode estar vazia") String senha,
                @NotNull(message = "Role não pode estar vazia") Role role) {
}
