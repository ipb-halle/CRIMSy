/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.ipb_halle.lbac.security;

import de.ipb_halle.lbac.entity.UserSessionsEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

/**
 *
 * @author halocal
 */

@ApplicationScoped
public class SessionService {

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public void deleteSessionByToken(String token) {
        em.createQuery(
            "DELETE FROM UserSessionsEntity s WHERE s.token = :token"
        )
        .setParameter("token", token)
        .executeUpdate();
    }

    @Transactional
    public void deleteExpiredSessions() { 
    LocalDateTime timeouThreshold = LocalDateTime.now().minusMinutes(1);

        List<UserSessionsEntity> expiredSessions = em.createQuery(
                "SELECT s FROM UserSessionsEntity s WHERE s.lastSeen < :threshold",
                UserSessionsEntity.class)
                .setParameter("threshold", timeouThreshold)
                .getResultList();

        for (UserSessionsEntity sessionsEntity : expiredSessions) {
            em.remove(sessionsEntity);
        }
    }
    
}
