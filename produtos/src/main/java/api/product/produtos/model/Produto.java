package api.product.produtos.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import api.product.produtos.enums.CategoriaProduto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "produtos")
@Data
public class Produto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false, name = "id_produto")
    private Long idProduto;

    @Column(unique = false, nullable = false, name = "id_vendedor")
    private UUID idVendedor;

    @Column(unique = false, nullable = false, name = "nome_produto")
    private String nomeProduto;

    @Column(unique = true, nullable = false, name = "url_imagem")
    private String UrlImagem;

    @Column(unique = true, nullable = false, name = "descricao")
    private String descricao;

    @Column(unique = true, nullable = false, name = "url_fotos")
    private List<String> urlFotos;

    @Column(unique = false, name = "data_criacao")
    private LocalDate dataCriacao = LocalDate.now();

    @Column(unique = false, nullable = false, name = "cidade_vendedor")
    private String cidadeVendedor;

    @Column(unique = false, nullable = false, name = "preco")
    private BigDecimal preco;

    @Column(nullable = false, name = "quantidade_estoque")
    private int quantidadeEstoque;

    @Column(unique = false, nullable = false, name = "categoria_produto")
    private CategoriaProduto categoriaProduto;

    @Column(nullable = false, name = "pontuacao_produto")
    private BigDecimal pontuacaoProduto = BigDecimal.ZERO;

    @Column(nullable = false, name = "quantidade_avaliadores")
    private int quantidadeAvaliadores = 0;

    @OneToMany(mappedBy = "produto")
    private List<Avaliacao> avaliacoes;
}
