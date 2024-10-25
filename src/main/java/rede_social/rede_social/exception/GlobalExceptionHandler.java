package rede_social.rede_social.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import rede_social.rede_social.exception.authException.*;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<String> handleMaxSizeException(MaxUploadSizeExceededException exc) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body("O tamanho do arquivo excede o limite máximo permitido!");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handleDataIntegrityViolationException(DataIntegrityViolationException exc) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body("Erro de integridade de dados: valor duplicado.");
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handleUserNotFoundException(UserNotFoundException exc) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exc.getMessage());
    }

    @ExceptionHandler(UserNotVerifiedException.class)
    public ResponseEntity<String> handleUserNotVerifiedException(UserNotVerifiedException exc) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(exc.getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<String> handleInvalidCredentialsException(InvalidCredentialsException exc) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(exc.getMessage());
    }

    @ExceptionHandler(ConfirmationCodeNotFoundException.class)
    public ResponseEntity<String> handleConfirmationCodeNotFoundException(ConfirmationCodeNotFoundException exc) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exc.getMessage());
    }

    @ExceptionHandler(InvalidConfirmationCodeException.class)
    public ResponseEntity<String> handleInvalidConfirmationCodeException(InvalidConfirmationCodeException exc) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exc.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGlobalException(Exception exc) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno do servidor: " + exc.getMessage());
    }
}
