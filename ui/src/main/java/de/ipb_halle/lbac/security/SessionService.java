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

    private static final int SESSION_TIMEOUT_MINUTES = 1; // adjust as needed

    private LocalDateTime getExpirationThreshold() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(SESSION_TIMEOUT_MINUTES);
        return threshold;
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
    public boolean isTokenExpired(String token) {

        UserSessionsEntity session = getUserSessionsEntityByToken(token);

        // token expired
        if (session == null) {
            return true;
        }

        boolean expiredSession = session.getLastSeen().isBefore(getExpirationThreshold());
        if (expiredSession) {
            System.out.println("expired session found for token: " + token + "!\n");
            deleteSessionByToken(token);
        } else {
            updateSession(token);
        }
        return expiredSession;
    }

    @Transactional
    public boolean isTokenValid(String token) {
        if (isTokenExpired(token)) {
            deleteSessionByToken(token);
            System.out.println("token is expired and deleted!\n");
            return false;
        }
        return true;
    }

    @Transactional
    public void deleteSessionByToken(String token) {
        em.createQuery("DELETE FROM UserSessionsEntity s WHERE s.token = :token")
                .setParameter("token", token)
                .executeUpdate();
    }

    @Transactional
    public void deleteExpiredSessions() {
        em.createQuery("DELETE FROM UserSessionsEntity s WHERE s.lastSeen < :threshold")
                .setParameter("threshold", getExpirationThreshold())
                .executeUpdate();
    }

    @Transactional
    public void updateSession(String token) {
        em.createQuery(
                "UPDATE UserSessionsEntity s SET s.lastSeen = :now WHERE s.token = :token")
                .setParameter("now", LocalDateTime.now())
                .setParameter("token", token)
                .executeUpdate();
    }
}
