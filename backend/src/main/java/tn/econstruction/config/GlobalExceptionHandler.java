package tn.econstruction.config;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.*;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import tn.econstruction.exception.BusinessException;

import java.time.LocalDateTime;
import java.util.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * Exceptions métier : la clé est résolue dans messages_fr/messages_ar.properties
     * selon la langue de la requête (en-tête Accept-Language, voir I18nConfig).
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessException(BusinessException ex) {
        String message = messageSource.getMessage(ex.getMessageKey(), ex.getArgs(), LocaleContextHolder.getLocale());
        return buildResponse(HttpStatus.BAD_REQUEST, message);
    }

    /**
     * Filet de sécurité pour les exceptions non (encore) converties en BusinessException :
     * le message reste celui codé en dur dans le code (donc en français). À terme, toute
     * exception métier devrait passer par BusinessException plutôt que par ce handler.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /** Messages de validation (@NotBlank, @Pattern...) : déjà résolus dans la bonne langue
     * par Bean Validation, qui s'appuie sur ce même MessageSource via LocaleContextHolder. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors()
                .stream().map(FieldError::getDefaultMessage).toList();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", 400);
        body.put("errors", errors);
        return ResponseEntity.badRequest().body(body);
    }

    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}
