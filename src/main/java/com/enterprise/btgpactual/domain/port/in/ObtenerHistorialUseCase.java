package com.enterprise.btgpactual.domain.port.in;

import com.enterprise.btgpactual.domain.model.Transaccion;

import java.util.List;

public interface ObtenerHistorialUseCase {

    List<Transaccion> ejecutar(String clienteId);
}
