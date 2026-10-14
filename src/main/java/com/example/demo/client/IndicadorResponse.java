package com.example.demo.client;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Respuesta de la API de indicadores para un indicador economico.
 * Ejemplo: {"codigo":"uf","serie":[{"fecha":"2026-09-01T03:00:00.000Z","valor":41000.5}]}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record IndicadorResponse(
        String codigo,
        List<Observacion> serie) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Observacion(
            OffsetDateTime fecha,
            BigDecimal valor) {
    }
}
