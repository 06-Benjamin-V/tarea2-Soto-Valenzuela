package com.example.demo.client;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@Component
public class MindicadorClient {
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private final RestClient restClient;

    public MindicadorClient() {
        this(RestClient.builder()
                .baseUrl("https://mindicador.cl/api")
                .requestFactory(crearRequestFactory())
                .build());
    }

    MindicadorClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public BigDecimal obtenerValor(String unidad, LocalDate fecha) {
        String indicador = switch (unidad) {
            case "UF" -> "uf";
            case "USD" -> "dolar";
            default -> throw new IllegalArgumentException("unidad no soportadaaaaaaaaa: " + unidad);
        };

        IndicadorResponse respuesta = consultarIndicador(indicador, fecha);

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

    private static SimpleClientHttpRequestFactory crearRequestFactory() {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(10000);
        return factory;
    }

    private IndicadorResponse consultarIndicador(
            String indicador, LocalDate fecha) {

        for (int intento = 1;; intento++) {
            try {
                return restClient.get()
                        .uri("/{indicador}/{fecha}",
                                indicador, fecha.format(FORMATO_FECHA))
                        .retrieve()
                        .body(IndicadorResponse.class);
            } catch (ResourceAccessException e) {
                if (intento == 2) {
                    throw e;
                }
            }
        }
    }

    public IndicadorResponse obtenerSerieAnual(String unidad, int anio) {
        String indicador = switch (unidad) {
            case "UF" -> "uf";
            case "USD" -> "dolar";
            default -> throw new IllegalArgumentException(
                    "Unidad no soportada: " + unidad);
        };

        for (int intento = 1;; intento++) {
            try {
                IndicadorResponse respuesta = restClient.get()
                        .uri("/{indicador}/{anio}", indicador, anio)
                        .retrieve()
                        .body(IndicadorResponse.class);

                if (respuesta == null || respuesta.serie() == null) {
                    throw new IllegalStateException(
                            "Respuesta inválida para " + indicador + "/" + anio);
                }

                return respuesta;
            } catch (ResourceAccessException e) {
                if (intento == 2) {
                    throw e;
                }
            }
        }
    }
}
