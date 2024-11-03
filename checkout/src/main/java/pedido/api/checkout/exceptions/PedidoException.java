package pedido.api.checkout.exceptions;

public class PedidoException extends RuntimeException {
    public PedidoException(String message) {
        super(message);
    }
}