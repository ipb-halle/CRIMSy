/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.ipb_halle.lbac.security;

import de.ipb_halle.lbac.entity.UserSessionsEntity;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
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

    @PersistenceContext
    private EntityManager em;

    @Schedule(hour = "*", minute = "*/1", persistent = false)
    @Transactional
    public void cleanupExpiredSessions() {
        LocalDateTime timeouThreshold = LocalDateTime.now().minusMinutes(1);

        List<UserSessionsEntity> expiredSessions = em.createQuery(
                "SELECT s FROM UserSessionsEntity s WHERE s.lastSeen < :threshold",
                UserSessionsEntity.class)
                .setParameter("threshold", timeouThreshold)
                .getResultList();

        for (UserSessionsEntity sessionsEntity : expiredSessions) {
            em.remove(sessionsEntity);
        }
/*        int deletedCount = em.createQuery(
                "DELETE FROM UserSessionsEntity s WHERE s.lastSeen < timeouThreshold")
                .setParameter("timeouThreshold", timeouThreshold)
                .executeUpdate();
        System.out.println("SessionCleanupJob: removed " + deletedCount + " expired sessions");*/
        System.out.println("SessionCleanupJob: removed " + expiredSessions + " expired sessions");
    }
}
