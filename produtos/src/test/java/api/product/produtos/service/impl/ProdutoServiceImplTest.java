package api.product.produtos.service.impl;



import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import api.product.produtos.dtos.ProdutosDto.ProdutoDtoRequest;
import api.product.produtos.dtos.ProdutosDto.ProdutoDtoResponse;
import api.product.produtos.enums.CategoriaProduto;
import api.product.produtos.model.Produto;
import api.product.produtos.repository.ProdutoRepository;
import api.product.produtos.service.S3Service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.mock.web.MockMultipartFile;

public class ProdutoServiceImplTest {

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private S3Service s3Service;

    @InjectMocks
    private ProdutoServiceImpl produtoService;

    private Produto produto;
    private UUID vendedorId;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        vendedorId = UUID.randomUUID();
        produto = new Produto();
        produto.setIdProduto(1L);
        produto.setNomeProduto("Produto Teste");
        produto.setDescricao("Descrição do Produto Teste");
        produto.setPreco(new BigDecimal("100.00"));
        produto.setIdVendedor(vendedorId);
    }
    @Test
    public void testAddProduto() throws IOException {
        // Simula uma imagem principal e uma lista de fotos adicionais
        MockMultipartFile imagem = new MockMultipartFile("imagem", "imagem.jpg", "image/jpeg", "imagem content".getBytes());
        MockMultipartFile foto1 = new MockMultipartFile("foto1", "foto1.jpg", "image/jpeg", "foto1 content".getBytes());
        MockMultipartFile foto2 = new MockMultipartFile("foto2", "foto2.jpg", "image/jpeg", "foto2 content".getBytes());
    
        ProdutoDtoRequest produtoDtoRequest = new ProdutoDtoRequest(
                "Novo Produto",
                imagem,
                "Descrição do Produto",
                List.of(foto1, foto2),
                "São Paulo",
                new BigDecimal("120.00"),
                10,
                CategoriaProduto.ELETRONICOS,
                vendedorId
        );
    
        Produto produtoSalvo = new Produto();
        produtoSalvo.setIdProduto(2L);
        produtoSalvo.setNomeProduto(produtoDtoRequest.nomeProduto());
    
        when(produtoRepository.save(any(Produto.class))).thenReturn(produtoSalvo);
        when(s3Service.uploadImagemS3(anyString(), any())).thenReturn("https://s3-url.com/image.jpg");
    
        System.out.println("Iniciando teste de adição de produto...");
    
        ProdutoDtoResponse response = produtoService.addProduto(produtoDtoRequest, vendedorId);
    
        System.out.println("Produto adicionado com sucesso: " + response.nomeProduto());
    
        assertNotNull(response);
        assertEquals("Novo Produto", response.nomeProduto());
        verify(produtoRepository, times(1)).save(any(Produto.class));
    }
    
    @Test
public void testRegistrarCompra_Success() {
    // Simula a situação onde o produto existe no repositório
    produto.setQuantidadeEstoque(70);  // Quantidade inicial de estoque
    when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
    when(produtoRepository.save(any(Produto.class))).thenReturn(produto);
    
    // Informa o estoque inicial
    System.out.println("Estoque inicial do produto: " + produto.getQuantidadeEstoque());

    // Realiza a compra de 10 unidades
    Produto produtoAtualizado = produtoService.registrarCompra(1L, 10);
    
    // Verifica se as quantidades foram atualizadas corretamente
    System.out.println("Estoque após compra: " + produtoAtualizado.getQuantidadeEstoque());
    System.out.println("Quantidade comprada: " + produtoAtualizado.getQuantidadeCompra());

    assertNotNull(produtoAtualizado);
    assertEquals(60, produtoAtualizado.getQuantidadeEstoque());  // Estoque após compra (70 - 10)
    assertEquals(10, produtoAtualizado.getQuantidadeCompra());   // Quantidade comprada após compra
    
    // Verifica se o método save foi chamado uma vez
    verify(produtoRepository, times(1)).save(any(Produto.class));
    System.out.println("Método save foi chamado uma vez.");
}

@Test
public void testGetTop10ProdutosMaisComprados() {
    // Simula produtos com diferentes quantidades de compra
    Produto produto1 = new Produto();
    produto1.setIdProduto(1L);
    produto1.setNomeProduto("Produto 1");
    produto1.setQuantidadeCompra(100);

    Produto produto2 = new Produto();
    produto2.setIdProduto(2L);
    produto2.setNomeProduto("Produto 2");
    produto2.setQuantidadeCompra(50);

    Produto produto3 = new Produto();
    produto3.setIdProduto(3L);
    produto3.setNomeProduto("Produto 3");
    produto3.setQuantidadeCompra(75);

    

    // Cria a lista de produtos
    List<Produto> produtos = List.of(produto1, produto2, produto3);

    // Ordena a lista de produtos pela quantidade de compra de forma decrescente
    produtos = produtos.stream()
            .sorted((p1, p2) -> Integer.compare(p2.getQuantidadeCompra(), p1.getQuantidadeCompra()))
            .collect(Collectors.toList());

    // Log da lista de produtos antes da ordenação
    System.out.println("Produtos antes da ordenação:");
    produtos.forEach(p -> System.out.println(p.getNomeProduto() + " - " + p.getQuantidadeCompra()));

    // Simula o comportamento do repositório com a lista já ordenada
    when(produtoRepository.findAll(PageRequest.of(0, 10, Sort.by(Sort.Order.desc("quantidadeCompra")))))
            .thenReturn(new PageImpl<>(produtos));

    // Chama o método que será testado
    List<ProdutoDtoResponse> resposta = produtoService.getTop10ProdutosMaisComprados();

    // Log da resposta após chamada do serviço
    System.out.println("Resposta após chamada do serviço:");
    resposta.forEach(r -> System.out.println(r.nomeProduto()));

    // Verifica se a resposta contém os produtos e está ordenada corretamente
    assertNotNull(resposta);
    assertEquals(3, resposta.size()); // Espera-se que sejam 3 produtos retornados
    assertEquals("Produto 1", resposta.get(0).nomeProduto()); // O produto mais comprado deve vir primeiro
    assertEquals("Produto 3", resposta.get(1).nomeProduto());
    assertEquals("Produto 2", resposta.get(2).nomeProduto());

    // Verifica se o repositório foi chamado corretamente
    verify(produtoRepository, times(1)).findAll(any(PageRequest.class));
    
    // Log de verificação do repositório
    System.out.println("Repositório chamado uma vez.");
}

}    