package com.enterprise.btgpactual.application.usecase;

import com.enterprise.btgpactual.domain.model.Fondo;
import com.enterprise.btgpactual.domain.port.in.ListarFondosUseCase;
import com.enterprise.btgpactual.domain.port.out.FondoRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListarFondosUseCaseImpl implements ListarFondosUseCase {

    private final FondoRepositoryPort fondoRepository;

    @Override
    public List<Fondo> ejecutar() {
        return fondoRepository.buscarTodos();
    }
}
