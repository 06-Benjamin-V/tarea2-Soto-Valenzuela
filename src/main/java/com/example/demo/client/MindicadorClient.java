package com.example.demo.client;

import java.math.BigDecimal;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import com.example.demo.service.UnidadNoSoportadaException;
import com.example.demo.service.ValorIndicadorNoDisponibleException;

/**
 * Cliente de la API publica de indicadores.
 *
 * Para el valor de un dia se usa la consulta por fecha:
 * /api/{indicador}/{dd-MM-yyyy},
 * porque el enunciado exige el valor de la UF o del dolar EN LA FECHA de cada
 * compra.
 *
 * Ademas se configuran timeouts y reintentos porque la API responde lento
 * o de forma intermitente.
 */
@Component
public class MindicadorClient {

    private static final Logger log = LoggerFactory.getLogger(MindicadorClient.class);

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final int INTENTOS = 3;
    private static final Duration ESPERA_BASE = Duration.ofMillis(600);

    private final RestClient restClient;

    public MindicadorClient() {
        this(construirRestClient());
    }

    MindicadorClient(RestClient restClient) {
        this.restClient = restClient;
    }

    private static RestClient construirRestClient() {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(10000);

        return RestClient.builder()
                .baseUrl("https://mindicador.cl/api")
                .requestFactory(factory)
                .build();
    }

    public BigDecimal obtenerValor(String unidad, LocalDate fecha) {
        String indicador = codigoDe(unidad);

        IndicadorResponse respuesta = conReintentos(
                () -> restClient.get()
                        .uri("/{indicador}/{fecha}", indicador, fecha.format(FORMATO_FECHA))
                        .retrieve()
                        .body(IndicadorResponse.class),
                indicador, fecha);

        if (respuesta == null || respuesta.serie() == null || respuesta.serie().isEmpty()) {
            throw new ValorIndicadorNoDisponibleException(
                    "no hay valor de " + indicador + " para la fecha " + fecha);
        }

        var observacion = respuesta.serie().getFirst();

        if (observacion.fecha() == null
                || !observacion.fecha().toLocalDate().equals(fecha)
                || observacion.valor() == null
                || observacion.valor().signum() <= 0) {
            throw new ValorIndicadorNoDisponibleException(
                    "el valor recibido de " + indicador + " no es valido para la fecha " + fecha);
        }

        return observacion.valor();
    }

    public IndicadorResponse obtenerSerieAnual(String unidad, int anio) {
        String indicador = codigoDe(unidad);

        IndicadorResponse respuesta = conReintentos(
                () -> restClient.get()
                        .uri("/{indicador}/{anio}", indicador, anio)
                        .retrieve()
                        .body(IndicadorResponse.class),
                indicador, anio);

        if (respuesta == null || respuesta.serie() == null) {
            throw new ValorIndicadorNoDisponibleException(
                    "respuesta invalida para " + indicador + "/" + anio);
        }

        return respuesta;
    }

    private <T> T conReintentos(Consulta<T> consulta, String indicador, Object referencia) {
        RuntimeException ultimoError = null;

        for (int intento = 1; intento <= INTENTOS; intento++) {
            try {
                return consulta.ejecutar();
            } catch (ResourceAccessException e) {
                ultimoError = new ValorIndicadorNoDisponibleException(
                        "no se pudo consultar " + indicador + " en " + referencia, e);
            } catch (ValorIndicadorNoDisponibleException e) {
                ultimoError = e;
            }

            log.warn("intento {}/{} fallo al consultar {} en {}: {}",
                    intento, INTENTOS, indicador, referencia, ultimoError.getMessage());

            if (intento < INTENTOS) {
                esperar(ESPERA_BASE.multipliedBy(intento));
            }
        }
        throw ultimoError;
    }

    @FunctionalInterface
    private interface Consulta<T> {
        T ejecutar();
    }

    private static void esperar(Duration duracion) {
        try {
            Thread.sleep(duracion.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ValorIndicadorNoDisponibleException(
                    "consulta interrumpida mientras se esperaba reintentar", e);
        }
    }

    public static String codigoDe(String unidad) {
        if (unidad == null) {
            throw new UnidadNoSoportadaException(null);
        }
        return switch (unidad.trim().toUpperCase()) {
            case "UF" -> "uf";
            case "USD" -> "dolar";
            default -> throw new UnidadNoSoportadaException(unidad);
        };
    }
}
