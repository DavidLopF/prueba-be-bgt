package com.enterprise.btgpactual.infrastructure.adapter.notification;

import com.enterprise.btgpactual.domain.model.Cliente;
import com.enterprise.btgpactual.domain.model.Transaccion;
import com.enterprise.btgpactual.domain.port.out.NotificacionPort;
import lombok.extern.slf4j.Slf4j;

/**
 * @deprecated Superseded by NotificacionAdapter in infrastructure.adapter.out.notification
 * Note: @Component removed intentionally to prevent multiple NotificacionPort bean conflicts.
 */
@Slf4j
@Deprecated
public class EmailNotificacionAdapter implements NotificacionPort {

    @Override
    public void notificarSuscripcion(Cliente cliente, Transaccion transaccion) {
        log.info("[EMAIL] {} - {}", cliente.getEmail(), transaccion.getFondoNombre());
    }
}
