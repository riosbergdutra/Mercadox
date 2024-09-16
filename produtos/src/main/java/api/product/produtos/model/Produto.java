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
    Long idProduto;

    @Column(unique = true, nullable = false, name = "id_vendedor")
    UUID idVendedor;

    @Column(unique = false, nullable = false, name = "nome_produto")
    String nomeProduto;

    @Column(unique = true, nullable = false, name = "url_imagem")
    String UrlImagem;

    @Column(unique = true, nullable = false, name = "descricao")
    String descricao;
    @Column(unique = true, nullable = false, name = "url_fotos")
    List<String> urlFotos;
    @Column(unique = false, name = "data_criacao")
    LocalDate dataCriacao = LocalDate.now();

    @Column(unique = false, nullable = false, name = "cidade_vendedor")
    String cidadeVendedor;

    @Column(unique = false, nullable = false, name = "preco_produto")
    BigDecimal PrecoProduto;

    @Column(unique = false, nullable = false, name = "categoria_produto")
    CategoriaProduto categoriaProduto;

    @Column(nullable = false, name = "pontuacao_produto")
    BigDecimal pontuacaoProduto = BigDecimal.ZERO;

    @Column(nullable = false, name = "quantidade_avaliadores")
    int quantidadeAvaliadores = 0;

    @OneToMany(mappedBy = "produto")
    private List<Avaliacao> avaliacoes;
}
