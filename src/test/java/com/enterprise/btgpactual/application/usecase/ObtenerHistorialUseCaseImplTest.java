package com.enterprise.btgpactual.application.usecase;

import com.enterprise.btgpactual.domain.exception.ClienteNoEncontradoException;
import com.enterprise.btgpactual.domain.model.*;
import com.enterprise.btgpactual.domain.port.out.ClienteRepositoryPort;
import com.enterprise.btgpactual.domain.port.out.TransaccionRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ObtenerHistorialUseCase - Pruebas unitarias")
class ObtenerHistorialUseCaseImplTest {

    @Mock private ClienteRepositoryPort clienteRepository;
    @Mock private TransaccionRepositoryPort transaccionRepository;

    @InjectMocks
    private ObtenerHistorialUseCaseImpl obtenerHistorialUseCase;

    @Test
    @DisplayName("Debe retornar el historial de transacciones del cliente")
    void debeRetornarHistorialExitosamente() {
        // given
        Cliente cliente = Cliente.builder()
                .id("cliente-001")
                .saldo(new BigDecimal("425000"))
                .fondosSuscritos(new HashSet<>())
                .build();

        List<Transaccion> transacciones = List.of(
                Transaccion.builder()
                        .id("txn-001")
                        .clienteId("cliente-001")
                        .fondoId("1")
                        .fondoNombre("FPV_BTG_PACTUAL_RECAUDADORA")
                        .tipo(TipoTransaccion.APERTURA)
                        .monto(new BigDecimal("75000"))
                        .fecha(LocalDateTime.now().minusDays(2))
                        .build(),
                Transaccion.builder()
                        .id("txn-002")
                        .clienteId("cliente-001")
                        .fondoId("1")
                        .fondoNombre("FPV_BTG_PACTUAL_RECAUDADORA")
                        .tipo(TipoTransaccion.CANCELACION)
                        .monto(new BigDecimal("75000"))
                        .fecha(LocalDateTime.now().minusDays(1))
                        .build()
        );

        given(clienteRepository.buscarPorId("cliente-001")).willReturn(Optional.of(cliente));
        given(transaccionRepository.buscarPorClienteId("cliente-001")).willReturn(transacciones);

        // when
        List<Transaccion> resultado = obtenerHistorialUseCase.ejecutar("cliente-001");

        // then
        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).getTipo()).isEqualTo(TipoTransaccion.APERTURA);
        assertThat(resultado.get(1).getTipo()).isEqualTo(TipoTransaccion.CANCELACION);
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando el cliente no tiene transacciones")
    void debeRetornarListaVaciaSinTransacciones() {
        // given
        Cliente cliente = Cliente.builder()
                .id("cliente-001")
                .saldo(new BigDecimal("500000"))
                .fondosSuscritos(new HashSet<>())
                .build();

        given(clienteRepository.buscarPorId("cliente-001")).willReturn(Optional.of(cliente));
        given(transaccionRepository.buscarPorClienteId("cliente-001")).willReturn(List.of());

        // when
        List<Transaccion> resultado = obtenerHistorialUseCase.ejecutar("cliente-001");

        // then
        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("Debe lanzar ClienteNoEncontradoException cuando el cliente no existe")
    void debeLanzarExcepcionCuandoClienteNoExiste() {
        // given
        given(clienteRepository.buscarPorId("cliente-999")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> obtenerHistorialUseCase.ejecutar("cliente-999"))
                .isInstanceOf(ClienteNoEncontradoException.class)
                .hasMessageContaining("cliente-999");

        verify(transaccionRepository, never()).buscarPorClienteId(any());
    }
}
