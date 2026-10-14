package com.example.demo.controller;

import com.example.demo.dto.ComprasUnidadResponse;
import com.example.demo.service.CompraService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/compras")
public class CompraController {

    private final CompraService service;

    public CompraController(CompraService service) {
        this.service = service;
    }

    @GetMapping("/{unidad}")
    public ComprasUnidadResponse obtenerPorUnidad(
            @PathVariable("unidad") String unidad) {

        return service.obtenerPorUnidad(unidad);
    }
}