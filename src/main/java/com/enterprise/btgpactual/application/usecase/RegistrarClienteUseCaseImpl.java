package com.enterprise.btgpactual.application.usecase;

import com.enterprise.btgpactual.domain.exception.EmailYaRegistradoException;
import com.enterprise.btgpactual.domain.model.Cliente;
import com.enterprise.btgpactual.domain.port.in.RegistrarClienteUseCase;
import com.enterprise.btgpactual.domain.port.out.ClienteRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegistrarClienteUseCaseImpl implements RegistrarClienteUseCase {

    private final ClienteRepositoryPort clienteRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Cliente ejecutar(Command command) {
        if (clienteRepository.buscarPorEmail(command.email()).isPresent()) {
            throw new EmailYaRegistradoException(command.email());
        }

        Cliente nuevoCliente = Cliente.nuevo(
                command.nombre(),
                command.email(),
                command.telefono(),
                command.preferencia()
        );

        String passwordHash = passwordEncoder.encode(command.password());
        return clienteRepository.registrar(nuevoCliente, passwordHash);
    }
}
