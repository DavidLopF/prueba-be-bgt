package com.enterprise.btgpactual.domain.port.in;

import com.enterprise.btgpactual.domain.model.Fondo;

import java.util.List;

public interface ListarFondosUseCase {

    List<Fondo> ejecutar();
}
