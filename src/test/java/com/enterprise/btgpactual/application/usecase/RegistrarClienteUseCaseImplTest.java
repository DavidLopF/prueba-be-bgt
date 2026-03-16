package com.enterprise.btgpactual.application.usecase;

import com.enterprise.btgpactual.domain.exception.EmailYaRegistradoException;
import com.enterprise.btgpactual.domain.model.Cliente;
import com.enterprise.btgpactual.domain.model.PreferenciaNotificacion;
import com.enterprise.btgpactual.domain.port.in.RegistrarClienteUseCase;
import com.enterprise.btgpactual.domain.port.out.ClienteRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegistrarClienteUseCase - Pruebas unitarias")
class RegistrarClienteUseCaseImplTest {

    @Mock private ClienteRepositoryPort clienteRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RegistrarClienteUseCaseImpl registrarClienteUseCase;

    private final RegistrarClienteUseCase.Command command = new RegistrarClienteUseCase.Command(
            "Juan Pérez",
            "juan@btg.com",
            "3001234567",
            "password123",
            PreferenciaNotificacion.EMAIL
    );

    @Test
    @DisplayName("Debe registrar un cliente nuevo exitosamente")
    void debeRegistrarClienteExitosamente() {
        Cliente clienteEsperado = Cliente.builder()
                .id("cliente-001")
                .nombre("Juan Pérez")
                .email("juan@btg.com")
                .saldo(new BigDecimal("500000"))
                .preferenciaNotificacion(PreferenciaNotificacion.EMAIL)
                .fondosSuscritos(new HashSet<>())
                .build();

        given(clienteRepository.buscarPorEmail("juan@btg.com")).willReturn(Optional.empty());
        given(passwordEncoder.encode("password123")).willReturn("hashed-password");
        given(clienteRepository.registrar(any(Cliente.class), eq("hashed-password")))
                .willReturn(clienteEsperado);

        Cliente resultado = registrarClienteUseCase.ejecutar(command);

        assertThat(resultado.getId()).isEqualTo("cliente-001");
        assertThat(resultado.getNombre()).isEqualTo("Juan Pérez");
        assertThat(resultado.getEmail()).isEqualTo("juan@btg.com");
        verify(passwordEncoder).encode("password123");
        verify(clienteRepository).registrar(any(Cliente.class), eq("hashed-password"));
    }

    @Test
    @DisplayName("El cliente nuevo debe tener saldo inicial de 500.000")
    void clienteNuevoDebeIniciarConSaldo500000() {
        given(clienteRepository.buscarPorEmail("juan@btg.com")).willReturn(Optional.empty());
        given(passwordEncoder.encode(any())).willReturn("hashed");
        given(clienteRepository.registrar(any(Cliente.class), any())).willAnswer(inv -> inv.getArgument(0));

        Cliente resultado = registrarClienteUseCase.ejecutar(command);

        assertThat(resultado.getSaldo()).isEqualByComparingTo(new BigDecimal("500000"));
        assertThat(resultado.getFondosSuscritos()).isEmpty();
    }

    @Test
    @DisplayName("Debe lanzar EmailYaRegistradoException cuando el email ya existe")
    void debeLanzarExcepcionCuandoEmailYaRegistrado() {
        Cliente clienteExistente = Cliente.builder()
                .id("cliente-existente")
                .email("juan@btg.com")
                .build();

        given(clienteRepository.buscarPorEmail("juan@btg.com"))
                .willReturn(Optional.of(clienteExistente));

        assertThatThrownBy(() -> registrarClienteUseCase.ejecutar(command))
                .isInstanceOf(EmailYaRegistradoException.class);

        verify(clienteRepository, never()).registrar(any(), any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    @DisplayName("Debe guardar preferencia de notificación correctamente")
    void debeGuardarPreferenciaNotificacion() {
        RegistrarClienteUseCase.Command commandSms = new RegistrarClienteUseCase.Command(
                "Ana García", "ana@btg.com", "3009876543", "pass1234", PreferenciaNotificacion.SMS
        );

        given(clienteRepository.buscarPorEmail("ana@btg.com")).willReturn(Optional.empty());
        given(passwordEncoder.encode(any())).willReturn("hashed");
        given(clienteRepository.registrar(any(Cliente.class), any())).willAnswer(inv -> inv.getArgument(0));

        Cliente resultado = registrarClienteUseCase.ejecutar(commandSms);

        assertThat(resultado.getPreferenciaNotificacion()).isEqualTo(PreferenciaNotificacion.SMS);
    }
}
