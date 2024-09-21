package api.product.produtos.dtos.AvaliacaoDto;

import java.math.BigDecimal;
import java.util.UUID;

public record AvaliacaoResponseDto(
        UUID idAvaliacao,
        UUID idUsuario,
        BigDecimal pontuacao,
        Long idProduto) {

}
