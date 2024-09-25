package cart.api.carrinho.model;


import java.util.UUID;
import java.util.List;

import jakarta.persistence.*;

import lombok.Data;

@Entity
@Table(name = "carrinho")
@Data
public class Carrinho {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO) // ou UUID se suportado
    @Column(unique = true, nullable = false, name = "id_carrinho")
    private UUID idCarrinho;

    @Column(unique = true, nullable = false, name = "id_usuario")
    private UUID idUsuario;
    
    @ElementCollection
    @CollectionTable(name = "item_carrinho", joinColumns = @JoinColumn(name = "id_carrinho"))
    private List<ItemCarrinho> itens;
}