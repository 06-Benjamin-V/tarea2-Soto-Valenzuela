package com.example.demo.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Detalle de una compra, ya convertida a pesos.
 *
 * Los nombres de los campos en el JSON son exactamente los del enunciado:
 * id, producto, fecha, monto, unidad, valor_unidad, monto_pesos.
 */
public record CompraDetalleResponse(
        long id,
        String producto,
        LocalDate fecha,
        BigDecimal monto,
        String unidad,
        @JsonProperty("valor_unidad") BigDecimal valorUnidad,
        @JsonProperty("monto_pesos") long montoPesos) {
}
