package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.client.MindicadorClient;
import com.example.demo.dto.CompraDetalleResponse;
import com.example.demo.model.Compra;
import com.example.demo.repository.CompraRepository;

@ExtendWith(MockitoExtension.class)
class CompraDetalleServiceTest {

    @Mock
    private CompraRepository compraRepository;

    @Mock
    private MindicadorClient mindicadorClient;

    @Mock
    private ConversionService conversionService;

    @InjectMocks
    private CompraDetalleService service;

    @Test
    void retornaLaCompraConvertidaAPesos() {
        Compra compra = new Compra(3L, "Escritorio", new BigDecimal("8.0"), "UF",
                LocalDate.of(2026, 9, 4));
        when(compraRepository.buscarPorId(3L)).thenReturn(Optional.of(compra));
        when(mindicadorClient.obtenerValor("UF", compra.fecha()))
                .thenReturn(new BigDecimal("41000.5"));
        when(conversionService.convertirAPesos(compra.monto(), new BigDecimal("41000.5")))
                .thenReturn(328004L);

        CompraDetalleResponse detalle = service.obtenerDetalle(3L);

        assertThat(detalle.id()).isEqualTo(3L);
        assertThat(detalle.producto()).isEqualTo("Escritorio");
        assertThat(detalle.fecha()).isEqualTo(LocalDate.of(2026, 9, 4));
        assertThat(detalle.monto()).isEqualByComparingTo("8.0");
        assertThat(detalle.unidad()).isEqualTo("UF");
        assertThat(detalle.valorUnidad()).isEqualByComparingTo("41000.5");
        assertThat(detalle.montoPesos()).isEqualTo(328004L);
    }

    @Test
    void consultaElIndicadorEnLaFechaDeLaCompra() {
        Compra compra = new Compra(2L, "Monitor", new BigDecimal("180"), "USD",
                LocalDate.of(2026, 9, 2));
        when(compraRepository.buscarPorId(2L)).thenReturn(Optional.of(compra));
        when(mindicadorClient.obtenerValor("USD", LocalDate.of(2026, 9, 2)))
                .thenReturn(new BigDecimal("950.5"));
        when(conversionService.convertirAPesos(compra.monto(), new BigDecimal("950.5")))
                .thenReturn(171090L);

        service.obtenerDetalle(2L);

        verify(mindicadorClient).obtenerValor("USD", LocalDate.of(2026, 9, 2));
    }

    @Test
    void responde404CuandoElIdNoExiste() {
        when(compraRepository.buscarPorId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtenerDetalle(99L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }
}
