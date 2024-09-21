package api.product.produtos.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import api.product.produtos.dtos.AvaliacaoDto.AvaliacaoRequestDto;
import api.product.produtos.dtos.AvaliacaoDto.AvaliacaoResponseDto;
import api.product.produtos.exceptions.ProdutoNotFoundException;
import api.product.produtos.model.Avaliacao;
import api.product.produtos.model.Produto;
import api.product.produtos.repository.AvaliacaoRepository;
import api.product.produtos.repository.ProdutoRepository;
import api.product.produtos.service.AvaliacaoService;

@Service
public class AvaliacaoServiceImpl implements AvaliacaoService {

    @Autowired
    private AvaliacaoRepository avaliacaoRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Override
    public AvaliacaoResponseDto createAvaliacao(AvaliacaoRequestDto avaliacaoRequest, UUID userId) {
        Produto produto = produtoRepository.findById(avaliacaoRequest.idProduto())
                .orElseThrow(() -> new ProdutoNotFoundException("Produto não encontrado"));
    
        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setIdUsuario(userId);  // Associa o usuário autenticado à avaliação
        avaliacao.setPontuacao(avaliacaoRequest.pontuacao());
        avaliacao.setProduto(produto);
    
        Avaliacao savedAvaliacao = avaliacaoRepository.save(avaliacao);
        atualizarPontuacaoMediaProduto(produto);
    
        return new AvaliacaoResponseDto(
                savedAvaliacao.getIdAvaliacao(),
                savedAvaliacao.getIdUsuario(),
                savedAvaliacao.getPontuacao(),
                savedAvaliacao.getProduto().getIdProduto());
    }
    
    @Override
    public AvaliacaoResponseDto getAvaliacaoById(UUID idAvaliacao) {
        Avaliacao avaliacao = avaliacaoRepository.findById(idAvaliacao)
                .orElseThrow(() -> new RuntimeException("Avaliação não encontrada"));
        return new AvaliacaoResponseDto(
                avaliacao.getIdAvaliacao(),
                avaliacao.getIdUsuario(),
                avaliacao.getPontuacao(),
                avaliacao.getProduto().getIdProduto());
    }

    @Override
    public List<AvaliacaoResponseDto> getAvaliacoesByProduto(Long idProduto) {
        Produto produto = produtoRepository.findById(idProduto)
                .orElseThrow(() -> new ProdutoNotFoundException("Produto não encontrado"));
        return produto.getAvaliacoes().stream()
                .map(avaliacao -> new AvaliacaoResponseDto(
                        avaliacao.getIdAvaliacao(),
                        avaliacao.getIdUsuario(),
                        avaliacao.getPontuacao(),
                        produto.getIdProduto()))
                .collect(Collectors.toList());
    }

    @Override
public AvaliacaoResponseDto updateAvaliacao(UUID idAvaliacao, AvaliacaoRequestDto avaliacaoRequest, UUID userId) {
    Avaliacao avaliacao = verificarUsuario(userId, idAvaliacao);

    avaliacao.setPontuacao(avaliacaoRequest.pontuacao());
    Avaliacao updatedAvaliacao = avaliacaoRepository.save(avaliacao);

    atualizarPontuacaoMediaProduto(updatedAvaliacao.getProduto());

    return new AvaliacaoResponseDto(
            updatedAvaliacao.getIdAvaliacao(),
            updatedAvaliacao.getIdUsuario(),
            updatedAvaliacao.getPontuacao(),
            updatedAvaliacao.getProduto().getIdProduto());
}


@Override
public void deleteAvaliacao(UUID idAvaliacao, UUID userId) {
    Avaliacao avaliacao = verificarUsuario(userId, idAvaliacao);
    Produto produto = avaliacao.getProduto();

    avaliacaoRepository.delete(avaliacao);
    atualizarPontuacaoMediaProduto(produto);
}


    @Override
    public BigDecimal calcularMediaAvaliacoes(Long idProduto) {
        Produto produto = produtoRepository.findById(idProduto)
                .orElseThrow(() -> new ProdutoNotFoundException("Produto não encontrado"));

        if (produto.getAvaliacoes().isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal somaPontuacao = produto.getAvaliacoes().stream()
                .map(Avaliacao::getPontuacao)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal media = somaPontuacao.divide(BigDecimal.valueOf(produto.getAvaliacoes().size()), 2, RoundingMode.HALF_UP);

        return media.min(BigDecimal.valueOf(5));
    }

    private void atualizarPontuacaoMediaProduto(Produto produto) {
        BigDecimal media = calcularMediaAvaliacoes(produto.getIdProduto());
        produto.setPontuacaoProduto(media);
        produto.setQuantidadeAvaliadores(produto.getAvaliacoes().size());
        produtoRepository.save(produto);
    }

    private Avaliacao verificarUsuario(UUID userId, UUID idAvaliacao) {
        Avaliacao avaliacao = avaliacaoRepository.findById(idAvaliacao)
                .orElseThrow(() -> new RuntimeException("Avaliação não encontrada"));
    
        if (!avaliacao.getIdUsuario().equals(userId)) {
            throw new RuntimeException("Usuário não autorizado para modificar essa avaliação");
        }
    
        return avaliacao;
    }
    
    
}
