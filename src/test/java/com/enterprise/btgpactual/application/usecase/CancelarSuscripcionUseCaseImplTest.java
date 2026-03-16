package com.enterprise.btgpactual.application.usecase;

import com.enterprise.btgpactual.domain.exception.SuscripcionNoEncontradaException;
import com.enterprise.btgpactual.domain.model.*;
import com.enterprise.btgpactual.domain.port.out.ClienteRepositoryPort;
import com.enterprise.btgpactual.domain.port.out.FondoRepositoryPort;
import com.enterprise.btgpactual.domain.port.out.TransaccionRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("CancelarSuscripcionUseCase - Pruebas unitarias")
class CancelarSuscripcionUseCaseImplTest {

    @Mock private ClienteRepositoryPort clienteRepository;
    @Mock private FondoRepositoryPort fondoRepository;
    @Mock private TransaccionRepositoryPort transaccionRepository;

    @InjectMocks
    private CancelarSuscripcionUseCaseImpl cancelarSuscripcionUseCase;

    private Fondo fondo;

    @BeforeEach
    void setUp() {
        fondo = Fondo.builder()
                .id("1")
                .nombre("FPV_BTG_PACTUAL_RECAUDADORA")
                .montoMinimo(new BigDecimal("75000"))
                .categoria(CategoriaFondo.FPV)
                .build();
    }

    @Test
    @DisplayName("Debe crear transacción CANCELACION y devolver el monto al cliente")
    void debeCancelarSuscripcionExitosamente() {
        // given
        Set<String> fondosSuscritos = new HashSet<>();
        fondosSuscritos.add("1");
        Cliente clienteSuscrito = Cliente.builder()
                .id("cliente-001")
                .saldo(new BigDecimal("425000")) // ya descontado el monto del fondo
                .fondosSuscritos(fondosSuscritos)
                .build();

        given(clienteRepository.buscarPorId("cliente-001")).willReturn(Optional.of(clienteSuscrito));
        given(fondoRepository.buscarPorId("1")).willReturn(Optional.of(fondo));
        given(clienteRepository.guardar(any())).willAnswer(inv -> inv.getArgument(0));

        Transaccion transaccionGuardada = Transaccion.builder()
                .id("txn-002")
                .clienteId("cliente-001")
                .fondoId("1")
                .tipo(TipoTransaccion.CANCELACION)
                .monto(new BigDecimal("75000"))
                .fecha(LocalDateTime.now())
                .build();
        given(transaccionRepository.guardar(any())).willReturn(transaccionGuardada);

        // when
        Transaccion resultado = cancelarSuscripcionUseCase.ejecutar("cliente-001", "1");

        // then
        assertThat(resultado.getTipo()).isEqualTo(TipoTransaccion.CANCELACION);
        assertThat(resultado.getMonto()).isEqualByComparingTo(new BigDecimal("75000"));
        verify(clienteRepository).guardar(any());
    }

    @Test
    @DisplayName("Debe retornar el monto al saldo del cliente al cancelar")
    void debeRetornarMontoAlCancelar() {
        // given
        Set<String> fondosSuscritos = new HashSet<>();
        fondosSuscritos.add("1");
        Cliente clienteSuscrito = Cliente.builder()
                .id("cliente-001")
                .saldo(new BigDecimal("425000"))
                .fondosSuscritos(fondosSuscritos)
                .build();

        given(clienteRepository.buscarPorId("cliente-001")).willReturn(Optional.of(clienteSuscrito));
        given(fondoRepository.buscarPorId("1")).willReturn(Optional.of(fondo));
        given(clienteRepository.guardar(any())).willAnswer(inv -> {
            Cliente c = inv.getArgument(0);
            assertThat(c.getSaldo()).isEqualByComparingTo(new BigDecimal("500000")); // 425k + 75k devueltos
            return c;
        });
        given(transaccionRepository.guardar(any())).willAnswer(inv -> inv.getArgument(0));

        // when
        cancelarSuscripcionUseCase.ejecutar("cliente-001", "1");

        // then — verificado en el willAnswer
        verify(clienteRepository).guardar(any());
    }

    @Test
    @DisplayName("Debe lanzar SuscripcionNoEncontradaException cuando no tiene suscripción activa")
    void debeLanzarExcepcionCuandoNoTieneSuscripcion() {
        // given
        Cliente clienteSinSuscripcion = Cliente.builder()
                .id("cliente-001")
                .saldo(new BigDecimal("500000"))
                .fondosSuscritos(new HashSet<>())
                .build();

        given(clienteRepository.buscarPorId("cliente-001")).willReturn(Optional.of(clienteSinSuscripcion));
        given(fondoRepository.buscarPorId("1")).willReturn(Optional.of(fondo));

        // when & then
        assertThatThrownBy(() -> cancelarSuscripcionUseCase.ejecutar("cliente-001", "1"))
                .isInstanceOf(SuscripcionNoEncontradaException.class)
                .hasMessageContaining("FPV_BTG_PACTUAL_RECAUDADORA");
    }
}
