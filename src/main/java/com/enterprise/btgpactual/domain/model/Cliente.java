package com.enterprise.btgpactual.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {

    private static final BigDecimal SALDO_INICIAL = new BigDecimal("500000");

    private String id;
    private String nombre;
    private String email;
    private String telefono;
    private BigDecimal saldo;
    private PreferenciaNotificacion preferenciaNotificacion;

    @Builder.Default
    private Set<String> fondosSuscritos = new HashSet<>();

    // ── Factory method ────────────────────────────────────────────────────────
    public static Cliente nuevo(String nombre, String email, String telefono,
                                PreferenciaNotificacion preferencia) {
        return Cliente.builder()
                .nombre(nombre)
                .email(email)
                .telefono(telefono)
                .saldo(SALDO_INICIAL)
                .preferenciaNotificacion(preferencia)
                .fondosSuscritos(new HashSet<>())
                .build();
    }

    // ── Comportamiento de dominio ─────────────────────────────────────────────
    public boolean tieneSaldoSuficiente(BigDecimal montoRequerido) {
        return this.saldo.compareTo(montoRequerido) >= 0;
    }

    public boolean estaSuscritoA(String fondoId) {
        return this.fondosSuscritos.contains(fondoId);
    }

    public Cliente suscribirFondo(String fondoId, BigDecimal monto) {
        Set<String> nuevosFondos = new HashSet<>(this.fondosSuscritos);
        nuevosFondos.add(fondoId);
        return toBuilder()
                .saldo(this.saldo.subtract(monto))
                .fondosSuscritos(nuevosFondos)
                .build();
    }

    public Cliente cancelarFondo(String fondoId, BigDecimal monto) {
        Set<String> nuevosFondos = new HashSet<>(this.fondosSuscritos);
        nuevosFondos.remove(fondoId);
        return toBuilder()
                .saldo(this.saldo.add(monto))
                .fondosSuscritos(nuevosFondos)
                .build();
    }

    public ClienteBuilder toBuilder() {
        return Cliente.builder()
                .id(this.id)
                .nombre(this.nombre)
                .email(this.email)
                .telefono(this.telefono)
                .saldo(this.saldo)
                .preferenciaNotificacion(this.preferenciaNotificacion)
                .fondosSuscritos(new HashSet<>(this.fondosSuscritos));
    }
}
