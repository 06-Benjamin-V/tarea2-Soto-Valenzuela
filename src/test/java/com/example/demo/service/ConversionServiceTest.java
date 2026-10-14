package com.example.demo.service;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConversionServiceTest {

    private final ConversionService service = new ConversionService();

    @ParameterizedTest
    @CsvSource({
            "25.5, 40000.25, 1020006",
            "95.9, 950.50, 91153",
            "1, 100.49, 100",
            "1, 100.50, 101",
            "1, 100.51, 101",
            "0, 950.50, 0"
    })
    void convierteAPesosYRedondea(
            String monto, String valorUnidad, long esperado) {

        long resultado = service.convertirAPesos(
                new BigDecimal(monto),
                new BigDecimal(valorUnidad)
        );

        assertEquals(esperado, resultado);
    }
}