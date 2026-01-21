/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.ipb_halle.lbac.security;

import de.ipb_halle.lbac.entity.UserSessionsEntity;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

/**
 *
 * @author halocal
 */
@Singleton
@Startup
public class SessionCleanupJob {

    @Inject
    private SessionService sessionService;

    @Schedule(hour = "*", minute = "*/1", persistent = false)
    public void cleanupExpiredSessions() {
        sessionService.deleteExpiredSessions();
    }
}
