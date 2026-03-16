package com.enterprise.btgpactual.infrastructure.adapter.persistence;

import com.enterprise.btgpactual.domain.model.Transaccion;
import com.enterprise.btgpactual.domain.port.out.TransaccionRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TransaccionRepositoryAdapter implements TransaccionRepositoryPort {

    private final TransaccionMongoRepository mongoRepository;

    @Override
    public Transaccion guardar(Transaccion transaccion) {
        return toDomain(mongoRepository.save(toDocument(transaccion)));
    }

    @Override
    public List<Transaccion> buscarPorClienteId(String clienteId) {
        return mongoRepository.findByClienteIdOrderByFechaDesc(clienteId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private Transaccion toDomain(TransaccionDocument doc) {
        return Transaccion.builder()
                .id(doc.getId())
                .clienteId(doc.getClienteId())
                .fondoId(doc.getFondoId())
                .fondoNombre(doc.getFondoNombre())
                .tipo(doc.getTipo())
                .monto(doc.getMonto())
                .fecha(doc.getFecha())
                .build();
    }

    private TransaccionDocument toDocument(Transaccion t) {
        return TransaccionDocument.builder()
                .id(t.getId())
                .clienteId(t.getClienteId())
                .fondoId(t.getFondoId())
                .fondoNombre(t.getFondoNombre())
                .tipo(t.getTipo())
                .monto(t.getMonto())
                .fecha(t.getFecha())
                .build();
    }
}
