package com.enterprise.btgpactual.api.dto;

import com.enterprise.btgpactual.domain.model.TipoTransaccion;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class TransaccionResponseDTO {

    private String id;
    private String fondoId;
    private String fondoNombre;
    private TipoTransaccion tipo;
    private BigDecimal monto;
    private LocalDateTime fecha;
}
