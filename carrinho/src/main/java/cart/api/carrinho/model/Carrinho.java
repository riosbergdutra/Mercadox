package cart.api.carrinho.model;

import java.util.UUID;
import java.util.List;

import jakarta.persistence.*;
import java.math.BigDecimal;

import lombok.Data;

@Entity
@Table(name = "carrinho")
@Data
public class Carrinho {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // ou UUID se suportado
    @Column(unique = true, nullable = false, name = "id_carrinho")
    private UUID idCarrinho;

    @Column(unique = true, nullable = false, name = "id_usuario")
    private UUID idUsuario;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "item_carrinho", joinColumns = @JoinColumn(name = "id_carrinho"))
    private List<ItemCarrinho> itens;

    @Column(name = "valor_total", nullable = false)
    private BigDecimal valorTotal = BigDecimal.ZERO;

    public void calcularValorTotal() {
        this.valorTotal = itens.stream()
                .map(ItemCarrinho::getValorTotalItem) 
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}