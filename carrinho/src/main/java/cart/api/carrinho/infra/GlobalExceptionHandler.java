package cart.api.carrinho.infra;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import cart.api.carrinho.exceptions.CarrinhoNotFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CarrinhoNotFoundException.class)
    public ResponseEntity<String> handleCarrinhoNotFoundException(CarrinhoNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
}
