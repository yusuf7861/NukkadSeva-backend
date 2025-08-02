package com.nukkadseva.nukkadsevabackend.service.auditservice;

public interface AuditService {
    void logPasswordResetEvent(String userId, String email, boolean success, String details);
}
