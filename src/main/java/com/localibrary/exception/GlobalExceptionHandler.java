package com.localibrary.exception;

import com.localibrary.util.Constants;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Manipulador global de exceções.
 * Captura todas as exceções lançadas pelos controllers e retorna respostas padronizadas.
 * Atende ao RNF-02: mensagens de erro claras para códigos HTTP.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Estrutura de resposta de erro padronizada
     */
    private static class ErrorResponse {
        private LocalDateTime timestamp;
        private int status;
        private String error;
        private String message;
        private String path;
        private Map<String, String> details;

        public ErrorResponse(int status, String error, String message, String path) {
            this.timestamp = LocalDateTime.now();
            this.status = status;
            this.error = error;
            this.message = message;
            this.path = path;
        }

        // Getters
        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public int getStatus() {
            return status;
        }

        public String getError() {
            return error;
        }

        public String getMessage() {
            return message;
        }

        public String getPath() {
            return path;
        }

        public Map<String, String> getDetails() {
            return details;
        }

        public void setDetails(Map<String, String> details) {
            this.details = details;
        }
    }

    /**
     * Trata exceção de recurso não encontrado.
     * HTTP 404 - Not Found
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {

        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    /**
     * Trata exceções de regra de negócio.
     * HTTP 400 - Bad Request ou 409 - Conflict
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex, WebRequest request) {

        // Se é conflito de dados (CNPJ duplicado, email já cadastrado, etc)
        HttpStatus status = ex.getCode() != null && ex.getCode().contains("DUPLICADO")
                ? HttpStatus.CONFLICT
                : HttpStatus.BAD_REQUEST;

        ErrorResponse error = new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(error, status);
    }

    /**
     * Trata exceções de autenticação.
     * HTTP 401 - Unauthorized
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(
            UnauthorizedException ex, WebRequest request) {

        ErrorResponse error = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Trata exceções de autorização (acesso negado).
     * HTTP 403 - Forbidden
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex, WebRequest request) {

        ErrorResponse error = new ErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                "Forbidden",
                Constants.MSG_PROIBIDO,
                request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    /**
     * Trata exceções de geolocalização.
     * HTTP 400 - Bad Request (endereço inválido) ou 500 (erro de API)
     */
    @ExceptionHandler(GeolocationException.class)
    public ResponseEntity<ErrorResponse> handleGeolocationException(
            GeolocationException ex, WebRequest request) {

        // Se tem endereço, provavelmente é erro do usuário (400)
        // Se não, é erro da API externa (500)
        HttpStatus status = ex.getAddress() != null
                ? HttpStatus.BAD_REQUEST
                : HttpStatus.INTERNAL_SERVER_ERROR;

        ErrorResponse error = new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );

        // Adiciona detalhes se disponíveis
        if (ex.getAddress() != null || ex.getApiError() != null) {
            Map<String, String> details = new HashMap<>();
            if (ex.getAddress() != null) {
                details.put("endereco", ex.getAddress());
            }
            if (ex.getApiError() != null) {
                details.put("erroAPI", ex.getApiError());
            }
            error.setDetails(details);
        }

        return new ResponseEntity<>(error, status);
    }

    /**
     * Trata erros de validação do Bean Validation.
     * HTTP 400 - Bad Request
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, WebRequest request) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Error",
                Constants.MSG_DADOS_INVALIDOS,
                request.getDescription(false).replace("uri=", "")
        );
        error.setDetails(errors);

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Trata exceções genéricas não mapeadas.
     * HTTP 500 - Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex, WebRequest request) {

        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                Constants.MSG_ERRO_GENERICO,
                request.getDescription(false).replace("uri=", "")
        );

        // Em ambiente de desenvolvimento, podemos adicionar mais detalhes
        // error.setDetails(Map.of("exception", ex.getClass().getName()));

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}