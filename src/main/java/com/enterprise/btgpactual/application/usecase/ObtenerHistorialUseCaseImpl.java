package com.enterprise.btgpactual.application.usecase;

import com.enterprise.btgpactual.domain.exception.ClienteNoEncontradoException;
import com.enterprise.btgpactual.domain.model.Transaccion;
import com.enterprise.btgpactual.domain.port.in.ObtenerHistorialUseCase;
import com.enterprise.btgpactual.domain.port.out.ClienteRepositoryPort;
import com.enterprise.btgpactual.domain.port.out.TransaccionRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ObtenerHistorialUseCaseImpl implements ObtenerHistorialUseCase {

    private final ClienteRepositoryPort clienteRepository;
    private final TransaccionRepositoryPort transaccionRepository;

    @Override
    public List<Transaccion> ejecutar(String clienteId) {
        clienteRepository.buscarPorId(clienteId)
                .orElseThrow(() -> new ClienteNoEncontradoException(clienteId));

        return transaccionRepository.buscarPorClienteId(clienteId);
    }
}
