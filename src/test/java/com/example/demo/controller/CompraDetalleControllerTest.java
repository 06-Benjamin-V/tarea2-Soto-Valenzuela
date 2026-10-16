package com.example.demo.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.dto.CompraDetalleResponse;
import com.example.demo.service.CompraDetalleService;
import com.example.demo.service.ValorIndicadorNoDisponibleException;

@WebMvcTest(CompraDetalleController.class)
class CompraDetalleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CompraDetalleService compraDetalleService;

    @Test
    void devuelveElDetalleConLosCamposDelEnunciado() throws Exception {
        when(compraDetalleService.obtenerDetalle(1L)).thenReturn(new CompraDetalleResponse(
                1L, "Notebook", LocalDate.of(2026, 9, 1), new BigDecimal("25.5"), "UF",
                new BigDecimal("41000.5"), 1045513L));

        mockMvc.perform(get("/compras/detalle/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.producto").value("Notebook"))
                .andExpect(jsonPath("$.fecha").value("2026-09-01"))
                .andExpect(jsonPath("$.monto").value(25.5))
                .andExpect(jsonPath("$.unidad").value("UF"))
                .andExpect(jsonPath("$.valor_unidad").value(41000.5))
                .andExpect(jsonPath("$.monto_pesos").value(1045513));
    }

    @Test
    void responde404CuandoLaCompraNoExiste() throws Exception {
        when(compraDetalleService.obtenerDetalle(eq(99L)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "no existe"));

        mockMvc.perform(get("/compras/detalle/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void responde502CuandoLaApiNoEntregaValor() throws Exception {
        when(compraDetalleService.obtenerDetalle(eq(7L)))
                .thenThrow(new ValorIndicadorNoDisponibleException("sin valor para esa fecha"));

        mockMvc.perform(get("/compras/detalle/7"))
                .andExpect(status().isBadGateway());
    }
}
