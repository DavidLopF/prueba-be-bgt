package com.enterprise.btgpactual.shared.dto.response;

import com.enterprise.btgpactual.domain.model.CategoriaFondo;
import com.enterprise.btgpactual.domain.model.Fondo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FondoResponse {

    private String id;
    private String nombre;
    private BigDecimal montoMinimo;
    private CategoriaFondo categoria;

    public static FondoResponse from(Fondo fondo) {
        return FondoResponse.builder()
                .id(fondo.getId())
                .nombre(fondo.getNombre())
                .montoMinimo(fondo.getMontoMinimo())
                .categoria(fondo.getCategoria())
                .build();
    }
}
