package com.enterprise.btgpactual.application.usecase;

import com.enterprise.btgpactual.domain.exception.ClienteNoEncontradoException;
import com.enterprise.btgpactual.domain.exception.FondoNoEncontradoException;
import com.enterprise.btgpactual.domain.exception.SaldoInsuficienteException;
import com.enterprise.btgpactual.domain.exception.SuscripcionExistenteException;
import com.enterprise.btgpactual.domain.model.*;
import com.enterprise.btgpactual.domain.port.out.ClienteRepositoryPort;
import com.enterprise.btgpactual.domain.port.out.FondoRepositoryPort;
import com.enterprise.btgpactual.domain.port.out.NotificacionPort;
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
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("SuscribirFondoUseCase - Pruebas unitarias")
class SuscribirFondoUseCaseImplTest {

    @Mock private ClienteRepositoryPort clienteRepository;
    @Mock private FondoRepositoryPort fondoRepository;
    @Mock private TransaccionRepositoryPort transaccionRepository;
    @Mock private NotificacionPort notificacionPort;

    @InjectMocks
    private SuscribirFondoUseCaseImpl suscribirFondoUseCase;

    private Cliente clienteConSaldo;
    private Fondo fondo;

    @BeforeEach
    void setUp() {
        clienteConSaldo = Cliente.builder()
                .id("cliente-001")
                .nombre("Juan Pérez")
                .email("juan@btg.com")
                .saldo(new BigDecimal("500000"))
                .preferenciaNotificacion(PreferenciaNotificacion.EMAIL)
                .fondosSuscritos(new HashSet<>())
                .build();

        fondo = Fondo.builder()
                .id("1")
                .nombre("FPV_BTG_PACTUAL_RECAUDADORA")
                .montoMinimo(new BigDecimal("75000"))
                .categoria(CategoriaFondo.FPV)
                .build();
    }

    @Test
    @DisplayName("Debe crear transacción APERTURA cuando la suscripción es exitosa")
    void debeSuscribirExitosamente() {
        // given
        given(clienteRepository.buscarPorId("cliente-001")).willReturn(Optional.of(clienteConSaldo));
        given(fondoRepository.buscarPorId("1")).willReturn(Optional.of(fondo));
        given(clienteRepository.guardar(any())).willAnswer(inv -> inv.getArgument(0));

        Transaccion transaccionGuardada = Transaccion.builder()
                .id("txn-001")
                .clienteId("cliente-001")
                .fondoId("1")
                .fondoNombre("FPV_BTG_PACTUAL_RECAUDADORA")
                .tipo(TipoTransaccion.APERTURA)
                .monto(new BigDecimal("75000"))
                .fecha(LocalDateTime.now())
                .build();
        given(transaccionRepository.guardar(any())).willReturn(transaccionGuardada);

        // when
        Transaccion resultado = suscribirFondoUseCase.ejecutar("cliente-001", "1");

        // then
        assertThat(resultado.getTipo()).isEqualTo(TipoTransaccion.APERTURA);
        assertThat(resultado.getMonto()).isEqualByComparingTo(new BigDecimal("75000"));
        assertThat(resultado.getFondoNombre()).isEqualTo("FPV_BTG_PACTUAL_RECAUDADORA");
        verify(notificacionPort).notificarSuscripcion(any(), any());
    }

    @Test
    @DisplayName("Debe descontar el monto del saldo del cliente al suscribirse")
    void debeDescontarSaldoAlSuscribirse() {
        // given
        given(clienteRepository.buscarPorId("cliente-001")).willReturn(Optional.of(clienteConSaldo));
        given(fondoRepository.buscarPorId("1")).willReturn(Optional.of(fondo));

        Cliente clienteCapturado = null;
        given(clienteRepository.guardar(any())).willAnswer(inv -> {
            Cliente c = inv.getArgument(0);
            assertThat(c.getSaldo()).isEqualByComparingTo(new BigDecimal("425000")); // 500k - 75k
            return c;
        });
        given(transaccionRepository.guardar(any())).willAnswer(inv -> inv.getArgument(0));

        // when
        suscribirFondoUseCase.ejecutar("cliente-001", "1");

        // then
        verify(clienteRepository).guardar(any());
    }

    @Test
    @DisplayName("Debe lanzar SaldoInsuficienteException cuando el saldo es menor al monto mínimo")
    void debeLanzarExcepcionCuandoSaldoInsuficiente() {
        // given
        Cliente clienteSinSaldo = Cliente.builder()
                .id("cliente-001")
                .saldo(new BigDecimal("10000"))
                .fondosSuscritos(new HashSet<>())
                .build();

        given(clienteRepository.buscarPorId("cliente-001")).willReturn(Optional.of(clienteSinSaldo));
        given(fondoRepository.buscarPorId("1")).willReturn(Optional.of(fondo));

        // when & then
        assertThatThrownBy(() -> suscribirFondoUseCase.ejecutar("cliente-001", "1"))
                .isInstanceOf(SaldoInsuficienteException.class)
                .hasMessageContaining("FPV_BTG_PACTUAL_RECAUDADORA");

        verify(transaccionRepository, never()).guardar(any());
        verify(notificacionPort, never()).notificarSuscripcion(any(), any());
    }

    @Test
    @DisplayName("Debe lanzar SuscripcionExistenteException cuando ya está suscrito al fondo")
    void debeLanzarExcepcionCuandoYaEstaSuscrito() {
        // given
        Set<String> fondosSuscritos = new HashSet<>();
        fondosSuscritos.add("1");
        Cliente clienteYaSuscrito = Cliente.builder()
                .id("cliente-001")
                .saldo(new BigDecimal("500000"))
                .fondosSuscritos(fondosSuscritos)
                .build();

        given(clienteRepository.buscarPorId("cliente-001")).willReturn(Optional.of(clienteYaSuscrito));
        given(fondoRepository.buscarPorId("1")).willReturn(Optional.of(fondo));

        // when & then
        assertThatThrownBy(() -> suscribirFondoUseCase.ejecutar("cliente-001", "1"))
                .isInstanceOf(SuscripcionExistenteException.class);
    }

    @Test
    @DisplayName("Debe lanzar ClienteNoEncontradoException cuando el cliente no existe")
    void debeLanzarExcepcionCuandoClienteNoExiste() {
        given(clienteRepository.buscarPorId("cliente-999")).willReturn(Optional.empty());

        assertThatThrownBy(() -> suscribirFondoUseCase.ejecutar("cliente-999", "1"))
                .isInstanceOf(ClienteNoEncontradoException.class)
                .hasMessageContaining("cliente-999");
    }

    @Test
    @DisplayName("Debe lanzar FondoNoEncontradoException cuando el fondo no existe")
    void debeLanzarExcepcionCuandoFondoNoExiste() {
        given(clienteRepository.buscarPorId("cliente-001")).willReturn(Optional.of(clienteConSaldo));
        given(fondoRepository.buscarPorId("fondo-999")).willReturn(Optional.empty());

        assertThatThrownBy(() -> suscribirFondoUseCase.ejecutar("cliente-001", "fondo-999"))
                .isInstanceOf(FondoNoEncontradoException.class)
                .hasMessageContaining("fondo-999");
    }
}
