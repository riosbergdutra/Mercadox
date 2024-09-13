package api.product.produtos.exceptions;

public class S3ImageDeletionException extends RuntimeException {
    public S3ImageDeletionException(String message) {
        super(message);
    }

    public S3ImageDeletionException(String message, Throwable cause) {
        super(message, cause);
    }
}