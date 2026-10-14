package com.example.demo.service;

import java.util.ArrayList;
import java.util.HashMap;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.client.IndicadorResponse;
import com.example.demo.client.MindicadorClient;
import com.example.demo.dto.CompraResponse;
import com.example.demo.dto.ComprasUnidadResponse;
import com.example.demo.repository.CompraRepository;

@Service
public class CompraService {

    private final CompraRepository repository;
    private final MindicadorClient client;
    private final ConversionService conversionService;

    public CompraService(
            CompraRepository repository,
            MindicadorClient client,
            ConversionService conversionService) {

        this.repository = repository;
        this.client = client;
        this.conversionService = conversionService;
    }

    public ComprasUnidadResponse obtenerPorUnidad(String unidad) {
        String unidadCsv = switch (unidad) {
            case "uf" -> "UF";
            case "dolar" -> "USD";
            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unidad inválida. Use uf o dolar");
        };

        var compras = new ArrayList<CompraResponse>();
        long totalPesos = 0;
        var seriesPorAnio = new HashMap<Integer, IndicadorResponse>();

        for (var compra : repository.leerCompras()) {
            if (!compra.unidad().equals(unidadCsv)) {
                continue;
            }

            var serieAnual = seriesPorAnio.computeIfAbsent(
                    compra.fecha().getYear(),
                    anio -> client.obtenerSerieAnual(unidadCsv, anio));

            var observacion = serieAnual.serie().stream()
                    .filter(dato -> dato.fecha() != null
                            && dato.fecha().toLocalDate().equals(compra.fecha()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            "No hay valor de " + unidadCsv + " para " + compra.fecha()));

            var valorUnidad = observacion.valor();

            if (valorUnidad == null || valorUnidad.signum() <= 0) {
                throw new IllegalStateException(
                        "Valor inválido para " + compra.fecha());
            }
            long montoPesos = conversionService.convertirAPesos(
                    compra.monto(), valorUnidad);

            compras.add(new CompraResponse(
                    compra.id(),
                    compra.producto(),
                    compra.monto(),
                    compra.fecha(),
                    valorUnidad,
                    montoPesos));

            totalPesos = Math.addExact(totalPesos, montoPesos);
        }

        return new ComprasUnidadResponse(
                unidadCsv, compras, totalPesos);
    }
}