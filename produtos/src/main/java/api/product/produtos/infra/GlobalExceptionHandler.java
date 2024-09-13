package api.product.produtos.infra;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import api.product.produtos.dtos.ErroDto.ErrorResponse;
import api.product.produtos.exceptions.FindAllProductsException;
import api.product.produtos.exceptions.ProdutoNotFoundException;
import api.product.produtos.exceptions.S3ImageDeletionException;
import api.product.produtos.exceptions.UsuarioNotFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProdutoNotFoundException.class)
    public ResponseEntity<String> handleProdutoNotFoundException(ProdutoNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ex.getMessage());
    }

    @ExceptionHandler(FindAllProductsException.class)
    public ResponseEntity<String> handleProductServiceException(FindAllProductsException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Ocorreu um erro interno. Tente novamente mais tarde.");
    }
    /**
     * Manipulador para exceções do tipo UsuarioNotFoundException.
     * 
     * @param ex Exceção lançada quando um usuário não é encontrado.
     * @return Resposta com status 404 (Not Found) e mensagem de erro.
     */
    @ExceptionHandler(UsuarioNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorResponse> handleUsuarioNotFound(UsuarioNotFoundException ex) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Manipulador para exceções de falha ao excluir imagem do S3.
     *
     * @param ex Exceção lançada.
     * @return Mensagem de erro e status HTTP.
     */
    @ExceptionHandler(S3ImageDeletionException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ResponseEntity<String> handleS3ImageDeletionException(S3ImageDeletionException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
