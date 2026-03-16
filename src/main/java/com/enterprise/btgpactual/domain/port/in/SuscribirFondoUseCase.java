package com.enterprise.btgpactual.domain.port.in;

import com.enterprise.btgpactual.domain.model.Transaccion;

public interface SuscribirFondoUseCase {

    Transaccion ejecutar(String clienteId, String fondoId);
}
