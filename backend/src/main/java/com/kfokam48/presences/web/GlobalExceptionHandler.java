package com.kfokam48.presences.web;

import com.kfokam48.presences.exception.ApiException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Map;

/**
 * B4 : gestion centralisée des erreurs. Toute erreur — sans exception —
 * sort au format imposé { "code": "...", "message": "..." }. Aucune stack trace.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static ResponseEntity<Object> body(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(Map.of("code", code, "message", message));
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Object> apiException(ApiException e) {
        return body(e.getStatus(), e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> validation(MethodArgumentNotValidException e) {
        String champ = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(f -> f.getField())
                .orElse("corps");
        return body(HttpStatus.BAD_REQUEST, "CHAMP_MANQUANT", "Le champ " + champ + " est obligatoire.");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Object> corpsIllisible(HttpMessageNotReadableException e) {
        return body(HttpStatus.BAD_REQUEST, "CORPS_INVALIDE",
                "Le corps de la requête est manquant ou mal formé.");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Object> parametreInvalide(MethodArgumentTypeMismatchException e) {
        return body(HttpStatus.BAD_REQUEST, "PARAMETRE_INVALIDE",
                "Le paramètre " + e.getName() + " est invalide.");
    }

    /** Filet de sécurité : une contrainte d'unicité peut être violée par deux requêtes concurrentes. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> integrite(DataIntegrityViolationException e) {
        return body(HttpStatus.CONFLICT, "CONFLIT",
                "L'opération entre en conflit avec une donnée existante.");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Object> introuvable(NoResourceFoundException e) {
        return body(HttpStatus.NOT_FOUND, "RESSOURCE_INCONNUE", "Cette ressource n'existe pas.");
    }

    /** Dernier recours : jamais de stack trace vers le client (B4). */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> inattendue(Exception e) {
        return body(HttpStatus.INTERNAL_SERVER_ERROR, "ERREUR_INTERNE",
                "Une erreur interne est survenue.");
    }
}
