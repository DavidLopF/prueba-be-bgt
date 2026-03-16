package com.enterprise.btgpactual.infrastructure.adapter.in.rest;

import com.enterprise.btgpactual.config.TestSecurityConfig;
import com.enterprise.btgpactual.domain.exception.*;
import com.enterprise.btgpactual.domain.model.CategoriaFondo;
import com.enterprise.btgpactual.domain.model.Fondo;
import com.enterprise.btgpactual.domain.model.TipoTransaccion;
import com.enterprise.btgpactual.domain.model.Transaccion;
import com.enterprise.btgpactual.domain.port.in.CancelarSuscripcionUseCase;
import com.enterprise.btgpactual.domain.port.in.ListarFondosUseCase;
import com.enterprise.btgpactual.domain.port.in.ObtenerHistorialUseCase;
import com.enterprise.btgpactual.domain.port.in.SuscribirFondoUseCase;
import com.enterprise.btgpactual.infrastructure.security.JwtTokenProvider;
import com.enterprise.btgpactual.infrastructure.security.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = FondoController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)
)
@Import(TestSecurityConfig.class)
@DisplayName("FondoController - Pruebas unitarias")
class FondoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private JwtTokenProvider jwtTokenProvider;

    @MockBean private SuscribirFondoUseCase suscribirFondoUseCase;
    @MockBean private CancelarSuscripcionUseCase cancelarSuscripcionUseCase;
    @MockBean private ObtenerHistorialUseCase obtenerHistorialUseCase;
    @MockBean private ListarFondosUseCase listarFondosUseCase;

    // ── Fixtures ──────────────────────────────────────────────────────────────

    private Fondo fondoFPV() {
        return Fondo.builder()
                .id("1").nombre("FPV_BTG_PACTUAL_RECAUDADORA")
                .montoMinimo(new BigDecimal("75000")).categoria(CategoriaFondo.FPV)
                .build();
    }

    private Transaccion transaccionApertura() {
        return Transaccion.builder()
                .id("txn-001").clienteId("cliente-001").fondoId("1")
                .fondoNombre("FPV_BTG_PACTUAL_RECAUDADORA")
                .tipo(TipoTransaccion.APERTURA).monto(new BigDecimal("75000"))
                .fecha(LocalDateTime.of(2024, 1, 15, 10, 0))
                .build();
    }

    // ── GET /fondos ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /fondos debe retornar 200 con la lista de fondos")
    void listarFondos_retorna200ConListaDeFondos() throws Exception {
        given(listarFondosUseCase.ejecutar()).willReturn(List.of(fondoFPV()));

        mockMvc.perform(get("/api/v1/fondos").with(user("cliente-001")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value("1"))
                .andExpect(jsonPath("$.data[0].nombre").value("FPV_BTG_PACTUAL_RECAUDADORA"))
                .andExpect(jsonPath("$.data[0].montoMinimo").value(75000))
                .andExpect(jsonPath("$.data[0].categoria").value("FPV"));
    }

    @Test
    @DisplayName("GET /fondos debe retornar 200 con lista vacía cuando no hay fondos")
    void listarFondos_retorna200ConListaVacia() throws Exception {
        given(listarFondosUseCase.ejecutar()).willReturn(List.of());

        mockMvc.perform(get("/api/v1/fondos").with(user("cliente-001")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    // ── POST /fondos/{fondoId}/suscribir ──────────────────────────────────────

    @Test
    @DisplayName("POST /fondos/{id}/suscribir debe retornar 200 con la transacción de apertura")
    void suscribir_retorna200ConTransaccionApertura() throws Exception {
        given(suscribirFondoUseCase.ejecutar("cliente-001", "1")).willReturn(transaccionApertura());

        mockMvc.perform(post("/api/v1/fondos/1/suscribir").with(user("cliente-001")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Suscripción exitosa al fondo"))
                .andExpect(jsonPath("$.data.id").value("txn-001"))
                .andExpect(jsonPath("$.data.tipo").value("APERTURA"))
                .andExpect(jsonPath("$.data.fondoNombre").value("FPV_BTG_PACTUAL_RECAUDADORA"));
    }

    @Test
    @DisplayName("POST /fondos/{id}/suscribir debe retornar 400 cuando el saldo es insuficiente")
    void suscribir_retorna400CuandoSaldoInsuficiente() throws Exception {
        given(suscribirFondoUseCase.ejecutar("cliente-001", "1"))
                .willThrow(new SaldoInsuficienteException("FPV_RECAUDADORA"));

        mockMvc.perform(post("/api/v1/fondos/1/suscribir").with(user("cliente-001")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /fondos/{id}/suscribir debe retornar 400 cuando ya está suscrito")
    void suscribir_retorna400CuandoSuscripcionExistente() throws Exception {
        given(suscribirFondoUseCase.ejecutar("cliente-001", "1"))
                .willThrow(new SuscripcionExistenteException("FPV_RECAUDADORA"));

        mockMvc.perform(post("/api/v1/fondos/1/suscribir").with(user("cliente-001")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /fondos/{id}/suscribir debe retornar 404 cuando el cliente no existe")
    void suscribir_retorna404CuandoClienteNoEncontrado() throws Exception {
        given(suscribirFondoUseCase.ejecutar("cliente-001", "1"))
                .willThrow(new ClienteNoEncontradoException("cliente-001"));

        mockMvc.perform(post("/api/v1/fondos/1/suscribir").with(user("cliente-001")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /fondos/{id}/suscribir debe retornar 404 cuando el fondo no existe")
    void suscribir_retorna404CuandoFondoNoEncontrado() throws Exception {
        given(suscribirFondoUseCase.ejecutar("cliente-001", "1"))
                .willThrow(new FondoNoEncontradoException("1"));

        mockMvc.perform(post("/api/v1/fondos/1/suscribir").with(user("cliente-001")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ── DELETE /fondos/{fondoId}/cancelar ─────────────────────────────────────

    @Test
    @DisplayName("DELETE /fondos/{id}/cancelar debe retornar 200 con la transacción de cancelación")
    void cancelar_retorna200ConTransaccionCancelacion() throws Exception {
        Transaccion cancelacion = Transaccion.builder()
                .id("txn-002").clienteId("cliente-001").fondoId("1")
                .fondoNombre("FPV_BTG_PACTUAL_RECAUDADORA")
                .tipo(TipoTransaccion.CANCELACION).monto(new BigDecimal("75000"))
                .fecha(LocalDateTime.of(2024, 1, 16, 10, 0))
                .build();

        given(cancelarSuscripcionUseCase.ejecutar("cliente-001", "1")).willReturn(cancelacion);

        mockMvc.perform(delete("/api/v1/fondos/1/cancelar").with(user("cliente-001")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Suscripción cancelada exitosamente"))
                .andExpect(jsonPath("$.data.tipo").value("CANCELACION"));
    }

    @Test
    @DisplayName("DELETE /fondos/{id}/cancelar debe retornar 400 cuando no tiene suscripción")
    void cancelar_retorna400CuandoSuscripcionNoEncontrada() throws Exception {
        given(cancelarSuscripcionUseCase.ejecutar("cliente-001", "1"))
                .willThrow(new SuscripcionNoEncontradaException("FPV_RECAUDADORA"));

        mockMvc.perform(delete("/api/v1/fondos/1/cancelar").with(user("cliente-001")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ── GET /fondos/historial ─────────────────────────────────────────────────

    @Test
    @DisplayName("GET /fondos/historial debe retornar 200 con el historial de transacciones")
    void historial_retorna200ConListaDeTransacciones() throws Exception {
        given(obtenerHistorialUseCase.ejecutar("cliente-001")).willReturn(List.of(transaccionApertura()));

        mockMvc.perform(get("/api/v1/fondos/historial").with(user("cliente-001")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].tipo").value("APERTURA"))
                .andExpect(jsonPath("$.data[0].fondoNombre").value("FPV_BTG_PACTUAL_RECAUDADORA"));
    }

    @Test
    @DisplayName("GET /fondos/historial debe retornar 200 con lista vacía si no hay transacciones")
    void historial_retorna200ConListaVaciaSiNoHayTransacciones() throws Exception {
        given(obtenerHistorialUseCase.ejecutar("cliente-001")).willReturn(List.of());

        mockMvc.perform(get("/api/v1/fondos/historial").with(user("cliente-001")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("GET /fondos/historial debe retornar 404 cuando el cliente no existe")
    void historial_retorna404CuandoClienteNoEncontrado() throws Exception {
        given(obtenerHistorialUseCase.ejecutar("cliente-001"))
                .willThrow(new ClienteNoEncontradoException("cliente-001"));

        mockMvc.perform(get("/api/v1/fondos/historial").with(user("cliente-001")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }
}
