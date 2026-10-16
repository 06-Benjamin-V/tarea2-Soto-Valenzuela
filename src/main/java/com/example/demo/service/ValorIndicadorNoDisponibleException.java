package com.example.demo.service;

/** No se pudo obtener el valor de la UF o del dolar para la fecha de la compra. */
public class ValorIndicadorNoDisponibleException extends RuntimeException {

    public ValorIndicadorNoDisponibleException(String mensaje) {
        super(mensaje);
    }

    public ValorIndicadorNoDisponibleException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
