package api.product.produtos.model;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "avaliacoes")
@Data
public class Avaliacao {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(unique = true, nullable = false, name = "id_avaliacao")
    UUID idAvaliacao;

    @Column(nullable = false, name = "id_usuario")
    UUID idUsuario;

    @Column(nullable = false, name = "pontuacao")
    BigDecimal pontuacao;

    @ManyToOne
    @JoinColumn(name = "id_produto", nullable = false)
    private Produto produto;
}
