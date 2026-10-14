package com.example.demo.client;

import java.math.BigDecimal;
import java.net.http.HttpClient;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.example.demo.service.UnidadNoSoportadaException;
import com.example.demo.service.ValorIndicadorNoDisponibleException;

/**
 * Cliente de la API publica de indicadores (mindicador.cl).
 *
 * Se usa la consulta por fecha: /api/{indicador}/{dd-MM-yyyy}
 * porque el enunciado exige el valor de la UF o del dolar EN LA FECHA de cada compra.
 * La ruta de resumen (/api/) solo entrega el valor del dia actual.
 *
 * La API publica responde lento o de forma intermitente, por eso se configuran
 * timeouts y algunos reintentos con espera creciente.
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
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofSeconds(20));

        return RestClient.builder()
                .baseUrl("https://mindicador.cl/api")
                .requestFactory(factory)
                .build();
    }

    public BigDecimal obtenerValor(String unidad, LocalDate fecha) {
        String indicador = codigoDe(unidad);

        RuntimeException ultimoError = null;
        for (int intento = 1; intento <= INTENTOS; intento++) {
            try {
                return consultar(indicador, fecha);
            } catch (ValorIndicadorNoDisponibleException e) {
                ultimoError = e;
                log.warn("intento {}/{} fallo al consultar {} en {}: {}",
                        intento, INTENTOS, indicador, fecha, e.getMessage());
                if (intento < INTENTOS) {
                    esperar(ESPERA_BASE.multipliedBy(intento));
                }
            }
        }
        throw ultimoError;
    }

    private BigDecimal consultar(String indicador, LocalDate fecha) {
        IndicadorResponse respuesta;
        try {
            respuesta = restClient.get()
                    .uri("/{indicador}/{fecha}", indicador, fecha.format(FORMATO_FECHA))
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (peticion, respuestaHttp) -> {
                        throw new ValorIndicadorNoDisponibleException(
                                "la API respondio " + respuestaHttp.getStatusCode()
                                        + " para " + indicador + " en " + fecha);
                    })
                    .body(IndicadorResponse.class);
        } catch (ValorIndicadorNoDisponibleException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new ValorIndicadorNoDisponibleException(
                    "no se pudo consultar el indicador " + indicador + " en " + fecha, e);
        }

        if (respuesta == null || respuesta.serie() == null || respuesta.serie().isEmpty()) {
            throw new ValorIndicadorNoDisponibleException(
                    "no hay valor de " + indicador + " para la fecha " + fecha);
        }

        var observacion = respuesta.serie().get(0);

        if (observacion.fecha() == null
                || !observacion.fecha().toLocalDate().equals(fecha)
                || observacion.valor() == null
                || observacion.valor().signum() <= 0) {
            throw new ValorIndicadorNoDisponibleException(
                    "el valor recibido de " + indicador + " no es valido para la fecha " + fecha);
        }

        return observacion.valor();
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

    /** Traduce la unidad del CSV al codigo de indicador de la API. */
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
