package pedido.api.checkout.infra;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import pedido.api.checkout.dto.ErrorResponse;
import pedido.api.checkout.exceptions.PedidoException;

@ControllerAdvice
public class GlobalExceptionHandler {
     @ExceptionHandler(PedidoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public String hadlePedidoException(PedidoException ex) {
        return ex.getMessage();
    }
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse errorResponse = new ErrorResponse("An unexpected error occurred");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
