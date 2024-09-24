package cart.api.carrinho.model;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Embeddable
@Data
public class ItemCarrinho {
     private Long idProduto;
     
     @Min(1)
     private Integer quantidade;
}
