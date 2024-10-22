package api.product.produtos.service.impl;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import api.product.produtos.dtos.ProdutosDto.ProdutoDtoRequest;
import api.product.produtos.dtos.ProdutosDto.ProdutoDtoResponse;
import api.product.produtos.dtos.adicionaraocarrinho.AdicionarAoCarrinhoRequestDto;
import api.product.produtos.dtos.carrinhodto.CarrinhoDtoRequest;
import api.product.produtos.dtos.produtobyidDto.ProdutoByIdResponse;
import api.product.produtos.exceptions.FindAllProductsException;
import api.product.produtos.exceptions.ProdutoNotFoundException;
import api.product.produtos.exceptions.UsuarioNotFoundException;
import api.product.produtos.model.Produto;
import api.product.produtos.repository.ProdutoRepository;
import api.product.produtos.service.ProdutoService;
import api.product.produtos.service.S3Service;

@Service
public class ProdutoServiceImpl implements ProdutoService {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private S3Service s3Service;

    @Autowired
    RestTemplate restTemplate;

    @Override
    public ProdutoDtoResponse updateProduto(Long idProduto, ProdutoDtoRequest produtoDtoRequest, UUID userId) {
        // Verifica se o vendedor tem permissão para atualizar o produto
        Produto produto = verificarVendedor(userId, idProduto);

        produto.setNomeProduto(produtoDtoRequest.nomeProduto());
        produto.setDescricao(produtoDtoRequest.descricao());
        produto.setPreco(produtoDtoRequest.preco());
        produto.setCategoriaProduto(produtoDtoRequest.categoriaProduto());
        produto.setCidadeVendedor(produtoDtoRequest.cidadeVendedor());

        // Atualizar imagem principal, se houver
        if (produtoDtoRequest.imagem() != null && !produtoDtoRequest.imagem().isEmpty()) {
            String imagemKey = userId.toString() + "/" + UUID.randomUUID() + ".jpg";
            try {
                String imagemUrl = s3Service.uploadImagemS3(imagemKey, produtoDtoRequest.imagem());
                produto.setUrlImagem(imagemUrl);
            } catch (IOException e) {
                throw new RuntimeException("Erro ao fazer upload da imagem", e);
            }
        }

        // Atualizar fotos adicionais, se houver
        if (produtoDtoRequest.fotos() != null && !produtoDtoRequest.fotos().isEmpty()) {
            List<String> fotosUrls = produtoDtoRequest.fotos().stream()
                    .filter(foto -> !foto.isEmpty())
                    .map(foto -> {
                        String fotoKey = userId.toString() + "/" + UUID.randomUUID() + ".jpg";
                        try {
                            return s3Service.uploadImagemS3(fotoKey, foto);
                        } catch (IOException e) {
                            throw new RuntimeException("Erro ao fazer upload da foto", e);
                        }
                    })
                    .collect(Collectors.toList());
            produto.setUrlFotos(fotosUrls);
        }

        Produto produtoAtualizado = produtoRepository.save(produto);

        return new ProdutoDtoResponse(
                produtoAtualizado.getIdProduto(),
                produtoAtualizado.getNomeProduto(),
                produtoAtualizado.getUrlImagem(),
                produtoAtualizado.getUrlFotos(),
                produtoAtualizado.getPreco(),
                produtoAtualizado.getCategoriaProduto(),
                produtoAtualizado.getCidadeVendedor());
    }

    @Override
    public void deleteProduto(Long idProduto, UUID userId) {
        // Verifica se o vendedor tem permissão para excluir o produto
        Produto produto = verificarVendedor(userId, idProduto);

        // Excluir a imagem principal e fotos adicionais do S3
        Stream.concat(
                Stream.ofNullable(produto.getUrlImagem()),
                produto.getUrlFotos() != null ? produto.getUrlFotos().stream() : Stream.empty()).forEach(url -> {
                    String key = url.substring(url.lastIndexOf("/") + 1);
                    s3Service.deleteImagemS3(key);
                });

        // Excluir o produto do banco de dados
        produtoRepository.delete(produto);
    }

