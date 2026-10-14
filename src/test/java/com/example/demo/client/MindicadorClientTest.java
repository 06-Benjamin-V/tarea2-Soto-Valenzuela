package com.example.demo.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class MindicadorClientTest {

    private MockRestServiceServer servidor;
    private MindicadorClient client;

    @BeforeEach
    void configurar() {
        var builder = RestClient.builder()
                .baseUrl("https://mindicador.cl/api");

        servidor = MockRestServiceServer.bindTo(builder).build();
        client = new MindicadorClient(builder.build());
    }

    @ParameterizedTest
    @CsvSource({
            "UF, uf, 40000.25",
            "USD, dolar, 950.50"
    })
    void consultaIndicadorPorFechaYLeeSuValor(
            String unidad, String indicador, String valor) {

        String json = """
                {
                  "codigo": "%s",
                  "nombre": "Indicador de prueba",
                  "serie": [
                    {
                      "fecha": "2026-09-01T04:00:00.000Z",
                      "valor": %s
                    }
                  ]
                }
                """.formatted(indicador, valor);

        servidor.expect(requestTo(
                        "https://mindicador.cl/api/" + indicador + "/01-09-2026"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        BigDecimal resultado = client.obtenerValor(
                unidad, LocalDate.of(2026, 9, 1));

        assertEquals(0, new BigDecimal(valor).compareTo(resultado));
        servidor.verify();
    }

    @Test
    void rechazaUnaFechaSinValorDisponible() {
        servidor.expect(requestTo(
                        "https://mindicador.cl/api/uf/01-09-2026"))
                .andRespond(withSuccess(
                        """
                        {"codigo": "uf", "serie": []}
                        """,
                        MediaType.APPLICATION_JSON));

        assertThrows(IllegalStateException.class,
                () -> client.obtenerValor("UF", LocalDate.of(2026, 9, 1)));

        servidor.verify();
    }
}