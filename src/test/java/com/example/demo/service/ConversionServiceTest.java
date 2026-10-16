package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.example.demo.client.MindicadorClient;

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
    void convierteAPesosYRedondea(String monto, String valorUnidad, long esperado) {
        long resultado = service.convertirAPesos(new BigDecimal(monto), new BigDecimal(valorUnidad));

        assertThat(resultado).isEqualTo(esperado);
    }

    @ParameterizedTest
    @CsvSource({"UF, uf", "USD, dolar", "uf, uf", "usd, dolar", " uf , uf"})
    void traduceLaUnidadAlCodigoDeLaApi(String unidad, String codigoEsperado) {
        assertThat(MindicadorClient.codigoDe(unidad)).isEqualTo(codigoEsperado);
    }

    @ParameterizedTest
    @CsvSource({"EUR", "CLP", "''"})
    void rechazaUnidadesNoSoportadas(String unidad) {
        assertThatThrownBy(() -> MindicadorClient.codigoDe(unidad))
                .isInstanceOf(UnidadNoSoportadaException.class);
    }
}
