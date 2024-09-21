package api.product.produtos.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import api.product.produtos.dtos.AvaliacaoDto.AvaliacaoRequestDto;
import api.product.produtos.dtos.AvaliacaoDto.AvaliacaoResponseDto;
import api.product.produtos.service.AvaliacaoService;

@RestController
@RequestMapping("/avaliacoes")
public class AvaliacaoController {

    @Autowired
    private AvaliacaoService avaliacaoService;

    @PostMapping("/{idUsuario}/criar")
    public ResponseEntity<AvaliacaoResponseDto> criarAvaliacao(@PathVariable("idUsuario") UUID idUsuario,
            @RequestBody AvaliacaoRequestDto avaliacaoRequest,
            Authentication authentication) {
        UUID userId = getUserIdFromAuthentication(authentication);

        AvaliacaoResponseDto avaliacao = avaliacaoService.createAvaliacao(avaliacaoRequest, userId);
        return ResponseEntity.ok(avaliacao);
    }

    @GetMapping("/produto/{idProduto}")
    public ResponseEntity<List<AvaliacaoResponseDto>> listarAvaliacoesPorProduto(@PathVariable Long idProduto) {
        List<AvaliacaoResponseDto> avaliacoes = avaliacaoService.getAvaliacoesByProduto(idProduto);
        return ResponseEntity.ok(avaliacoes);
    }

    @GetMapping("/{idAvaliacao}")
    public ResponseEntity<AvaliacaoResponseDto> buscarAvaliacaoPorId(@PathVariable UUID idAvaliacao) {
        AvaliacaoResponseDto avaliacao = avaliacaoService.getAvaliacaoById(idAvaliacao);
        return ResponseEntity.ok(avaliacao);
    }

    @PutMapping("/{idUsuario}/{idAvaliacao}/atualizar")
    public ResponseEntity<AvaliacaoResponseDto> atualizarAvaliacao(@PathVariable("idUsuario") UUID idUsuario,
            @PathVariable UUID idAvaliacao,
            @RequestBody AvaliacaoRequestDto avaliacaoRequest,
            Authentication authentication) {
        UUID userId = getUserIdFromAuthentication(authentication);

        AvaliacaoResponseDto avaliacao = avaliacaoService.updateAvaliacao(idAvaliacao, avaliacaoRequest, userId);
        return ResponseEntity.ok(avaliacao);
    }

    @DeleteMapping("/{idUsuario}/{idAvaliacao}/deletar")
    public ResponseEntity<Void> deletarAvaliacao(@PathVariable("idUsuario") UUID idUsuario,
            @PathVariable UUID idAvaliacao,
            Authentication authentication) {
        UUID userId = getUserIdFromAuthentication(authentication);

        avaliacaoService.deleteAvaliacao(idAvaliacao, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/media/{idProduto}")
    public ResponseEntity<BigDecimal> calcularMediaDeAvaliacoes(@PathVariable Long idProduto) {
        BigDecimal media = avaliacaoService.calcularMediaAvaliacoes(idProduto);
        return ResponseEntity.ok(media);
    }

    private UUID getUserIdFromAuthentication(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }
}
