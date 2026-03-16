package com.enterprise.btgpactual.infrastructure.adapter.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TransaccionMongoRepository extends MongoRepository<TransaccionDocument, String> {

    List<TransaccionDocument> findByClienteIdOrderByFechaDesc(String clienteId);
}
