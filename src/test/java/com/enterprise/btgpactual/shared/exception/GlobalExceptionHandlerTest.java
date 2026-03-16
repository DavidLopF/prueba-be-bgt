package com.enterprise.btgpactual.shared.exception;

import com.enterprise.btgpactual.domain.exception.*;
import com.enterprise.btgpactual.shared.dto.response.ApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GlobalExceptionHandler - Pruebas unitarias")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("SaldoInsuficienteException debe retornar 400 con el mensaje de la excepción")
    void handleSaldoInsuficiente_retorna400() {
        SaldoInsuficienteException ex = new SaldoInsuficienteException("FPV_RECAUDADORA");

        ResponseEntity<ApiResponse<Void>> response = handler.handleSaldoInsuficiente(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).isEqualTo(ex.getMessage());
    }

    @Test
    @DisplayName("SuscripcionExistenteException debe retornar 400 con el mensaje de la excepción")
    void handleSuscripcionExistente_retorna400() {
        SuscripcionExistenteException ex = new SuscripcionExistenteException("FPV_ECOPETROL");

        ResponseEntity<ApiResponse<Void>> response = handler.handleSuscripcionExistente(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).isEqualTo(ex.getMessage());
    }

    @Test
    @DisplayName("SuscripcionNoEncontradaException debe retornar 400 con el mensaje de la excepción")
    void handleSuscripcionNoEncontrada_retorna400() {
        SuscripcionNoEncontradaException ex = new SuscripcionNoEncontradaException("FPV_DINAMICA");

        ResponseEntity<ApiResponse<Void>> response = handler.handleSuscripcionNoEncontrada(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).isEqualTo(ex.getMessage());
    }

    @Test
    @DisplayName("ClienteNoEncontradoException debe retornar 404")
    void handleClienteNoEncontrado_retorna404() {
        ClienteNoEncontradoException ex = new ClienteNoEncontradoException("cliente-999");

        ResponseEntity<ApiResponse<Void>> response = handler.handleClienteNoEncontrado(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).isEqualTo(ex.getMessage());
    }

    @Test
    @DisplayName("FondoNoEncontradoException debe retornar 404")
    void handleFondoNoEncontrado_retorna404() {
        FondoNoEncontradoException ex = new FondoNoEncontradoException("fondo-999");

        ResponseEntity<ApiResponse<Void>> response = handler.handleFondoNoEncontrado(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).isEqualTo(ex.getMessage());
    }

    @Test
    @DisplayName("EmailYaRegistradoException debe retornar 409")
    void handleEmailYaRegistrado_retorna409() {
        EmailYaRegistradoException ex = new EmailYaRegistradoException("juan@btg.com");

        ResponseEntity<ApiResponse<Void>> response = handler.handleEmailYaRegistrado(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).isEqualTo(ex.getMessage());
    }

    @Test
    @DisplayName("BadCredentialsException debe retornar 401 con mensaje genérico")
    void handleBadCredentials_retorna401() {
        BadCredentialsException ex = new BadCredentialsException("Bad credentials");

        ResponseEntity<ApiResponse<Void>> response = handler.handleBadCredentials(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).isEqualTo("Email o contraseña incorrectos");
    }

    @Test
    @DisplayName("Exception genérica debe retornar 500")
    void handleGeneral_retorna500() {
        Exception ex = new RuntimeException("Error inesperado");

        ResponseEntity<ApiResponse<Void>> response = handler.handleGeneral(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).isEqualTo("Error interno del servidor");
    }
}
