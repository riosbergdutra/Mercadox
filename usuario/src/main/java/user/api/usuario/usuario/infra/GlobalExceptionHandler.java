package user.api.usuario.usuario.infra;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import user.api.usuario.usuario.dtos.ErroDto.ErrorResponse;
import user.api.usuario.usuario.exceptions.EnderecoNotFoundException;
import user.api.usuario.usuario.exceptions.ImageProcessingException;
import user.api.usuario.usuario.exceptions.InvalidCredentialsException;
import user.api.usuario.usuario.exceptions.S3ImageDeletionException;
import user.api.usuario.usuario.exceptions.UsuarioNotFoundException;
import org.springframework.http.HttpStatus;

/**
 * Classe de manipulação global de exceções.
 * 
 * Esta classe fornece manipuladores para exceções específicas e retorna
 * respostas apropriadas para o cliente.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

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
     * Manipulador para exceções do tipo UsuarioNotFoundException.
     * 
     * @param ex Exceção lançada quando um usuário não é encontrado.
     * @return Resposta com status 404 (Not Found) e mensagem de erro.
     */
    @ExceptionHandler(EnderecoNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorResponse> handleEmailNotFound(EnderecoNotFoundException ex) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Manipulador para exceções do tipo ImageProcessingException.
     * 
     * @param ex Exceção lançada durante o processamento de uma imagem.
     * @return Resposta com status 400 (Bad Request) e mensagem de erro.
     */
    @ExceptionHandler(ImageProcessingException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleImageProcessingException(ImageProcessingException ex) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Manipulador para exceções do tipo InvalidCredentialsException.
     * 
     * @param ex Exceção lançada quando as credenciais fornecidas são inválidas.
     * @return Resposta com status 401 (Unauthorized) e mensagem de erro.
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<ErrorResponse> handleInvalidCredentialsException(InvalidCredentialsException ex) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * Manipulador para exceções genéricas.
     * 
     * @param ex Exceção genérica lançada em casos não tratados.
     * @return Resposta com status 500 (Internal Server Error) e mensagem de erro
     *         padrão.
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse errorResponse = new ErrorResponse("An unexpected error occurred");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
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
