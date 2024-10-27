package cart.api.carrinho.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

public record Endereco(
    @NotEmpty(message = "rua é obrigatório")
    String rua,

    @NotEmpty(message = "Número é obrigatório")
    String numero,

    @Column(nullable = false)
    String complemento,

    @NotEmpty(message = "Bairro é obrigatório")
    String bairro,

    @NotEmpty(message = "Cidade é obrigatória")
    String cidade,

    @NotEmpty(message = "Estado é obrigatório")
    String estado,

    @NotEmpty(message = "CEP é obrigatório")
    @Pattern(regexp = "\\d{5}-\\d{3}", message = "CEP inválido")
     String cep
) {

}
