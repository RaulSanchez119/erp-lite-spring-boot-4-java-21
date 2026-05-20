package com.raulsanchez.erp_lite.persistence.mongo.documents;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "audit_logs")
public class AuditLogDocument {

    @Id
    private ObjectId id;

    private String className;

    private String methodName;

    private String endpoint;

    private String ipAddress;

    private String userId;

    private Boolean success;

    private String errorMessage;

    private Long executionTimeMs;

    private Instant timestamp;
}
