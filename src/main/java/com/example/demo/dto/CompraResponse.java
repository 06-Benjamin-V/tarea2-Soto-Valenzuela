package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CompraResponse(
        long id,
        String producto,
        BigDecimal monto,
        LocalDate fecha,
        @JsonProperty("valor_unidad") BigDecimal valorUnidad,
        @JsonProperty("monto_pesos") long montoPesos
) {
}