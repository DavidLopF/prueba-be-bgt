package com.enterprise.btgpactual.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Cliente - Pruebas del modelo de dominio")
class ClienteTest {

    private Cliente cliente;

    @BeforeEach
    void setUp() {
        cliente = Cliente.builder()
                .id("cliente-001")
                .nombre("Juan Pérez")
                .email("juan@btg.com")
                .telefono("3001234567")
                .saldo(new BigDecimal("500000"))
                .preferenciaNotificacion(PreferenciaNotificacion.EMAIL)
                .fondosSuscritos(new HashSet<>())
                .build();
    }

    // ── Factory method ────────────────────────────────────────────────────────

    @Test
    @DisplayName("nuevo() debe crear un cliente con saldo inicial de 500.000")
    void nuevoDebeCrearClienteConSaldoInicial() {
        Cliente nuevo = Cliente.nuevo("Ana García", "ana@btg.com", "3009876543", PreferenciaNotificacion.SMS);

        assertThat(nuevo.getSaldo()).isEqualByComparingTo(new BigDecimal("500000"));
        assertThat(nuevo.getNombre()).isEqualTo("Ana García");
        assertThat(nuevo.getEmail()).isEqualTo("ana@btg.com");
        assertThat(nuevo.getTelefono()).isEqualTo("3009876543");
        assertThat(nuevo.getPreferenciaNotificacion()).isEqualTo(PreferenciaNotificacion.SMS);
    }

    @Test
    @DisplayName("nuevo() debe crear un cliente con fondos suscritos vacíos")
    void nuevoDebeCrearClienteConFondosVacios() {
        Cliente nuevo = Cliente.nuevo("Ana García", "ana@btg.com", "3009876543", PreferenciaNotificacion.EMAIL);

        assertThat(nuevo.getFondosSuscritos()).isEmpty();
        assertThat(nuevo.getId()).isNull();
    }

    // ── tieneSaldoSuficiente ──────────────────────────────────────────────────

    @Test
    @DisplayName("tieneSaldoSuficiente() debe retornar true cuando saldo es mayor al requerido")
    void tieneSaldoSuficienteCuandoSaldoEsMayor() {
        assertThat(cliente.tieneSaldoSuficiente(new BigDecimal("75000"))).isTrue();
    }

    @Test
    @DisplayName("tieneSaldoSuficiente() debe retornar true cuando saldo es exactamente igual al requerido")
    void tieneSaldoSuficienteCuandoSaldoEsIgual() {
        assertThat(cliente.tieneSaldoSuficiente(new BigDecimal("500000"))).isTrue();
    }

    @Test
    @DisplayName("tieneSaldoSuficiente() debe retornar false cuando saldo es menor al requerido")
    void noTieneSaldoSuficienteCuandoSaldoEsMenor() {
        assertThat(cliente.tieneSaldoSuficiente(new BigDecimal("500001"))).isFalse();
    }

    // ── estaSuscritoA ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("estaSuscritoA() debe retornar false cuando no tiene ninguna suscripción")
    void noEstaSuscritoCuandoFondosVacios() {
        assertThat(cliente.estaSuscritoA("fondo-1")).isFalse();
    }

    @Test
    @DisplayName("estaSuscritoA() debe retornar true cuando ya está suscrito al fondo")
    void estaSuscritoCuandoTieneFondoSuscrito() {
        Set<String> fondos = new HashSet<>();
        fondos.add("fondo-1");
        Cliente clienteSuscrito = Cliente.builder()
                .id("cliente-001")
                .saldo(new BigDecimal("425000"))
                .fondosSuscritos(fondos)
                .build();

        assertThat(clienteSuscrito.estaSuscritoA("fondo-1")).isTrue();
        assertThat(clienteSuscrito.estaSuscritoA("fondo-2")).isFalse();
    }

    // ── suscribirFondo ────────────────────────────────────────────────────────

    @Test
    @DisplayName("suscribirFondo() debe descontar el monto del saldo")
    void suscribirFondoDebeDescontarMonto() {
        Cliente actualizado = cliente.suscribirFondo("fondo-1", new BigDecimal("75000"));

        assertThat(actualizado.getSaldo()).isEqualByComparingTo(new BigDecimal("425000"));
    }

    @Test
    @DisplayName("suscribirFondo() debe agregar el fondoId a los fondos suscritos")
    void suscribirFondoDebeAgregarFondoId() {
        Cliente actualizado = cliente.suscribirFondo("fondo-1", new BigDecimal("75000"));

        assertThat(actualizado.getFondosSuscritos()).containsExactly("fondo-1");
    }

    @Test
    @DisplayName("suscribirFondo() no debe modificar el cliente original (inmutabilidad)")
    void suscribirFondoNoDebeModificarOriginal() {
        cliente.suscribirFondo("fondo-1", new BigDecimal("75000"));

        assertThat(cliente.getSaldo()).isEqualByComparingTo(new BigDecimal("500000"));
        assertThat(cliente.getFondosSuscritos()).isEmpty();
    }

    @Test
    @DisplayName("suscribirFondo() debe mantener los demás datos del cliente")
    void suscribirFondoDebeMantenerDatos() {
        Cliente actualizado = cliente.suscribirFondo("fondo-1", new BigDecimal("75000"));

        assertThat(actualizado.getId()).isEqualTo(cliente.getId());
        assertThat(actualizado.getNombre()).isEqualTo(cliente.getNombre());
        assertThat(actualizado.getEmail()).isEqualTo(cliente.getEmail());
    }

    // ── cancelarFondo ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("cancelarFondo() debe devolver el monto al saldo")
    void cancelarFondoDebeRetornarMonto() {
        Set<String> fondos = new HashSet<>();
        fondos.add("fondo-1");
        Cliente clienteSuscrito = Cliente.builder()
                .id("cliente-001")
                .saldo(new BigDecimal("425000"))
                .fondosSuscritos(fondos)
                .build();

        Cliente actualizado = clienteSuscrito.cancelarFondo("fondo-1", new BigDecimal("75000"));

        assertThat(actualizado.getSaldo()).isEqualByComparingTo(new BigDecimal("500000"));
    }

    @Test
    @DisplayName("cancelarFondo() debe eliminar el fondoId de los fondos suscritos")
    void cancelarFondoDebeEliminarFondoId() {
        Set<String> fondos = new HashSet<>();
        fondos.add("fondo-1");
        fondos.add("fondo-2");
        Cliente clienteSuscrito = Cliente.builder()
                .id("cliente-001")
                .saldo(new BigDecimal("250000"))
                .fondosSuscritos(fondos)
                .build();

        Cliente actualizado = clienteSuscrito.cancelarFondo("fondo-1", new BigDecimal("75000"));

        assertThat(actualizado.getFondosSuscritos()).containsExactly("fondo-2");
        assertThat(actualizado.getFondosSuscritos()).doesNotContain("fondo-1");
    }

    @Test
    @DisplayName("cancelarFondo() no debe modificar el cliente original (inmutabilidad)")
    void cancelarFondoNoDebeModificarOriginal() {
        Set<String> fondos = new HashSet<>();
        fondos.add("fondo-1");
        Cliente clienteSuscrito = Cliente.builder()
                .id("cliente-001")
                .saldo(new BigDecimal("425000"))
                .fondosSuscritos(fondos)
                .build();

        clienteSuscrito.cancelarFondo("fondo-1", new BigDecimal("75000"));

        assertThat(clienteSuscrito.getSaldo()).isEqualByComparingTo(new BigDecimal("425000"));
        assertThat(clienteSuscrito.getFondosSuscritos()).contains("fondo-1");
    }
}
