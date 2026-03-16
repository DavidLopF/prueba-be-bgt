package com.enterprise.btgpactual.domain.port.out;

import com.enterprise.btgpactual.domain.model.Fondo;

import java.util.List;
import java.util.Optional;

public interface FondoRepositoryPort {

    Optional<Fondo> buscarPorId(String id);

    List<Fondo> buscarTodos();
}
