/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.ipb_halle.lbac.security.service;

import de.ipb_halle.lbac.entity.UserSessionsEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 *
 * @author halocal
 */
@ApplicationScoped
public class SessionService {

    @PersistenceContext
    private EntityManager em;

    private static final int SESSION_TIMEOUT_SECONDS = 60; // adjust as needed

    private LocalDateTime getExpirationThreshold() {
        return LocalDateTime.now().minusSeconds(SESSION_TIMEOUT_SECONDS);
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public UserSessionsEntity getUserSessionsEntityByToken(String token) {
        return em.createQuery(
                "SELECT s FROM UserSessionsEntity s WHERE s.token = :token",
                UserSessionsEntity.class)
                .setParameter("token", token)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    @Transactional
    public boolean isTokenExpired(String token, boolean updateLastSeen) {
        UserSessionsEntity session = getUserSessionsEntityByToken(token);
        // token expired
        if (session == null) {
            return true;
        }

        boolean expiredSession = session.getLastSeen().isBefore(getExpirationThreshold());
        if (expiredSession) {
            deleteSessionByToken(token);
        } else if(updateLastSeen) {
            updateSession(token);
        }
        return expiredSession;
    }

    @Transactional
    public boolean isTokenValid(String token, boolean updateLastSeen) {
        return !isTokenExpired(token, updateLastSeen);
    }

    public void deleteSessionByToken(String token) {
        em.createQuery("DELETE FROM UserSessionsEntity s WHERE s.token = :token")
                .setParameter("token", token)
                .executeUpdate();
    }

    @Transactional
    public void deleteExpiredSessions() {
        System.out.println("in delete, expire threshold: " + getExpirationThreshold());
        em.createQuery("DELETE FROM UserSessionsEntity s WHERE s.lastSeen < :threshold")
                .setParameter("threshold", getExpirationThreshold())
                .executeUpdate();
    }

    @Transactional
    public boolean updateSession(String token) {
        int updated = em.createQuery(
                "UPDATE UserSessionsEntity s "
                + "SET s.lastSeen = :now "
                + "WHERE s.token = :token")
                .setParameter("now", LocalDateTime.now())
                .setParameter("token", token)
                .executeUpdate();

        return updated == 1;
    }

    @Transactional
    public LocalDateTime getSessionExpiry(String token) {
        UserSessionsEntity session = getUserSessionsEntityByToken(token);
        if (session == null) {
            return null;
        }
        return session.getLastSeen().plusSeconds(SESSION_TIMEOUT_SECONDS);
         //return session.getLastSeen();
    }
}
