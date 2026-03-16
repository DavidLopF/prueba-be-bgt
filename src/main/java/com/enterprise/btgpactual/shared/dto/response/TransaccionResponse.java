package com.enterprise.btgpactual.shared.dto.response;

import com.enterprise.btgpactual.domain.model.TipoTransaccion;
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
public class TransaccionResponse {

    private String id;
    private String fondoId;
    private String fondoNombre;
    private TipoTransaccion tipo;
    private BigDecimal monto;
    private LocalDateTime fecha;

    public static TransaccionResponse from(com.enterprise.btgpactual.domain.model.Transaccion transaccion) {
        return TransaccionResponse.builder()
                .id(transaccion.getId())
                .fondoId(transaccion.getFondoId())
                .fondoNombre(transaccion.getFondoNombre())
                .tipo(transaccion.getTipo())
                .monto(transaccion.getMonto())
                .fecha(transaccion.getFecha())
                .build();
    }
}
