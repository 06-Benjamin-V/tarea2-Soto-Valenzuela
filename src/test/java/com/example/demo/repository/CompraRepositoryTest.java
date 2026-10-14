package com.example.demo.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.io.TempDir;

import com.example.demo.model.Compra;

class CompraRepositoryTest {
    @TempDir
    Path carpetaTemporal;

    void leeComprasConservandoDatosYOrden() throws Exception {
        Path archivo = carpetaTemporal.resolve("compras.csv");

        Files.writeString(archivo, """
                id,producto,monto,unidad,fecha
                3,Escritorio,8,UF,2026-09-04
                1,Notebook,25.5,UF,2026-09-01
                """);
        var repository = new CompraRepository(archivo.toString());

        assertEquals(List.of(
                new Compra(3, "Escritorio", new BigDecimal("8"),
                        "UF", LocalDate.of(2026, 9, 4))),
                repository.leerCompras());
    }
}