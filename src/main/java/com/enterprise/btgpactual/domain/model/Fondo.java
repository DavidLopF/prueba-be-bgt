package com.enterprise.btgpactual.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Fondo {

    private String id;
    private String nombre;
    private BigDecimal montoMinimo;
    private CategoriaFondo categoria;
}
