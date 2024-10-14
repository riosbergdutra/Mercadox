package cart.api.carrinho.model;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Min;
import lombok.Data;
import java.math.BigDecimal;

@Embeddable
@Data
public class ItemCarrinho {
     private Long idProduto;
     @Min(1)
     private int quantidade;

     private BigDecimal precoUnitario;


     public BigDecimal getValorTotalItem() {
          return this.precoUnitario.multiply(BigDecimal.valueOf(this.quantidade)); 
     }
} 
