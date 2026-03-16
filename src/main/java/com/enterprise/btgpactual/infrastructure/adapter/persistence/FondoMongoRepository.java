package com.enterprise.btgpactual.infrastructure.adapter.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface FondoMongoRepository extends MongoRepository<FondoDocument, String> {
}
