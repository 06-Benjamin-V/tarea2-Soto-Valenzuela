package com.example.demo.service;

/** La unidad de la compra no es UF ni USD. */
public class UnidadNoSoportadaException extends RuntimeException {

    public UnidadNoSoportadaException(String unidad) {
        super("unidad no soportada: " + unidad + " (se espera UF o USD)");
    }
}
