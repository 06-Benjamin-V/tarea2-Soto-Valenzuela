package com.example.demo.client;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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
