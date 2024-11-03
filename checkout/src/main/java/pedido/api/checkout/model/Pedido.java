package pedido.api.checkout.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Data;
import pedido.api.checkout.enums.EstadoPedido;
import pedido.api.checkout.enums.FormaPagamento;

@Entity
@Table(name = "pedido")
@Data
public class Pedido {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(unique = true, nullable = false, name = "id_pedido")
    private UUID idPedido;

    @Column(unique = false, nullable = false, name = "id_usuario")
    private UUID idUsuario;

    @Column(unique = false, nullable = false, name = "endereco_entrega")
    @Embedded
    private Endereco enderecoEntrega;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "item_carrinho", joinColumns = @JoinColumn(name = "id_carrinho"))
    private List<Produtos> itens;

    @Column(unique = false, nullable = false, name = "valor_compra")
    private BigDecimal valorCompra;

    @Enumerated(EnumType.STRING)
    @Column(unique = false, nullable = false, name = "estado_pedido")
    private EstadoPedido estadoDoPedido;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "forma_pagamento")
    private FormaPagamento formaPagamento;
}
