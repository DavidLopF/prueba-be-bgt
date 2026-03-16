package com.enterprise.btgpactual.application.usecase;

import com.enterprise.btgpactual.domain.model.CategoriaFondo;
import com.enterprise.btgpactual.domain.model.Fondo;
import com.enterprise.btgpactual.domain.port.out.FondoRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ListarFondosUseCase - Pruebas unitarias")
class ListarFondosUseCaseImplTest {

    @Mock private FondoRepositoryPort fondoRepository;

    @InjectMocks
    private ListarFondosUseCaseImpl listarFondosUseCase;

    @Test
    @DisplayName("Debe retornar todos los fondos disponibles")
    void debeRetornarTodosLosFondos() {
        List<Fondo> fondosEsperados = List.of(
                Fondo.builder().id("1").nombre("FPV_BTG_PACTUAL_RECAUDADORA")
                        .montoMinimo(new BigDecimal("75000")).categoria(CategoriaFondo.FPV).build(),
                Fondo.builder().id("2").nombre("FPV_BTG_PACTUAL_ECOPETROL")
                        .montoMinimo(new BigDecimal("125000")).categoria(CategoriaFondo.FPV).build(),
                Fondo.builder().id("3").nombre("DEUDAPRIVADA")
                        .montoMinimo(new BigDecimal("50000")).categoria(CategoriaFondo.FIC).build()
        );

        given(fondoRepository.buscarTodos()).willReturn(fondosEsperados);

        List<Fondo> resultado = listarFondosUseCase.ejecutar();

        assertThat(resultado).hasSize(3);
        assertThat(resultado.get(0).getNombre()).isEqualTo("FPV_BTG_PACTUAL_RECAUDADORA");
        assertThat(resultado.get(2).getCategoria()).isEqualTo(CategoriaFondo.FIC);
        verify(fondoRepository).buscarTodos();
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay fondos registrados")
    void debeRetornarListaVaciaCuandoNoHayFondos() {
        given(fondoRepository.buscarTodos()).willReturn(List.of());

        List<Fondo> resultado = listarFondosUseCase.ejecutar();

        assertThat(resultado).isEmpty();
        verify(fondoRepository).buscarTodos();
    }

    @Test
    @DisplayName("Debe delegar directamente al repositorio sin lógica adicional")
    void debeDelegarAlRepositorio() {
        List<Fondo> fondos = List.of(
                Fondo.builder().id("1").nombre("FPV_RECAUDADORA")
                        .montoMinimo(new BigDecimal("75000")).categoria(CategoriaFondo.FPV).build()
        );
        given(fondoRepository.buscarTodos()).willReturn(fondos);

        List<Fondo> resultado = listarFondosUseCase.ejecutar();

        assertThat(resultado).isSameAs(fondos);
    }
}
