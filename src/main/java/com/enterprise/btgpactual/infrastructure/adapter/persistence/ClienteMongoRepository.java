package com.enterprise.btgpactual.infrastructure.adapter.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ClienteMongoRepository extends MongoRepository<ClienteDocument, String> {

    Optional<ClienteDocument> findByEmail(String email);
}