    @Override
    public List<ProdutoDtoResponse> getAllProdutos() {
        try {
            List<Produto> produtos = produtoRepository.findAll();
            return produtos.stream()
                    .map(produto -> new ProdutoDtoResponse(
                            produto.getIdProduto(),
                            produto.getNomeProduto(),
                            produto.getUrlImagem(),
                            produto.getUrlFotos(),
                            produto.getPreco(),
                            produto.getCategoriaProduto(),
                            produto.getCidadeVendedor()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new FindAllProductsException("falha ao achar os produtos", e);
        }
    }

    @Override
    public ProdutoByIdResponse getProdutoById(Long id) {
        return produtoRepository.findById(id)
                .map(produto -> new ProdutoByIdResponse(
                        produto.getIdProduto(),
                        produto.getNomeProduto(),
                        produto.getUrlImagem(),
                        produto.getDescricao(),
                        produto.getPreco(),
                        produto.getCategoriaProduto(),
                        produto.getCidadeVendedor()))
                .orElseThrow(() -> new ProdutoNotFoundException("Produto não encontrado"));
    }

    @Override
    public ProdutoDtoResponse addProduto(ProdutoDtoRequest produtoDtoRequest, UUID userId) {
        // Comparar o ID do vendedor com o ID do token
        if (!userId.equals(produtoDtoRequest.idVendedor())) {
            throw new UsuarioNotFoundException(
                    "Vendedor com ID: " + userId + " não encontrado ou não autorizado.");
        }

        Produto novoProduto = new Produto();
        novoProduto.setIdVendedor(userId); // Definindo o ID do vendedor
        novoProduto.setNomeProduto(produtoDtoRequest.nomeProduto());
        novoProduto.setDescricao(produtoDtoRequest.descricao());
        novoProduto.setCidadeVendedor(produtoDtoRequest.cidadeVendedor());
        novoProduto.setPreco(produtoDtoRequest.preco());
        novoProduto.setQuantidadeEstoque(produtoDtoRequest.quantidadeEstoque());
        novoProduto.setCategoriaProduto(produtoDtoRequest.categoriaProduto());

        // Manipular a imagem principal
        if (produtoDtoRequest.imagem() != null && !produtoDtoRequest.imagem().isEmpty()) {
            String imagemKey = userId.toString() + "/" + UUID.randomUUID() + ".jpg";
            String imagemUrl;
            try {
                imagemUrl = s3Service.uploadImagemS3(imagemKey, produtoDtoRequest.imagem());
            } catch (IOException e) {
                throw new RuntimeException("Erro ao fazer upload da imagem", e);
            }
            novoProduto.setUrlImagem(imagemUrl);
        }

        // Manipular as fotos adicionais
        if (produtoDtoRequest.fotos() != null && !produtoDtoRequest.fotos().isEmpty()) {
            List<String> fotosUrls = produtoDtoRequest.fotos().stream()
                    .filter(foto -> !foto.isEmpty())
                    .map(foto -> {
                        String fotoKey = userId.toString() + "/" + UUID.randomUUID() + ".jpg";
                        try {
                            return s3Service.uploadImagemS3(fotoKey, foto);
                        } catch (IOException e) {
                            throw new RuntimeException("Erro ao fazer upload da foto", e);
                        }
                    })
                    .collect(Collectors.toList());
            novoProduto.setUrlFotos(fotosUrls);
        }

        Produto produtoSalvo = produtoRepository.save(novoProduto);

        return new ProdutoDtoResponse(
                produtoSalvo.getIdProduto(),
                produtoSalvo.getNomeProduto(),
                produtoSalvo.getUrlImagem(),
                produtoSalvo.getUrlFotos(),
                produtoSalvo.getPreco(),
                produtoSalvo.getCategoriaProduto(),
                produtoSalvo.getCidadeVendedor());
    }

    @Override
    public void adicionarProdutoAoCarrinho(AdicionarAoCarrinhoRequestDto requestDto, UUID userId, UUID idCarrinho) {
        // Verifica se o produto existe
        Produto produto = produtoRepository.findById(requestDto.idProduto())
                .orElseThrow(() -> new ProdutoNotFoundException("Produto não encontrado"));

        // Preparar DTO para o carrinho
        CarrinhoDtoRequest carrinhoDtoRequest = new CarrinhoDtoRequest();
        carrinhoDtoRequest.setIdProduto(produto.getIdProduto());
        carrinhoDtoRequest.setIdVendedor(produto.getIdVendedor());
        carrinhoDtoRequest.setQuantidade(requestDto.quantidade());
        carrinhoDtoRequest.setPrecoUnitario(produto.getPreco());
        // Fazer chamada para a API do carrinho
        String url = String.format("http://localhost:8084/carrinho/%s/adicionar/%s", idCarrinho, userId);

        restTemplate.postForEntity(url, carrinhoDtoRequest, Void.class);
    }

    @Override
    public boolean verificarEstoqueDisponivel(Long idProduto, int quantidadeSolicitada) {
        Produto produto = produtoRepository.findById(idProduto)
                .orElseThrow(() -> new ProdutoNotFoundException("Produto não encontrado"));

        // Verifica se o estoque disponível é maior ou igual à quantidade solicitada
        return produto.getQuantidadeEstoque() >= quantidadeSolicitada;
    }

    private Produto verificarVendedor(UUID userId, Long idProduto) {
        return produtoRepository.findByIdVendedorAndIdProduto(userId, idProduto)
                .orElseThrow(() -> new UsuarioNotFoundException(
                        "Vendedor com ID: " + userId + " não autorizado para o produto com ID: " + idProduto));
    }

}
