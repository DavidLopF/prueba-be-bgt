package com.enterprise.btgpactual.infrastructure.adapter.out.notification;

import com.enterprise.btgpactual.domain.model.Cliente;
import com.enterprise.btgpactual.domain.model.Transaccion;
import com.enterprise.btgpactual.domain.port.out.NotificacionPort;
import org.springframework.context.annotation.Primary;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Primary
@Component
public class NotificacionAdapter implements NotificacionPort {

    @Override
    public void notificarSuscripcion(Cliente cliente, Transaccion transaccion) {
        switch (cliente.getPreferenciaNotificacion()) {
            case EMAIL -> enviarEmail(cliente, transaccion);
            case SMS   -> enviarSms(cliente, transaccion);
        }
    }

    private void enviarEmail(Cliente cliente, Transaccion transaccion) {
        // Integración real: AWS SES
        log.info("[EMAIL] Para: {} | Fondo: {} | Monto: COP {} | Transacción: {}",
                cliente.getEmail(),
                transaccion.getFondoNombre(),
                transaccion.getMonto(),
                transaccion.getId());
    }

    private void enviarSms(Cliente cliente, Transaccion transaccion) {
        // Integración real: AWS SNS
        log.info("[SMS] Para: {} | Fondo: {} | Monto: COP {} | Transacción: {}",
                cliente.getTelefono(),
                transaccion.getFondoNombre(),
                transaccion.getMonto(),
                transaccion.getId());
    }
}
