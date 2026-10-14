package com.example.demo.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class ConversionService {

    public long convertirAPesos(
            BigDecimal monto, BigDecimal valorUnidad) {

        return monto.multiply(valorUnidad)
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();
    }
}