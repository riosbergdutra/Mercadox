package api.product.produtos.dtos.carrinhodto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CarrinhoDtoRequest {
    @NotNull(message = "ID do produto não pode ser nulo")
    private Long idProduto;

    @NotNull(message = "ID do vendedor não pode ser nulo")
    private UUID idVendedor;

    @NotNull(message = "Quantidade não pode ser nula")
    private Integer quantidade;
}