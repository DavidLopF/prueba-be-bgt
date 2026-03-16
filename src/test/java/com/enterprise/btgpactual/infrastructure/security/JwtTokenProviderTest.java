package com.enterprise.btgpactual.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtTokenProvider - Pruebas unitarias")
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    private static final String SECRET = "BTGPactual-Fondos-JWT-Secret-Key-2024!!";
    private static final long EXPIRATION = 86400000L; // 24 horas

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", SECRET);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpiration", EXPIRATION);
    }

    @Test
    @DisplayName("generarToken() debe generar un token no nulo y no vacío")
    void generarTokenDebeRetornarTokenValido() {
        String token = jwtTokenProvider.generarToken("cliente-001");

        assertThat(token).isNotNull();
        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3); // formato JWT: header.payload.signature
    }

    @Test
    @DisplayName("extraerClienteId() debe extraer el clienteId correcto del token")
    void extraerClienteIdDebeRetornarClienteIdCorrecto() {
        String token = jwtTokenProvider.generarToken("cliente-001");

        String clienteId = jwtTokenProvider.extraerClienteId(token);

        assertThat(clienteId).isEqualTo("cliente-001");
    }

    @Test
    @DisplayName("validarToken() debe retornar true para un token válido")
    void validarTokenDebeRetornarTrueParaTokenValido() {
        String token = jwtTokenProvider.generarToken("cliente-001");

        assertThat(jwtTokenProvider.validarToken(token)).isTrue();
    }

    @Test
    @DisplayName("validarToken() debe retornar false para un token inválido")
    void validarTokenDebeRetornarFalseParaTokenInvalido() {
        assertThat(jwtTokenProvider.validarToken("token.invalido.falso")).isFalse();
    }

    @Test
    @DisplayName("validarToken() debe retornar false para un token vacío")
    void validarTokenDebeRetornarFalseParaTokenVacio() {
        assertThat(jwtTokenProvider.validarToken("")).isFalse();
    }

    @Test
    @DisplayName("validarToken() debe retornar false para token firmado con otro secreto")
    void validarTokenDebeRetornarFalseParaTokenConOtroSecreto() {
        JwtTokenProvider otroProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(otroProvider, "jwtSecret", "otro-secreto-completamente-diferente!!");
        ReflectionTestUtils.setField(otroProvider, "jwtExpiration", EXPIRATION);

        String tokenDeOtroProvider = otroProvider.generarToken("cliente-001");

        assertThat(jwtTokenProvider.validarToken(tokenDeOtroProvider)).isFalse();
    }

    @Test
    @DisplayName("Ciclo completo: generar y extraer clienteId coinciden")
    void cicloCompletoGenerarYExtraer() {
        String clienteIdOriginal = "cliente-xyz-123";
        String token = jwtTokenProvider.generarToken(clienteIdOriginal);
        String clienteIdExtraido = jwtTokenProvider.extraerClienteId(token);

        assertThat(clienteIdExtraido).isEqualTo(clienteIdOriginal);
    }

    @Test
    @DisplayName("Tokens generados para distintos clientes deben ser distintos")
    void tokensDiferentesParaDiferentesClientes() {
        String token1 = jwtTokenProvider.generarToken("cliente-001");
        String token2 = jwtTokenProvider.generarToken("cliente-002");

        assertThat(token1).isNotEqualTo(token2);
    }
}
