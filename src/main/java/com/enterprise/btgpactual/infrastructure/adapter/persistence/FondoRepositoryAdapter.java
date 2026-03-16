package com.enterprise.btgpactual.infrastructure.adapter.persistence;

import com.enterprise.btgpactual.domain.model.Fondo;
import com.enterprise.btgpactual.domain.port.out.FondoRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class FondoRepositoryAdapter implements FondoRepositoryPort {

    private final FondoMongoRepository mongoRepository;

    @Override
    public Optional<Fondo> buscarPorId(String id) {
        return mongoRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Fondo> buscarTodos() {
        return mongoRepository.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    private Fondo toDomain(FondoDocument doc) {
        return Fondo.builder()
                .id(doc.getId())
                .nombre(doc.getNombre())
                .montoMinimo(doc.getMontoMinimo())
                .categoria(doc.getCategoria())
                .build();
    }
}
