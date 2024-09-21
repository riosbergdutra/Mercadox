package api.product.produtos.dtos.AvaliacaoDto;

import java.math.BigDecimal;
import java.util.UUID;

public record AvaliacaoRequestDto(
        UUID idUsuario,
        BigDecimal pontuacao,
        Long idProduto
) {
    
}
