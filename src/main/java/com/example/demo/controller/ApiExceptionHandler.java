package com.example.demo.controller;

import java.io.UncheckedIOException;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.demo.service.UnidadNoSoportadaException;
import com.example.demo.service.ValorIndicadorNoDisponibleException;

/** Traduce los errores de negocio a respuestas HTTP claras. */
@RestControllerAdvice
public class ApiExceptionHandler {

    /** El CSV de compras no se pudo leer o tiene un formato invalido. */
    @ExceptionHandler({UncheckedIOException.class, IllegalArgumentException.class})
    public ResponseEntity<Map<String, Object>> csvInvalido(RuntimeException e) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }

    /** La compra tiene una unidad que no es UF ni USD. */
    @ExceptionHandler(UnidadNoSoportadaException.class)
    public ResponseEntity<Map<String, Object>> unidadNoSoportada(UnidadNoSoportadaException e) {
        return error(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    /** La API de indicadores no entrego valor para esa fecha. */
    @ExceptionHandler(ValorIndicadorNoDisponibleException.class)
    public ResponseEntity<Map<String, Object>> indicadorNoDisponible(
            ValorIndicadorNoDisponibleException e) {
        return error(HttpStatus.BAD_GATEWAY, e.getMessage());
    }

    private ResponseEntity<Map<String, Object>> error(HttpStatus estado, String mensaje) {
        return ResponseEntity.status(estado)
                .body(Map.of("status", estado.value(), "error", estado.getReasonPhrase(),
                        "message", mensaje == null ? estado.getReasonPhrase() : mensaje));
    }
}
