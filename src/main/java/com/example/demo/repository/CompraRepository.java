package com.example.demo.repository;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.example.demo.model.Compra;

@Repository
public class CompraRepository {
    private final Path csvPath;

    public CompraRepository(@Value("${compras.csv-path}") String csvPath) {
        this.csvPath = Path.of(csvPath);
    }

    public List<Compra> leerCompras() {
        try {
            List<String> lineas = Files.readAllLines(
                    csvPath, StandardCharsets.UTF_8);

            if (lineas.isEmpty()
                    || !lineas.getFirst().equals("id,producto,monto,unidad,fecha")) {
                throw new IllegalArgumentException(
                        "El CSV debe tener el encabezado id,producto,monto,unidad,fecha");
            }
            List<Compra> compras = new ArrayList<>();

            for (int i = 1; i < lineas.size(); i++) {
                String linea = lineas.get(i);

                if (linea.isBlank()) {
                    continue;
                }
                String[] campos = linea.split(",", -1);

                if (campos.length != 5) {
                    throw new IllegalArgumentException("la fla" + (i + 1) + "debe tener 5 columnnas");
                }
                compras.add(new Compra(
                        Long.parseLong(campos[0].trim()),
                        campos[1].trim(),
                        new BigDecimal(campos[2].trim()),
                        campos[3].trim(),
                        LocalDate.parse(campos[4].trim())));
            }
            return compras;
        } catch (IOException e) {
            throw new UncheckedIOException(
                    "no se puede leer el CSV: " + csvPath, e);
        }
    }
}
