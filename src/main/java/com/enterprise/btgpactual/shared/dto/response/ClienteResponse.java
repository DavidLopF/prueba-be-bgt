package com.enterprise.btgpactual.shared.dto.response;

import com.enterprise.btgpactual.domain.model.Cliente;
import com.enterprise.btgpactual.domain.model.PreferenciaNotificacion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Set;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteResponse {

    private String id;
    private String nombre;
    private String email;
    private BigDecimal saldo;
    private Set<String> fondosSuscritos;
    private PreferenciaNotificacion preferenciaNotificacion;

    public static ClienteResponse from(Cliente cliente) {
        return ClienteResponse.builder()
                .id(cliente.getId())
                .nombre(cliente.getNombre())
                .email(cliente.getEmail())
                .saldo(cliente.getSaldo())
                .fondosSuscritos(cliente.getFondosSuscritos())
                .preferenciaNotificacion(cliente.getPreferenciaNotificacion())
                .build();
    }
}
