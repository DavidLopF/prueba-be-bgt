package com.enterprise.btgpactual.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaccion {

    private String id;
    private String clienteId;
    private String fondoId;
    private String fondoNombre;
    private TipoTransaccion tipo;
    private BigDecimal monto;
    private LocalDateTime fecha;
}
