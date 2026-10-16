package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.CompraDetalleResponse;
import com.example.demo.service.CompraDetalleService;

/** Desafio 2: GET /compras/detalle/{id} */
@RestController
@RequestMapping("/compras")
public class CompraDetalleController {

    private final CompraDetalleService compraDetalleService;

    public CompraDetalleController(CompraDetalleService compraDetalleService) {
        this.compraDetalleService = compraDetalleService;
    }

    @GetMapping("/detalle/{id}")
    public CompraDetalleResponse detalle(@PathVariable long id) {
        return compraDetalleService.obtenerDetalle(id);
    }
}
