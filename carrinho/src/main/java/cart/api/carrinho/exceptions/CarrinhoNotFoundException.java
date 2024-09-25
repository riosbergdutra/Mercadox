package cart.api.carrinho.exceptions;

public class CarrinhoNotFoundException extends RuntimeException {
    public CarrinhoNotFoundException(String message) {
        super(message);
    }
}