package com.localibrary.exception;

import com.localibrary.dto.ApiErrorDTO;
import com.localibrary.util.Constants; // Import Constants
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiErrorDTO> handleEntityNotFound(EntityNotFoundException ex) {
        // Usa mensagem genérica se a exceção não tiver msg, ou concatena
        String msg = ex.getMessage() != null ? ex.getMessage() : Constants.MSG_NAO_ENCONTRADO;
        return buildResponse(HttpStatus.NOT_FOUND, msg, null);
    }

    @ExceptionHandler(EntityExistsException.class)
    public ResponseEntity<ApiErrorDTO> handleEntityExists(EntityExistsException ex) {
        return buildResponse(HttpStatus.CONFLICT, Constants.MSG_CONFLITO + ": " + ex.getMessage(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorDTO> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage()).toList();

        return buildResponse(HttpStatus.BAD_REQUEST, Constants.MSG_DADOS_INVALIDOS, errors);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorDTO> handleAccessDenied(AccessDeniedException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, Constants.MSG_PROIBIDO, null);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorDTO> handleAuthenticationException(AuthenticationException ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED, Constants.MSG_NAO_AUTORIZADO, null);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorDTO> handleIllegalArgument(IllegalArgumentException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorDTO> handleGenericException(Exception ex) {
        ex.printStackTrace();
        // Usa mensagem genérica para erros 500
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, Constants.MSG_ERRO_GENERICO, List.of(ex.getMessage()));
    }

    // Método auxiliar para limpar o código
    private ResponseEntity<ApiErrorDTO> buildResponse(HttpStatus status, String msg, List<String> errors) {
        ApiErrorDTO error = new ApiErrorDTO(status.value(), msg, errors);
        return new ResponseEntity<>(error, status);
    }
}