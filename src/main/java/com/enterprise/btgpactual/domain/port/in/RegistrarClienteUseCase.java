package com.enterprise.btgpactual.domain.port.in;

import com.enterprise.btgpactual.domain.model.Cliente;
import com.enterprise.btgpactual.domain.model.PreferenciaNotificacion;

public interface RegistrarClienteUseCase {

    record Command(
            String nombre,
            String email,
            String telefono,
            String password,
            PreferenciaNotificacion preferencia
    ) {}

    Cliente ejecutar(Command command);
}
