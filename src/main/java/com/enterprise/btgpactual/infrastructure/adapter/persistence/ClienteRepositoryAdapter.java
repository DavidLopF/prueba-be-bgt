package com.enterprise.btgpactual.infrastructure.adapter.persistence;

import com.enterprise.btgpactual.domain.exception.ClienteNoEncontradoException;
import com.enterprise.btgpactual.domain.model.Cliente;
import com.enterprise.btgpactual.domain.port.out.ClienteRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ClienteRepositoryAdapter implements ClienteRepositoryPort {

    private final ClienteMongoRepository mongoRepository;

    @Override
    public Optional<Cliente> buscarPorId(String id) {
        return mongoRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Cliente> buscarPorEmail(String email) {
        return mongoRepository.findByEmail(email).map(this::toDomain);
    }

    @Override
    public Cliente guardar(Cliente cliente) {
        ClienteDocument existing = mongoRepository.findById(cliente.getId())
                .orElseThrow(() -> new ClienteNoEncontradoException(cliente.getId()));
        ClienteDocument updated = toDocument(cliente, existing.getPasswordHash());
        return toDomain(mongoRepository.save(updated));
    }

    @Override
    public Cliente registrar(Cliente cliente, String passwordHash) {
        ClienteDocument doc = ClienteDocument.builder()
                .nombre(cliente.getNombre())
                .email(cliente.getEmail())
                .telefono(cliente.getTelefono())
                .saldo(cliente.getSaldo())
                .preferenciaNotificacion(cliente.getPreferenciaNotificacion())
                .fondosSuscritos(new HashSet<>(cliente.getFondosSuscritos()))
                .passwordHash(passwordHash)
                .build();
        return toDomain(mongoRepository.save(doc));
    }

    private Cliente toDomain(ClienteDocument doc) {
        return Cliente.builder()
                .id(doc.getId())
                .nombre(doc.getNombre())
                .email(doc.getEmail())
                .telefono(doc.getTelefono())
                .saldo(doc.getSaldo())
                .preferenciaNotificacion(doc.getPreferenciaNotificacion())
                .fondosSuscritos(new HashSet<>(doc.getFondosSuscritos()))
                .build();
    }

    private ClienteDocument toDocument(Cliente cliente, String passwordHash) {
        return ClienteDocument.builder()
                .id(cliente.getId())
                .nombre(cliente.getNombre())
                .email(cliente.getEmail())
                .telefono(cliente.getTelefono())
                .saldo(cliente.getSaldo())
                .preferenciaNotificacion(cliente.getPreferenciaNotificacion())
                .fondosSuscritos(new HashSet<>(cliente.getFondosSuscritos()))
                .passwordHash(passwordHash)
                .build();
    }
}
