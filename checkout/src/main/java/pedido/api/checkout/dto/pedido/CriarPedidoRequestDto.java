package pedido.api.checkout.dto.pedido;


import jakarta.validation.constraints.NotNull;
import pedido.api.checkout.enums.FormaPagamento;
import pedido.api.checkout.model.Endereco;
import pedido.api.checkout.model.Produtos;
import java.util.List;
import java.math.BigDecimal;

public record CriarPedidoRequestDto(

    @NotNull
     Endereco enderecoEntrega,

    @NotNull
     List<Produtos> itens,

    @NotNull
     BigDecimal valorCompra,

    @NotNull
     FormaPagamento formaPagamento
) {
    
}
