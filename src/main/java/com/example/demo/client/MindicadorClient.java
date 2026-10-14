package com.example.demo.client;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class MindicadorClient {
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private final RestClient restClient;

    public MindicadorClient() {
        this.restClient = RestClient.builder()
                .baseUrl("https://mindicador.cl/api")
                .build();
    }

    public BigDecimal obtenerValor(String unidad, LocalDate fecha) {
        String indicador = switch (unidad) {
            case "UF" -> "uf";
            case "USD" -> "dolar";
            default -> throw new IllegalArgumentException("unidad no soportadaaaaaaaaa: " + unidad);
        };

        IndicadorResponse respuesta = restClient.get()
                .uri("/{indicador}/{fecha}", indicador, fecha.format(FORMATO_FECHA))
                .retrieve()
                .body(IndicadorResponse.class);

        if (respuesta == null || respuesta.serie() == null || respuesta.serie().isEmpty()) {
            throw new IllegalStateException("no hay valor de " + indicador + " para " + fecha);
        }
        var observacion = respuesta.serie().getFirst();

        if (observacion.fecha() == null
                || !observacion.fecha().toLocalDate().equals(fecha)
                || observacion.valor() == null
                || observacion.valor().signum() <= 0) {
            throw new IllegalStateException("el valor recibido no es valido para " + fecha);
        }
        return observacion.valor();
    }
}
