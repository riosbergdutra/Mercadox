package api.product.produtos.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import api.product.produtos.dtos.AvaliacaoDto.AvaliacaoRequestDto;
import api.product.produtos.dtos.AvaliacaoDto.AvaliacaoResponseDto;

public interface AvaliacaoService {

    AvaliacaoResponseDto createAvaliacao(AvaliacaoRequestDto avaliacaoRequest, UUID userId);

    AvaliacaoResponseDto getAvaliacaoById(UUID idAvaliacao);

    List<AvaliacaoResponseDto> getAvaliacoesByProduto(Long idProduto);

    AvaliacaoResponseDto updateAvaliacao(UUID idAvaliacao, AvaliacaoRequestDto avaliacaoRequest, UUID userId);

    void deleteAvaliacao(UUID idAvaliacao, UUID userId);

    BigDecimal calcularMediaAvaliacoes(Long idProduto);
}
