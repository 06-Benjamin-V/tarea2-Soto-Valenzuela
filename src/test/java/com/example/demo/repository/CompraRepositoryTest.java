package com.example.demo.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.example.demo.model.Compra;

class CompraRepositoryTest {

    @TempDir
    Path tempDir;

    private CompraRepository repositorioCon(String contenido) throws Exception {
        Path csv = tempDir.resolve("compras.csv");
        Files.writeString(csv, contenido, StandardCharsets.UTF_8);
        return new CompraRepository(csv.toString());
    }

    @Test
    void leeComprasConservandoDatosYOrden() throws Exception {
        CompraRepository repositorio = repositorioCon("""
                id,producto,monto,unidad,fecha
                3,Escritorio,8,UF,2026-09-04
                1,Notebook,25.5,UF,2026-09-01
                """);

        assertEquals(List.of(
                new Compra(3, "Escritorio", new BigDecimal("8"),
                        "UF", LocalDate.of(2026, 9, 4)),
                new Compra(1, "Notebook", new BigDecimal("25.5"),
                        "UF", LocalDate.of(2026, 9, 1))),
                repositorio.leerCompras());
    }

    @Test
    void leeLasComprasEnElOrdenDelCsv() throws Exception {
        CompraRepository repositorio = repositorioCon("""
                id,producto,monto,unidad,fecha
                1,Notebook,25.5,UF,2026-09-01
                2,Monitor,180,USD,2026-09-02
                """);

        var compras = repositorio.leerCompras();

        assertThat(compras).hasSize(2);
        assertThat(compras.get(0).producto()).isEqualTo("Notebook");
        assertThat(compras.get(1).producto()).isEqualTo("Monitor");
    }

    @Test
    void buscaUnaCompraPorId() throws Exception {
        CompraRepository repositorio = repositorioCon("""
                id,producto,monto,unidad,fecha
                3,Escritorio,8,UF,2026-09-04
                """);

        Compra compra = repositorio.buscarPorId(3L).orElseThrow();

        assertThat(compra.producto()).isEqualTo("Escritorio");
        assertThat(compra.monto()).isEqualByComparingTo(new BigDecimal("8"));
        assertThat(compra.unidad()).isEqualTo("UF");
    }

    @Test
    void devuelveVacioSiElIdNoExiste() throws Exception {
        CompraRepository repositorio = repositorioCon("""
                id,producto,monto,unidad,fecha
                3,Escritorio,8,UF,2026-09-04
                """);

        assertThat(repositorio.buscarPorId(99L)).isEmpty();
    }
}
