package com.enterprise.btgpactual.domain.port.out;

import com.enterprise.btgpactual.domain.model.Cliente;
import com.enterprise.btgpactual.domain.model.Transaccion;

public interface NotificacionPort {

    void notificarSuscripcion(Cliente cliente, Transaccion transaccion);
}
