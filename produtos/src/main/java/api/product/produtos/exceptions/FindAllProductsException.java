package api.product.produtos.exceptions;

public class FindAllProductsException extends RuntimeException {

    public FindAllProductsException(String message) {
        super(message);
    }

    public FindAllProductsException(String message, Throwable cause) {
        super(message, cause);
    }
}
