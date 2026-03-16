package com.enterprise.btgpactual.infrastructure.adapter.in.rest;

import com.enterprise.btgpactual.config.TestSecurityConfig;
import com.enterprise.btgpactual.domain.exception.EmailYaRegistradoException;
import com.enterprise.btgpactual.domain.model.Cliente;
import com.enterprise.btgpactual.domain.model.PreferenciaNotificacion;
import com.enterprise.btgpactual.domain.port.in.RegistrarClienteUseCase;
import com.enterprise.btgpactual.domain.port.out.ClienteRepositoryPort;
import com.enterprise.btgpactual.infrastructure.security.JwtTokenProvider;
import com.enterprise.btgpactual.infrastructure.security.SecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = AuthController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)
)
@Import(TestSecurityConfig.class)
@DisplayName("AuthController - Pruebas unitarias")
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private RegistrarClienteUseCase registrarClienteUseCase;
    @MockBean private AuthenticationManager authenticationManager;
    @MockBean private JwtTokenProvider jwtTokenProvider;
    @MockBean private ClienteRepositoryPort clienteRepository;

    // ── Fixtures ──────────────────────────────────────────────────────────────

    private Cliente clienteRegistrado() {
        return Cliente.builder()
                .id("cliente-001").nombre("Juan Pérez").email("juan@btg.com")
                .telefono("3001234567").saldo(new BigDecimal("500000"))
                .preferenciaNotificacion(PreferenciaNotificacion.EMAIL)
                .fondosSuscritos(new HashSet<>())
                .build();
    }

    private Map<String, Object> bodyRegistroValido() {
        return Map.of(
                "nombre", "Juan Pérez",
                "email", "juan@btg.com",
                "telefono", "3001234567",
                "password", "password123",
                "preferenciaNotificacion", "EMAIL"
        );
    }

    // ── POST /auth/registro ───────────────────────────────────────────────────

    @Test
    @DisplayName("POST /auth/registro debe retornar 201 con token y datos del cliente")
    void registro_retorna201ConAuthResponse() throws Exception {
        given(registrarClienteUseCase.ejecutar(any())).willReturn(clienteRegistrado());
        given(jwtTokenProvider.generarToken("cliente-001")).willReturn("jwt-token-generado");

        mockMvc.perform(post("/api/v1/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bodyRegistroValido())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Cliente registrado exitosamente"))
                .andExpect(jsonPath("$.data.token").value("jwt-token-generado"))
                .andExpect(jsonPath("$.data.clienteId").value("cliente-001"))
                .andExpect(jsonPath("$.data.nombre").value("Juan Pérez"))
                .andExpect(jsonPath("$.data.email").value("juan@btg.com"));
    }

    @Test
    @DisplayName("POST /auth/registro debe retornar 409 cuando el email ya está registrado")
    void registro_retorna409CuandoEmailYaRegistrado() throws Exception {
        given(registrarClienteUseCase.ejecutar(any()))
                .willThrow(new EmailYaRegistradoException("juan@btg.com"));

        mockMvc.perform(post("/api/v1/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bodyRegistroValido())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /auth/registro debe retornar 400 cuando faltan campos obligatorios")
    void registro_retorna400CuandoBodyInvalido() throws Exception {
        Map<String, Object> bodyInvalido = Map.of("nombre", "Juan");

        mockMvc.perform(post("/api/v1/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bodyInvalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /auth/registro debe retornar 400 cuando el email tiene formato inválido")
    void registro_retorna400CuandoEmailInvalido() throws Exception {
        Map<String, Object> bodyInvalido = Map.of(
                "nombre", "Juan Pérez",
                "email", "no-es-un-email",
                "telefono", "3001234567",
                "password", "password123",
                "preferenciaNotificacion", "EMAIL"
        );

        mockMvc.perform(post("/api/v1/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bodyInvalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ── POST /auth/login ──────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /auth/login debe retornar 200 con token cuando las credenciales son correctas")
    void login_retorna200ConTokenCuandoCredencialesCorrectas() throws Exception {
        UsernamePasswordAuthenticationToken mockAuth = new UsernamePasswordAuthenticationToken(
                "cliente-001", null, List.of(new SimpleGrantedAuthority("ROLE_CLIENT")));

        given(authenticationManager.authenticate(any())).willReturn(mockAuth);
        given(jwtTokenProvider.generarToken("cliente-001")).willReturn("jwt-token-login");
        given(clienteRepository.buscarPorId("cliente-001")).willReturn(Optional.of(clienteRegistrado()));

        Map<String, String> bodyLogin = Map.of("email", "juan@btg.com", "password", "password123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bodyLogin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login exitoso"))
                .andExpect(jsonPath("$.data.token").value("jwt-token-login"))
                .andExpect(jsonPath("$.data.clienteId").value("cliente-001"));
    }

    @Test
    @DisplayName("POST /auth/login debe retornar 401 cuando las credenciales son incorrectas")
    void login_retorna401CuandoCredencialesIncorrectas() throws Exception {
        given(authenticationManager.authenticate(any()))
                .willThrow(new BadCredentialsException("Bad credentials"));

        Map<String, String> bodyLogin = Map.of("email", "juan@btg.com", "password", "wrong");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bodyLogin)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Email o contraseña incorrectos"));
    }

    @Test
    @DisplayName("POST /auth/login debe retornar 400 cuando el body está vacío")
    void login_retorna400CuandoBodyVacio() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ── GET /auth/me ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /auth/me debe retornar 200 con el perfil del cliente autenticado")
    void perfil_retorna200ConDatosDelCliente() throws Exception {
        given(clienteRepository.buscarPorId("cliente-001")).willReturn(Optional.of(clienteRegistrado()));

        mockMvc.perform(get("/api/v1/auth/me").with(user("cliente-001")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("cliente-001"))
                .andExpect(jsonPath("$.data.nombre").value("Juan Pérez"))
                .andExpect(jsonPath("$.data.email").value("juan@btg.com"))
                .andExpect(jsonPath("$.data.saldo").value(500000));
    }
}
