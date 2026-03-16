package com.enterprise.btgpactual.domain.port.out;

import com.enterprise.btgpactual.domain.model.Transaccion;

import java.util.List;

public interface TransaccionRepositoryPort {

    Transaccion guardar(Transaccion transaccion);

    List<Transaccion> buscarPorClienteId(String clienteId);
}
