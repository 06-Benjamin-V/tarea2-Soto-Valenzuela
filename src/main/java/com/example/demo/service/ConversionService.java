package com.example.demo.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Service;

/** Convierte montos a pesos chilenos, redondeando al peso. */
@Service
public class ConversionService {

    public long convertirAPesos(BigDecimal monto, BigDecimal valorUnidad) {
        return monto.multiply(valorUnidad)
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();
    }
}
