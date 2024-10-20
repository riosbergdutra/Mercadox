package pedido.api.checkout.model;

import java.math.BigDecimal;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Embeddable
@Data
public class Produtos {
     private Long idProduto;
     @Min(1)
     private int quantidade;

     private BigDecimal precoUnitario;

}
