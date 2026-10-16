package com.example.demo.service;

import java.math.BigDecimal;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.client.MindicadorClient;
import com.example.demo.dto.CompraDetalleResponse;
import com.example.demo.model.Compra;
import com.example.demo.repository.CompraRepository;

/** Desafio 2: detalle de una compra convertida a pesos. */
@Service
public class CompraDetalleService {

    private final CompraRepository compraRepository;
    private final MindicadorClient mindicadorClient;
    private final ConversionService conversionService;

    public CompraDetalleService(
            CompraRepository compraRepository,
            MindicadorClient mindicadorClient,
            ConversionService conversionService) {
        this.compraRepository = compraRepository;
        this.mindicadorClient = mindicadorClient;
        this.conversionService = conversionService;
    }

    public CompraDetalleResponse obtenerDetalle(long id) {
        Compra compra = compraRepository.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "no existe una compra con id " + id));

        BigDecimal valorUnidad = mindicadorClient.obtenerValor(compra.unidad(), compra.fecha());
        long montoPesos = conversionService.convertirAPesos(compra.monto(), valorUnidad);

        return new CompraDetalleResponse(
                compra.id(),
                compra.producto(),
                compra.fecha(),
                compra.monto(),
                compra.unidad(),
                valorUnidad,
                montoPesos);
    }
}
