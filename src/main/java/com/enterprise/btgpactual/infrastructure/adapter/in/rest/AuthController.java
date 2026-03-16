package com.enterprise.btgpactual.infrastructure.adapter.in.rest;

import com.enterprise.btgpactual.domain.model.Cliente;
import com.enterprise.btgpactual.domain.port.in.RegistrarClienteUseCase;
import com.enterprise.btgpactual.domain.port.out.ClienteRepositoryPort;
import com.enterprise.btgpactual.infrastructure.security.JwtTokenProvider;
import com.enterprise.btgpactual.shared.dto.request.LoginRequest;
import com.enterprise.btgpactual.shared.dto.request.RegistroClienteRequest;
import com.enterprise.btgpactual.shared.dto.response.ApiResponse;
import com.enterprise.btgpactual.shared.dto.response.AuthResponse;
import com.enterprise.btgpactual.shared.dto.response.ClienteResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Registro e inicio de sesión de clientes")
public class AuthController {

    private final RegistrarClienteUseCase registrarClienteUseCase;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final ClienteRepositoryPort clienteRepository;

    @PostMapping("/registro")
    @Operation(summary = "Registrar nuevo cliente")
    public ResponseEntity<ApiResponse<AuthResponse>> registro(
            @Valid @RequestBody RegistroClienteRequest request) {

        Cliente cliente = registrarClienteUseCase.ejecutar(new RegistrarClienteUseCase.Command(
                request.getNombre(),
                request.getEmail(),
                request.getTelefono(),
                request.getPassword(),
                request.getPreferenciaNotificacion()
        ));

        String token = jwtTokenProvider.generarToken(cliente.getId());

        AuthResponse authResponse = AuthResponse.builder()
                .token(token)
                .clienteId(cliente.getId())
                .nombre(cliente.getNombre())
                .email(cliente.getEmail())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Cliente registrado exitosamente", authResponse));
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        // Spring Security valida email + password y retorna autenticación con clienteId como principal
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        String clienteId = authentication.getName();
        String token = jwtTokenProvider.generarToken(clienteId);

        // Recuperar datos del cliente para el response
        Cliente cliente = clienteRepository.buscarPorId(clienteId).orElseThrow();

        AuthResponse authResponse = AuthResponse.builder()
                .token(token)
                .clienteId(clienteId)
                .nombre(cliente.getNombre())
                .email(cliente.getEmail())
                .build();

        return ResponseEntity.ok(ApiResponse.ok("Login exitoso", authResponse));
    }

    @GetMapping("/me")
    @Operation(summary = "Obtener perfil del cliente autenticado")
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<ClienteResponse>> perfil(Authentication authentication) {
        String clienteId = authentication.getName();
        Cliente cliente = clienteRepository.buscarPorId(clienteId).orElseThrow();
        return ResponseEntity.ok(ApiResponse.ok(ClienteResponse.from(cliente)));
    }
}
