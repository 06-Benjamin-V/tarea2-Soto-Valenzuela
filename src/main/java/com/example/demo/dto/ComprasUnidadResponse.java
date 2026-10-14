package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ComprasUnidadResponse(
        String unidad,
        List<CompraResponse> compras,
        @JsonProperty("total_pesos") long totalPesos
) {
}