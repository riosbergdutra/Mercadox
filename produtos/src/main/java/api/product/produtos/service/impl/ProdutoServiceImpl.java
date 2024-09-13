package api.product.produtos.service.impl;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import api.product.produtos.dtos.ProdutosDto.ProdutoDtoRequest;
import api.product.produtos.dtos.ProdutosDto.ProdutoDtoResponse;
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

    @Override
    public List<ProdutoDtoResponse> getAllProdutos() {
        try {
            List<Produto> produtos = produtoRepository.findAll();
            return produtos.stream()
                    .map(produto -> new ProdutoDtoResponse(
                            produto.getIdProduto(),
                            produto.getNomeProduto(),
                            produto.getUrlImagem(),
                            produto.getPrecoProduto(),
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
                        produto.getPrecoProduto(),
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
        novoProduto.setIdVendedor(userId);  // Definindo o ID do vendedor
        novoProduto.setNomeProduto(produtoDtoRequest.nomeProduto());
        novoProduto.setDescricao(produtoDtoRequest.descricao());
        novoProduto.setCidadeVendedor(produtoDtoRequest.cidadeVendedor());
        novoProduto.setPrecoProduto(produtoDtoRequest.precoProduto());
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
                produtoSalvo.getPrecoProduto(),
                produtoSalvo.getCategoriaProduto(),
                produtoSalvo.getCidadeVendedor());
    }
    
    private void verificarVendedor(UUID userId) {
        produtoRepository.findByIdVendedor(userId)
                .filter(vendedor -> userId.equals(vendedor.getIdVendedor()))
                .orElseThrow(() -> new UsuarioNotFoundException(
                        "Vendedor com ID: " + userId + " não encontrado ou não autorizado."));
    }
}
