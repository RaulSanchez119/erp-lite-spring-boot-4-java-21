package com.raulsanchez.erp_lite.persistence.mongo.repositories;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AuditLogRepository extends MongoRepository<AuditLogRepository, ObjectId> {
}
