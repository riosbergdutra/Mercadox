package pedido.api.checkout.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Embeddable
@Data
public class Endereco {

    @NotEmpty(message = "rua é obrigatório")
    private String rua;

    @NotEmpty(message = "Número é obrigatório")
    private String numero;
    
    @Column(nullable = false)
    private String complemento;

    @NotEmpty(message = "Bairro é obrigatório")
    private String bairro;

    @NotEmpty(message = "Cidade é obrigatória")
    private String cidade;

    @NotEmpty(message = "Estado é obrigatório")
    private String estado;

    @NotEmpty(message = "CEP é obrigatório")
    @Pattern(regexp = "\\d{5}-\\d{3}", message = "CEP inválido")
    private String cep;
}

