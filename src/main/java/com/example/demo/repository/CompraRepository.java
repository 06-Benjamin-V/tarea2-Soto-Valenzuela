package com.example.demo.repository;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.example.demo.model.Compra;

/**
 * Lee las compras desde el archivo CSV indicado en la variable de entorno CSV_PATH.
 * El CSV se lee en cada llamada para que el archivo pueda cambiarse sin reiniciar la app.
 */
@Repository
public class CompraRepository {

    private static final String ENCABEZADO = "id,producto,monto,unidad,fecha";

    private final Path csvPath;

    public CompraRepository(@Value("${compras.csv-path}") String csvPath) {
        this.csvPath = Path.of(csvPath);
    }

    public List<Compra> leerCompras() {
        List<String> lineas;
        try {
            lineas = Files.readAllLines(csvPath, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("no se pudo leer el CSV: " + csvPath, e);
        }

        if (lineas.isEmpty() || !lineas.get(0).trim().equals(ENCABEZADO)) {
            throw new IllegalArgumentException("el CSV debe tener el encabezado " + ENCABEZADO);
        }

        List<Compra> compras = new ArrayList<>();
        for (int i = 1; i < lineas.size(); i++) {
            String linea = lineas.get(i).trim();
            if (linea.isEmpty()) {
                continue;
            }
            String[] campos = linea.split(",", -1);
            if (campos.length != 5) {
                throw new IllegalArgumentException(
                        "la fila " + (i + 1) + " del CSV debe tener 5 columnas");
            }
            try {
                compras.add(new Compra(
                        Long.parseLong(campos[0].trim()),
                        campos[1].trim(),
                        new BigDecimal(campos[2].trim()),
                        campos[3].trim(),
                        LocalDate.parse(campos[4].trim())));
            } catch (NumberFormatException | DateTimeParseException e) {
                throw new IllegalArgumentException("la fila " + (i + 1) + " del CSV no es valida", e);
            }
        }
        return compras;
    }

    /** Devuelve la compra con ese id, o vacio si no existe. */
    public Optional<Compra> buscarPorId(long id) {
        return leerCompras().stream()
                .filter(compra -> compra.id() == id)
                .findFirst();
    }
}
