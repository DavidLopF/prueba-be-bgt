package com.enterprise.btgpactual.infrastructure.adapter.in.rest;

import com.enterprise.btgpactual.domain.model.Fondo;
import com.enterprise.btgpactual.domain.model.Transaccion;
import com.enterprise.btgpactual.domain.port.in.CancelarSuscripcionUseCase;
import com.enterprise.btgpactual.domain.port.in.ListarFondosUseCase;
import com.enterprise.btgpactual.domain.port.in.ObtenerHistorialUseCase;
import com.enterprise.btgpactual.domain.port.in.SuscribirFondoUseCase;
import com.enterprise.btgpactual.shared.dto.response.ApiResponse;
import com.enterprise.btgpactual.shared.dto.response.FondoResponse;
import com.enterprise.btgpactual.shared.dto.response.TransaccionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/fondos")
@RequiredArgsConstructor
@Tag(name = "Fondos", description = "Gestión de fondos de inversión")
@SecurityRequirement(name = "Bearer Authentication")
public class FondoController {

    private final SuscribirFondoUseCase suscribirFondoUseCase;
    private final CancelarSuscripcionUseCase cancelarSuscripcionUseCase;
    private final ObtenerHistorialUseCase obtenerHistorialUseCase;
    private final ListarFondosUseCase listarFondosUseCase;

    @GetMapping
    @Operation(summary = "Listar todos los fondos disponibles")
    public ResponseEntity<ApiResponse<List<FondoResponse>>> listarFondos() {
        List<FondoResponse> fondos = listarFondosUseCase.ejecutar()
                .stream()
                .map(FondoResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(fondos));
    }

    @PostMapping("/{fondoId}/suscribir")
    @Operation(summary = "Suscribirse a un fondo (apertura)")
    public ResponseEntity<ApiResponse<TransaccionResponse>> suscribir(
            @PathVariable String fondoId,
            Authentication authentication) {
        String clienteId = authentication.getName();
        Transaccion transaccion = suscribirFondoUseCase.ejecutar(clienteId, fondoId);
        return ResponseEntity.ok(
                ApiResponse.ok("Suscripción exitosa al fondo", TransaccionResponse.from(transaccion)));
    }

    @DeleteMapping("/{fondoId}/cancelar")
    @Operation(summary = "Cancelar suscripción a un fondo")
    public ResponseEntity<ApiResponse<TransaccionResponse>> cancelar(
            @PathVariable String fondoId,
            Authentication authentication) {
        String clienteId = authentication.getName();
        Transaccion transaccion = cancelarSuscripcionUseCase.ejecutar(clienteId, fondoId);
        return ResponseEntity.ok(
                ApiResponse.ok("Suscripción cancelada exitosamente", TransaccionResponse.from(transaccion)));
    }

    @GetMapping("/historial")
    @Operation(summary = "Ver historial de transacciones")
    public ResponseEntity<ApiResponse<List<TransaccionResponse>>> historial(
            Authentication authentication) {
        String clienteId = authentication.getName();
        List<TransaccionResponse> historial = obtenerHistorialUseCase.ejecutar(clienteId)
                .stream()
                .map(TransaccionResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(historial));
    }
}
