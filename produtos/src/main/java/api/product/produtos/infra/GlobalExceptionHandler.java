package api.product.produtos.infra;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import api.product.produtos.exceptions.FindAllProductsException;


@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FindAllProductsException.class)
    public ResponseEntity<String> handleProductServiceException(FindAllProductsException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Você pode adicionar outros manipuladores de exceção aqui
}
